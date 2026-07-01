package me.edoren.skin_changer.server;

import me.edoren.skin_changer.common.Constants;
import me.edoren.skin_changer.common.SkinChangerConfig;
import me.edoren.skin_changer.server.providers.CrafatarCapeProvider;
import me.edoren.skin_changer.server.providers.CrafatarSkinProvider;
import me.edoren.skin_changer.server.providers.MineskinSkinProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerController {
    private static ServerController singleInstance = null;

    private ServerController() {
    }

    public static ServerController GetInstance() {
        if (singleInstance == null)
            singleInstance = new ServerController();

        return singleInstance;
    }

    public void initialize(MinecraftServer server) {
        Path configDir = server.getServerDirectory().resolve("config");
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            LogManager.getLogger().warn("Failed to create config directory", e);
        }
        SkinChangerConfig.load(configDir);

        Path savesFolder = server.getWorldPath(LevelResource.ROOT);
        File skinChangerFolder = Paths.get(savesFolder.toString(), Constants.MOD_ID).normalize().toFile();

        SkinProviderController.GetInstance().initialize(skinChangerFolder);
        SkinProviderController.GetInstance().registerSkinProvider(new CrafatarSkinProvider());
        SkinProviderController.GetInstance().registerSkinProvider(new MineskinSkinProvider());
        SkinProviderController.GetInstance().registerCapeProvider(new CrafatarCapeProvider());
    }

    public void deinitialize(MinecraftServer server) {
        SkinProviderController.GetInstance().deinitialize();
    }
}
