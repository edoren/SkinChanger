package me.edoren.skin_changer.client.api;

import com.mojang.authlib.GameProfile;
import me.edoren.skin_changer.client.ImageUtils;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkinLoaderService {
    private static SkinLoaderService singleInstance = null;

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