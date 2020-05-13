package me.edoren.skin_changer.client.api;

import me.edoren.skin_changer.client.ImageUtils;
import me.edoren.skin_changer.common.NetworkContext;
import me.edoren.skin_changer.common.SharedPool;
import me.edoren.skin_changer.common.messages.PlayerSkinRequestMessage;
import me.edoren.skin_changer.common.models.PlayerModel;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Map;

public class SkinLoaderService {
    private static SkinLoaderService singleInstance = null;

    private final Map<PlayerModel, Object> playerSkinRequests = new HashMap<>();

    private final Map<PlayerModel, ISkin> loadedSkins = new HashMap<>();
    private final Map<PlayerModel, ISkin> loadedCapes = new HashMap<>();

    public static SkinLoaderService GetInstance() {
        if (singleInstance == null)
            singleInstance = new SkinLoaderService();

        return singleInstance;
    }

    private SkinLoaderService() {
    }

    public void loadPlayerSkin(PlayerModel model, byte[] data) {
        if (data == null) {
            loadedSkins.remove(model);
            return;
        }

        LogManager.getLogger().info("Loading skin for player {}", model);

        if (ImageUtils.isNotValidData(data)) {
            LogManager.getLogger().info("Error loading skin for player {}", model);
            return;
        }

        SkinData skin = new SkinData();
        skin.setSkinFilter(ImageUtils::legacyFilter);
        skin.put(data, ImageUtils.judgeSkinType(data));
        loadedSkins.put(model, skin);

        if (playerSkinRequests.containsKey(model)) {
            synchronized (playerSkinRequests.get(model)) {
                playerSkinRequests.get(model).notify();
            }
        }
    }

    public void loadPlayerCape(PlayerModel model, byte[] data) {
        if (data == null) {
            loadedCapes.remove(model);
            return;
        }

        LogManager.getLogger().info("Loading cape for player {}", model);

        if (ImageUtils.isNotValidData(data)) {
            LogManager.getLogger().info("Error loading cape for player {}", model);
            return;
        }

        SkinData skin = new SkinData();
        skin.put(data, ImageUtils.judgeSkinType(data));
        loadedCapes.put(model, skin);
    }

    public void requestPlayerSkin(PlayerModel model) {
        if (loadedSkins.containsKey(model) || playerSkinRequests.containsKey(model)) return;

        LogManager.getLogger().info("Requesting skin for player {}", model);

        Object signal = new Object();
        playerSkinRequests.put(model, signal);
        SharedPool.execute(() -> {
            PlayerSkinRequestMessage message = new PlayerSkinRequestMessage(model);
            NetworkContext.GetInstance().getSimpleChannel().sendToServer(message);
            synchronized (signal) {
                try {
                    signal.wait(5000);
                } catch (InterruptedException e) {
                    LogManager.getLogger().info("Error loading skin for player {}", model);
                }
                playerSkinRequests.remove(model);
            }
        });
    }

    public ISkin getSkin(PlayerModel model) {
        if (model == null || model.getId() == null) {
            return null;
        }
        return loadedSkins.get(model);
    }

    public ISkin getCape(PlayerModel model) {
        if (model == null || model.getId() == null) {
            return null;
        }
        return loadedCapes.get(model);
    }

    public void clear() {
        loadedSkins.clear();
        loadedCapes.clear();
    }
}