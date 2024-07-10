package me.edoren.skin_changer.server;

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

    public static ServerController GetInstance() {
        if (singleInstance == null)
            singleInstance = new ServerController();

        return singleInstance;
    }

    private ServerController() {
    }

    public void setServer(MinecraftServer server) {
        SkinsCommand.register(server.getFunctions().getDispatcher());

        Path savesFolder = server.getWorldPath(LevelResource.ROOT);
        File skinChangerFolder = Paths.get(savesFolder.toString(), "skin_changer").normalize().toFile();

        SkinProviderController.GetInstance().setSaveFolder(skinChangerFolder);
        SkinProviderController.GetInstance().clearSkinProviders();
        SkinProviderController.GetInstance().clearCapeProviders();
        SkinProviderController.GetInstance().registerSkinProvider(new CrafatarSkinProvider());
        SkinProviderController.GetInstance().registerSkinProvider(new MineskinSkinProvider());
        SkinProviderController.GetInstance().registerCapeProvider(new CrafatarCapeProvider());
    }
}
