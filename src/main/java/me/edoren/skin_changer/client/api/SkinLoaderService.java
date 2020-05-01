package me.edoren.skin_changer.client.api;

import com.mojang.authlib.GameProfile;
import me.edoren.skin_changer.client.ImageUtils;
import me.edoren.skin_changer.common.NetworkContext;
import me.edoren.skin_changer.common.SharedPool;
import me.edoren.skin_changer.common.messages.PlayerSkinRequestMessage;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkinLoaderService {
    private static SkinLoaderService singleInstance = null;

    private final Map<UUID, Object> playerSkinRequests = new HashMap<>();

    private final Map<UUID, ISkin> loadedSkins = new HashMap<>();
    private final Map<UUID, ISkin> loadedCapes = new HashMap<>();

    public static SkinLoaderService GetInstance() {
        if (singleInstance == null)
            singleInstance = new SkinLoaderService();

        return singleInstance;
    }

    private SkinLoaderService() {
    }

    public void loadPlayerSkin(String uuidString, byte[] data) {
        UUID uuid = UUID.fromString(uuidString);

        if (data == null) {
            loadedSkins.remove(uuid);
            return;
        }

        LogManager.getLogger().info("Loading skin for player {}", uuid);

        if (ImageUtils.isNotValidData(data)) {
            LogManager.getLogger().info("Error loading skin for player {}", uuid);
            return;
        }

        SkinData skin = new SkinData();
        skin.setSkinFilter(ImageUtils::legacyFilter);
        skin.put(data, ImageUtils.judgeSkinType(data));
        loadedSkins.put(uuid, skin);

        if (playerSkinRequests.containsKey(uuid)) {
            synchronized (playerSkinRequests.get(uuid)) {
                playerSkinRequests.get(uuid).notify();
            }
        }
    }

    public void loadPlayerCape(String uuidString, byte[] data) {
        UUID uuid = UUID.fromString(uuidString);

        if (data == null) {
            loadedCapes.remove(uuid);
            return;
        }

        LogManager.getLogger().info("Loading cape for player {}", uuid);

        if (ImageUtils.isNotValidData(data)) {
            LogManager.getLogger().info("Error loading cape for player {}", uuid);
            return;
        }

        SkinData skin = new SkinData();
        skin.put(data, ImageUtils.judgeSkinType(data));
        loadedCapes.put(uuid, skin);
    }

    public void requestPlayerSkin(GameProfile profile) {
        if (loadedSkins.containsKey(profile.getId()) || playerSkinRequests.containsKey(profile.getId())) return;

        LogManager.getLogger().info("Requesting skin for player {} with id {}", profile.getName(), profile.getId());

        Object signal = new Object();
        playerSkinRequests.put(profile.getId(), signal);
        SharedPool.execute(() -> {
            PlayerSkinRequestMessage message = new PlayerSkinRequestMessage(profile);
            NetworkContext.GetInstance().getSimpleChannel().sendToServer(message);
            synchronized (signal) {
                try {
                    signal.wait(5000);
                } catch (InterruptedException e) {
                    LogManager.getLogger().info("Error loading skin for player {}", profile.getName());
                }
                playerSkinRequests.remove(profile.getId());
            }
        });
    }

    public ISkin getSkin(GameProfile profile) {
        if (profile == null || profile.getId() == null) {
            return null;
        }

        UUID key = profile.getId();
        return loadedSkins.get(key);
    }

    public ISkin getCape(GameProfile profile) {
        if (profile == null || profile.getId() == null) {
            return null;
        }

        UUID key = profile.getId();
        return loadedCapes.get(key);
    }

    public void clear() {
        loadedSkins.clear();
        loadedCapes.clear();
    }
}