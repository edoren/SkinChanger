package me.edoren.skin_changer.server;

import me.edoren.skin_changer.common.Constants;
import me.edoren.skin_changer.server.providers.CrafatarCapeProvider;
import me.edoren.skin_changer.server.providers.CrafatarSkinProvider;
import me.edoren.skin_changer.server.providers.MineskinSkinProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
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
        Path savesFolder = server.getWorldPath(LevelResource.ROOT);
        File skinChangerFolder = Paths.get(savesFolder.toString(), Constants.MOD_ID).normalize().toFile();

        SkinProviderController.GetInstance().initialize(skinChangerFolder);
        SkinProviderController.GetInstance().registerSkinProvider(new CrafatarSkinProvider());
        SkinProviderController.GetInstance().registerSkinProvider(new MineskinSkinProvider());
        SkinProviderController.GetInstance().registerCapeProvider(new CrafatarCapeProvider());
    }
}
