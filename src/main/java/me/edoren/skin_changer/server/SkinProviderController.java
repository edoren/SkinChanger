package me.edoren.skin_changer.server;

import com.mojang.authlib.GameProfile;
import me.edoren.skin_changer.common.NetworkContext;
import me.edoren.skin_changer.common.NetworkUtils;
import me.edoren.skin_changer.common.SharedPool;
import me.edoren.skin_changer.common.messages.PlayerSkinUpdateMessage;
import me.edoren.skin_changer.common.models.PlayerSkinData;
import me.edoren.skin_changer.server.providers.ISkinProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;

import java.io.File;
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

    private final Map<DataType, List<ISkinProvider>> providers = new HashMap<>();
    private final Map<DataType, Map<UUID, byte[]>> loadedData = new HashMap<>();
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
    public void initialize(String saveFolder) {
        File skinsDir = new File(saveFolder, DataType.SKIN + "s");
        File capesDir = new File(saveFolder, DataType.CAPE + "s");
        skinsDir.mkdirs();
        capesDir.mkdirs();
        cacheFolders.put(DataType.SKIN, skinsDir.getPath());
        cacheFolders.put(DataType.CAPE, capesDir.getPath());

        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogin);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogout);
    }

    public void registerCapeProvider(ISkinProvider provider) {
        providers.get(DataType.CAPE).add(provider);
    }

    public void registerSkinProvider(ISkinProvider provider) {
        providers.get(DataType.SKIN).add(provider);
    }

    public boolean getPlayerSkin(GameProfile profile, ServerPlayerEntity requestingPlayer) {
        if (loadedData.get(DataType.SKIN).containsKey(profile.getId()) || loadPlayerDataFromCache(profile, DataType.SKIN)) {
            sendDataToTarget(profile.getId(), requestingPlayer);
            return true;
        }
        return false;
    }

    public boolean setPlayerSkinByName(GameProfile profile, String playerName, boolean cache) {
        return setPlayerDataByName(profile, playerName, cache, DataType.SKIN);
    }

    public boolean setPlayerSkinByURL(GameProfile profile, URL url, boolean cache) {
        return setPlayerDataByURL(profile, url, cache, DataType.SKIN);
    }

    public boolean setPlayerCapeByName(GameProfile profile, String playerName, boolean cache) {
        return setPlayerDataByName(profile, playerName, cache, DataType.CAPE);
    }

    public boolean setPlayerCapeByURL(GameProfile profile, URL url, boolean cache) {
        return setPlayerDataByURL(profile, url, cache, DataType.CAPE);
    }

    private boolean setPlayerDataByName(GameProfile profile, String playerName, boolean cache, DataType dataType) {
        for (ISkinProvider provider : providers.get(dataType)) {
            byte[] data = provider.getSkin(playerName);
            if (data != null) {
                return storePlayerData(profile, data, cache, dataType);
            }
        }
        return false;
    }

    private boolean setPlayerDataByURL(GameProfile profile, URL url, boolean cache, DataType dataType) {
        byte[] data = NetworkUtils.downloadFile(url.toString(), null, 2);
        return storePlayerData(profile, data, cache, dataType);
    }

    public void cleanPlayerSkin(GameProfile profile) {
        cleanPlayerData(profile, DataType.SKIN);
    }

    public void cleanPlayerCape(GameProfile profile) {
        cleanPlayerData(profile, DataType.CAPE);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void cleanPlayerData(GameProfile profile, DataType dataType) {
        if (loadedData.get(dataType).containsKey(profile.getId())) {
            loadedData.get(dataType).remove(profile.getId());
            sendPlayerDataToAll(profile.getId());
            File capeFile = new File(cacheFolders.get(dataType), profile.getId().toString());
            LogManager.getLogger().info("Removing {} for player {} with id {}", dataType, profile.getName(), profile.getId());
            if (capeFile.isFile()) {
                capeFile.delete();
            }
        }
    }

    private boolean storePlayerData(GameProfile profile, byte[] data, boolean cache, DataType dataType) {
        if (data == null) return false;
        UUID key = profile.getId();
        loadedData.get(dataType).put(key, data);
        if (cache)
            saveBytesToFile(Paths.get(cacheFolders.get(dataType), key.toString()), data);
        sendPlayerDataToAll(key);
        LogManager.getLogger().info("Loaded {} for player {} with id {}", dataType, profile.getName(), profile.getId());
        return true;
    }

    private void sendDataToTarget(UUID playerId, ServerPlayerEntity target) {
        Vector<PlayerSkinData> playerSkinData = new Vector<>();
        playerSkinData.add(getPlayerSkinData(playerId));
        PlayerSkinUpdateMessage message = new PlayerSkinUpdateMessage(playerSkinData);
        NetworkContext.GetInstance().getSimpleChannel().send(PacketDistributor.PLAYER.with(() -> target), message);
    }

    private void sendAllDataToTarget(ServerPlayerEntity target) {
        Vector<PlayerSkinData> playerSkinData = new Vector<>();

        Set<UUID> set = new HashSet<>();
        set.addAll(loadedData.get(DataType.SKIN).keySet());
        set.addAll(loadedData.get(DataType.CAPE).keySet());

        set.forEach((uuid) -> playerSkinData.add(getPlayerSkinData(uuid)));

        PlayerSkinUpdateMessage message = new PlayerSkinUpdateMessage(playerSkinData);
        NetworkContext.GetInstance().getSimpleChannel().send(PacketDistributor.PLAYER.with(() -> target), message);
    }

    private void sendPlayerDataToAll(UUID playerId) {
        Vector<PlayerSkinData> playerSkinData = new Vector<>();
        playerSkinData.add(getPlayerSkinData(playerId));
        PlayerSkinUpdateMessage message = new PlayerSkinUpdateMessage(playerSkinData);
        NetworkContext.GetInstance().getSimpleChannel().send(PacketDistributor.ALL.noArg(), message);
    }

    private void saveBytesToFile(final Path filePath, final byte[] bytes) {
        SharedPool.execute(() -> {
            try {
                Files.write(filePath, bytes);
                LogManager.getLogger().info("Caching file {}", filePath.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private PlayerSkinData getPlayerSkinData(UUID playerId) {
        String uuid = playerId.toString();
        byte[] skin = loadedData.get(DataType.SKIN).get(playerId);
        byte[] cape = loadedData.get(DataType.CAPE).get(playerId);
        return new PlayerSkinData(uuid, skin, cape);
    }

    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        GameProfile profile = event.getPlayer().getGameProfile();
        LogManager.getLogger().info("Player {} just logged in with id {}", profile.getName(), profile.getId());
        SharedPool.get().execute(() -> {
            sendAllDataToTarget(player);
            boolean succeeded = loadPlayerDataFromCache(profile, DataType.SKIN);
            succeeded = succeeded || loadPlayerDataFromCache(profile, DataType.CAPE);
            if (succeeded) {
                sendPlayerDataToAll(profile.getId());
            }
        });
    }

    public boolean loadPlayerDataFromCache(GameProfile profile, DataType dataType) {
        File file = new File(cacheFolders.get(dataType), profile.getId().toString());
        if (file.isFile()) {
            try {
                byte[] data = Files.readAllBytes(file.toPath());
                loadedData.get(dataType).put(profile.getId(), data);
                LogManager.getLogger().info("Loading local {} for player {} with id {}", dataType, profile.getName(), profile.getId());
                return true;
            } catch (IOException ignored) {
            }
        }
        return false;
    }

    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        GameProfile profile = event.getPlayer().getGameProfile();
        if (loadedData.get(DataType.SKIN).containsKey(profile.getId()) || loadedData.get(DataType.CAPE).containsKey(profile.getId())) {
            LogManager.getLogger().info("Removing session data for player {} with id {}", profile.getName(), profile.getId());
            loadedData.get(DataType.SKIN).remove(profile.getId());
            loadedData.get(DataType.CAPE).remove(profile.getId());
            sendPlayerDataToAll(profile.getId());
        }
    }
}