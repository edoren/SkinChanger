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
    private static SkinProviderController singleInstance = null;

    private final List<ISkinProvider> capeProviders = new ArrayList<>();
    private final List<ISkinProvider> skinProviders = new ArrayList<>();

    private final Map<UUID, byte[]> loadedSkins = new HashMap<>();
    private final Map<UUID, byte[]> loadedCapes = new HashMap<>();

    private String skinsFolder;
    private String capesFolder;

    public static SkinProviderController GetInstance() {
        if (singleInstance == null)
            singleInstance = new SkinProviderController();

        return singleInstance;
    }

    private SkinProviderController() {
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void initialize(String saveFolder) {
        File skinsDir = new File(saveFolder, "skins");
        File capesDir = new File(saveFolder, "capes");
        skinsDir.mkdirs();
        capesDir.mkdirs();
        this.skinsFolder = skinsDir.getPath();
        this.capesFolder = capesDir.getPath();

        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogin);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogout);
    }

    public void registerCapeProvider(ISkinProvider provider) {
        capeProviders.add(provider);
    }

    public void registerSkinProvider(ISkinProvider provider) {
        skinProviders.add(provider);
    }

    public boolean setPlayerSkin(GameProfile profile, String playerName, boolean cache) {
        for (ISkinProvider provider : skinProviders) {
            byte[] skin = provider.getSkin(playerName);
            if (skin != null) {
                return storePlayerSkin(profile, skin, cache);
            }
        }
        return false;
    }

    public boolean setPlayerSkin(GameProfile profile, URL url, boolean cache) {
        byte[] skin = NetworkUtils.downloadFile(url.toString(), null, 2);
        return storePlayerSkin(profile, skin, cache);
    }

    public boolean setPlayerCape(GameProfile profile, String playerName, boolean cache) {
        for (ISkinProvider provider : capeProviders) {
            byte[] cape = provider.getSkin(playerName);
            if (cape != null) {
                return storePlayerCape(profile, cape, cache);
            }
        }
        return false;
    }

    public boolean setPlayerCape(GameProfile profile, URL url, boolean cache) {
        byte[] cape = NetworkUtils.downloadFile(url.toString(), null, 2);
        return storePlayerCape(profile, cape, cache);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void cleanPlayerSkin(GameProfile profile) {
        if (loadedSkins.containsKey(profile.getId())) {
            LogManager.getLogger().info("Removing skin for player {} with id {}", profile.getName(), profile.getId());
            loadedSkins.remove(profile.getId());
            sendPlayerData(profile.getId());
            File skinFile = new File(skinsFolder, profile.getId().toString());
            if (skinFile.isFile()) {
                skinFile.delete();
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void cleanPlayerCape(GameProfile profile) {
        if (loadedCapes.containsKey(profile.getId())) {
            LogManager.getLogger().info("Removing cape for player {} with id {}", profile.getName(), profile.getId());
            loadedCapes.remove(profile.getId());
            sendPlayerData(profile.getId());
            File capeFile = new File(capesFolder, profile.getId().toString());
            if (capeFile.isFile()) {
                capeFile.delete();
            }
        }
    }

    private boolean storePlayerSkin(GameProfile profile, byte[] data, boolean cache) {
        if (data == null) return false;
        UUID key = profile.getId();
        loadedSkins.put(key, data);
        LogManager.getLogger().info("Loaded skin for player {} with id {}", profile.getName(), key);
        if (cache) {
            saveBytesToFile(Paths.get(skinsFolder, key.toString()), data);
        }
        sendPlayerData(key);
        return true;
    }

    private boolean storePlayerCape(GameProfile profile, byte[] data, boolean cache) {
        if (data == null) return false;
        UUID key = profile.getId();
        loadedCapes.put(key, data);
        LogManager.getLogger().info("Loaded cape for player {} with id {}", profile.getName(), key);
        if (cache) {
            saveBytesToFile(Paths.get(capesFolder, key.toString()), data);
        }
        sendPlayerData(key);
        return true;
    }

    private void sendAllDataToTarget(ServerPlayerEntity target) {
        Vector<PlayerSkinData> playerSkinData = new Vector<>();

        Set<UUID> set = new HashSet<>();
        set.addAll(loadedSkins.keySet());
        set.addAll(loadedCapes.keySet());

        set.forEach((uuid) -> playerSkinData.add(getPlayerSkinData(uuid)));

        PlayerSkinUpdateMessage message = new PlayerSkinUpdateMessage(playerSkinData);
        NetworkContext.GetInstance().getSimpleChannel().send(PacketDistributor.PLAYER.with(() -> target), message);
    }

    private void sendPlayerData(UUID playerId) {
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
        byte[] skin = loadedSkins.get(playerId);
        byte[] cape = loadedCapes.get(playerId);
        return new PlayerSkinData(uuid, skin, cape);
    }

    private byte[] loadBytesFromFile(Path filePath) {
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            return null;
        }
    }

    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        GameProfile profile = event.getPlayer().getGameProfile();
        LogManager.getLogger().info("Player {} just logged in with id {}", profile.getName(), profile.getId());
        SharedPool.get().execute(() -> {
            sendAllDataToTarget(player);

            UUID playerUUID = profile.getId();

            File skinFile = new File(skinsFolder, playerUUID.toString());
            File capeFile = new File(capesFolder, playerUUID.toString());

            if (skinFile.isFile()) {
                byte[] data = loadBytesFromFile(skinFile.toPath());
                if (data != null) {
                    loadedSkins.put(playerUUID, data);
                    LogManager.getLogger().info("Loaded skin for player {} with id {}", profile.getName(), playerUUID);
                }
            }

            if (capeFile.isFile()) {
                byte[] data = loadBytesFromFile(capeFile.toPath());
                if (data != null) {
                    loadedCapes.put(playerUUID, data);
                    LogManager.getLogger().info("Loaded cape for player {} with id {}", profile.getName(), playerUUID);
                }
            }

            sendPlayerData(playerUUID);
        });
    }

    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        GameProfile profile = event.getPlayer().getGameProfile();
        if (loadedSkins.containsKey(profile.getId()) || loadedCapes.containsKey(profile.getId())) {
            LogManager.getLogger().info("Removing data for player {} with id {}", profile.getName(), profile.getId());
            loadedSkins.remove(profile.getId());
            loadedCapes.remove(profile.getId());
            sendPlayerData(profile.getId());
        }
    }
}