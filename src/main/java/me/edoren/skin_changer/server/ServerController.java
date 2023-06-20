package me.edoren.skin_changer.server;

import me.edoren.skin_changer.common.Constants;
import me.edoren.skin_changer.server.permission.PermissionController;
import me.edoren.skin_changer.server.providers.CrafatarCapeProvider;
import me.edoren.skin_changer.server.providers.CrafatarSkinProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerController {
    private static ServerController singleInstance = null;

    private final PermissionController permissionController = new PermissionController();

    public static ServerController GetInstance() {
        if (singleInstance == null)
            singleInstance = new ServerController();

        return singleInstance;
    }

    private ServerController() {
        this.permissionController.initialize();
    }

    public void initialize(MinecraftServer server) {
        SkinsCommand.register(server.getFunctions().getDispatcher(), this.permissionController);

        Path savesFolder = server.getWorldPath(LevelResource.ROOT);
        File skinChangerFolder = Paths.get(savesFolder.toString(), Constants.MOD_ID).normalize().toFile();

        SkinProviderController.GetInstance().initialize(skinChangerFolder);
        SkinProviderController.GetInstance().registerSkinProvider(new CrafatarSkinProvider());
        SkinProviderController.GetInstance().registerCapeProvider(new CrafatarCapeProvider());
    }
}
