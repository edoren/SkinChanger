package me.edoren.skin_changer.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import me.edoren.skin_changer.common.NetworkContext;
import me.edoren.skin_changer.common.NetworkUtils;
import me.edoren.skin_changer.common.SharedPool;
import me.edoren.skin_changer.common.messages.PlayerSkinUpdateMessage;
import me.edoren.skin_changer.common.models.PlayerModel;
import me.edoren.skin_changer.common.models.PlayerSkinModel;
import me.edoren.skin_changer.server.providers.ISkinProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SkinProviderController {
    enum DataType {
        SKIN("skin"),
        CAPE("cape");

        private final String internal;

        DataType(String envUrl) {
            this.internal = envUrl;
        }

        @Override
        public String toString() {
            return internal;
        }
    }

    private static SkinProviderController singleInstance = null;

    private String cacheFile;
    private final Map<DataType, List<ISkinProvider>> providers = new HashMap<>();
    private final Map<DataType, Map<PlayerModel, byte[]>> loadedData = new HashMap<>();
    private final Map<DataType, String> cacheFolders = new HashMap<>();

    public static SkinProviderController GetInstance() {
        if (singleInstance == null)
            singleInstance = new SkinProviderController();

        return singleInstance;
    }

    private SkinProviderController() {
        providers.put(DataType.SKIN, new ArrayList<>());
        providers.put(DataType.CAPE, new ArrayList<>());
        loadedData.put(DataType.SKIN, new HashMap<>());
        loadedData.put(DataType.CAPE, new HashMap<>());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void initialize(File saveFolder) {
        File skinsDir = new File(saveFolder, DataType.SKIN + "s");
        File capesDir = new File(saveFolder, DataType.CAPE + "s");
        skinsDir.mkdirs();
        capesDir.mkdirs();
        cacheFolders.put(DataType.SKIN, skinsDir.getPath());
        cacheFolders.put(DataType.CAPE, capesDir.getPath());

        File cacheFile = new File(saveFolder, "cache.json");
        try {
            cacheFile.createNewFile();
        } catch (IOException ignored) {
        }
        this.cacheFile = cacheFile.getPath();

        MinecraftForge.EVENT_BUS.unregister(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void registerCapeProvider(ISkinProvider provider) {
        providers.get(DataType.CAPE).add(provider);
    }

    public void registerSkinProvider(ISkinProvider provider) {
        providers.get(DataType.SKIN).add(provider);
    }

    public boolean getPlayerSkin(GameProfile profile, ServerPlayer requestingPlayer) {
        PlayerModel model = new PlayerModel(profile);
        if (loadedData.get(DataType.SKIN).containsKey(model) || loadPlayerDataFromCache(model, DataType.SKIN)) {
            sendDataToTarget(model, requestingPlayer);
            return true;
        }
        return false;
    }

    public boolean setPlayerSkinByName(GameProfile profile, String playerName, boolean cache) {
        return setPlayerDataByName(new PlayerModel(profile), playerName, cache, DataType.SKIN);
    }

    public boolean setPlayerSkinByURL(GameProfile profile, URL url, boolean cache) {
        return setPlayerDataByURL(new PlayerModel(profile), url, cache, DataType.SKIN);
    }

    public boolean setPlayerCapeByName(GameProfile profile, String playerName, boolean cache) {
        return setPlayerDataByName(new PlayerModel(profile), playerName, cache, DataType.CAPE);
    }

    public boolean setPlayerCapeByURL(GameProfile profile, URL url, boolean cache) {
        return setPlayerDataByURL(new PlayerModel(profile), url, cache, DataType.CAPE);
    }

    private boolean setPlayerDataByName(PlayerModel model, String playerName, boolean cache, DataType dataType) {
        for (ISkinProvider provider : providers.get(dataType)) {
            byte[] data = provider.getSkin(playerName);
            if (data != null) {
                return storePlayerData(model, data, cache, dataType);
            }
        }
        return false;
    }

    private boolean setPlayerDataByURL(PlayerModel model, URL url, boolean cache, DataType dataType) {
        byte[] data = NetworkUtils.downloadFile(url.toString(), null, 2);
        return storePlayerData(model, data, cache, dataType);
    }

    public void cleanPlayerSkin(GameProfile profile) {
        cleanPlayerData(new PlayerModel(profile), DataType.SKIN);
    }

    public void cleanPlayerCape(GameProfile profile) {
        cleanPlayerData(new PlayerModel(profile), DataType.CAPE);
    }

    private void cleanPlayerData(PlayerModel model, DataType dataType) {
        deletePlayerDataFromCache(model, dataType);
        if (loadedData.get(dataType).containsKey(model)) {
            loadedData.get(dataType).remove(model);
            sendPlayerDataToAll(model);
        }
    }

    private boolean storePlayerData(PlayerModel model, byte[] data, boolean cache, DataType dataType) {
        if (data == null) return false;
        loadedData.get(dataType).put(model, data);
        if (cache)
            savePlayerDataToCache(dataType, model, data);
        sendPlayerDataToAll(model);
        LogManager.getLogger().info("Loaded {} for player {}", dataType, model);
        return true;
    }

    private void sendDataToTarget(PlayerModel profile, ServerPlayer target) {
        Vector<PlayerSkinModel> playerSkinData = new Vector<>();
        playerSkinData.add(getPlayerSkinData(profile));
        PlayerSkinUpdateMessage message = new PlayerSkinUpdateMessage(playerSkinData);
        NetworkContext.GetInstance().getSimpleChannel().send(PacketDistributor.PLAYER.with(() -> target), message);
    }

    private void sendAllDataToTarget(ServerPlayer target) {
        Vector<PlayerSkinModel> playerSkinData = new Vector<>();

        Set<PlayerModel> set = new HashSet<>();
        set.addAll(loadedData.get(DataType.SKIN).keySet());
        set.addAll(loadedData.get(DataType.CAPE).keySet());

        set.forEach((model) -> playerSkinData.add(getPlayerSkinData(model)));

        PlayerSkinUpdateMessage message = new PlayerSkinUpdateMessage(playerSkinData);
        NetworkContext.GetInstance().getSimpleChannel().send(PacketDistributor.PLAYER.with(() -> target), message);
    }

    private void sendPlayerDataToAll(PlayerModel profile) {
        Vector<PlayerSkinModel> playerSkinData = new Vector<>();
        playerSkinData.add(getPlayerSkinData(profile));
        PlayerSkinUpdateMessage message = new PlayerSkinUpdateMessage(playerSkinData);
        NetworkContext.GetInstance().getSimpleChannel().send(PacketDistributor.ALL.noArg(), message);
    }

    private PlayerSkinModel getPlayerSkinData(PlayerModel profile) {
        byte[] skin = loadedData.get(DataType.SKIN).get(profile);
        byte[] cape = loadedData.get(DataType.CAPE).get(profile);
        return new PlayerSkinModel(profile, skin, cape);
    }

    private void savePlayerDataToCache(DataType dataType, PlayerModel model, final byte[] bytes) {
        SharedPool.execute(() -> {
            try {
                String fileUUID = model.getId();

                List<PlayerModel> playerModels = readCacheFile();

                // Check if player model is already stored
                if (playerModels.contains(model)) {
                    for (PlayerModel playerModel : playerModels) {
                        if (playerModel.equals(model)) {
                            fileUUID = playerModel.getId();
                            break;
                        }
                    }
                } else {
                    playerModels.add(model);
                }

                writeCacheFile(playerModels);

                Path filePath = Paths.get(cacheFolders.get(dataType), fileUUID);
                Files.write(filePath, bytes);
                LogManager.getLogger().info("Caching file {}", filePath.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean loadPlayerDataFromCache(PlayerModel model, DataType dataType) {
        try {
            List<PlayerModel> playerModels = readCacheFile();
            for (PlayerModel playerModel : playerModels) {
                if (playerModel.equals(model)) {
                    File file = new File(cacheFolders.get(dataType), playerModel.getId());
                    if (file.isFile()) {
                        byte[] data = Files.readAllBytes(file.toPath());
                        loadedData.get(dataType).put(playerModel, data);
                        LogManager.getLogger().info("Loading local {} for player {}", dataType, playerModel);
                        return true;
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void deletePlayerDataFromCache(PlayerModel model, DataType dataType) {
        SharedPool.execute(() -> {
            try {
                List<PlayerModel> playerModels = readCacheFile();
                for (int i = 0; i < playerModels.size(); i++) {
                    PlayerModel playerModel = playerModels.get(i);
                    if (model.equals(playerModel)) {
                        File dataFileSkin = new File(cacheFolders.get(DataType.SKIN), playerModel.getId());
                        File dataFileCape = new File(cacheFolders.get(DataType.CAPE), playerModel.getId());

                        File fileToRemove = dataType.equals(DataType.SKIN) ? dataFileSkin : dataFileCape;

                        if (fileToRemove.isFile() && fileToRemove.delete()) {
                            LogManager.getLogger().info("Removing {} for player {}", dataType, model);
                        }

                        if (!dataFileSkin.isFile() && !dataFileSkin.isFile()) {
                            playerModels.remove(i);
                            writeCacheFile(playerModels);
                        }
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private List<PlayerModel> readCacheFile() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileReader fr = new FileReader(cacheFile);
        PlayerModel[] playerModels = gson.fromJson(fr, PlayerModel[].class);
        fr.close();
        return playerModels != null ? new ArrayList(Arrays.asList(playerModels)) : new ArrayList<>();
    }

    private void writeCacheFile(List<PlayerModel> playersCache) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter fw = new FileWriter(cacheFile);
        gson.toJson(playersCache, fw);
        fw.close();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        GameProfile profile = event.getEntity().getGameProfile();
        LogManager.getLogger().info("Player {} just logged in with id {}", profile.getName(), profile.getId());
        SharedPool.get().execute(() -> {
            sendAllDataToTarget(player);
            PlayerModel model = new PlayerModel(profile);
            boolean skinLoaded = loadPlayerDataFromCache(model, DataType.SKIN);
            boolean capeLoaded = loadPlayerDataFromCache(model, DataType.CAPE);
            if (!skinLoaded) {
                skinLoaded = setPlayerSkinByName(profile, profile.getName(), true);
            }
            if (!capeLoaded) {
                capeLoaded = setPlayerCapeByName(profile, profile.getName(), true);
            }
            if (skinLoaded || capeLoaded) {
                sendPlayerDataToAll(model);
            }
        });
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        GameProfile profile = event.getEntity().getGameProfile();
        PlayerModel model = new PlayerModel(profile);
        if (loadedData.get(DataType.SKIN).containsKey(model) || loadedData.get(DataType.CAPE).containsKey(model)) {
            LogManager.getLogger().info("Removing session data for player {}[{}]", profile.getName(), profile.getId());
            loadedData.get(DataType.SKIN).remove(model);
            loadedData.get(DataType.CAPE).remove(model);
            sendPlayerDataToAll(model);
        }
    }
}