package me.edoren.skin_changer.server;

import me.edoren.skin_changer.server.providers.CrafatarCapeProvider;
import me.edoren.skin_changer.server.providers.CrafatarSkinProvider;
import net.minecraft.server.MinecraftServer;

import java.io.File;
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

    public void initialize(MinecraftServer server) {
        SkinsCommand.register(server.getCommandManager().getDispatcher());

        String worldName = server.getServerConfiguration().getWorldName();
        File savesFolder = server.getDataDirectory();
        if (!server.isDedicatedServer()) {
            savesFolder = new File(savesFolder, "saves");
        }
        File skinChangerFolder = Paths.get(savesFolder.getPath(), worldName, "skin_changer").toFile();

        SkinProviderController.GetInstance().initialize(skinChangerFolder);
        SkinProviderController.GetInstance().registerSkinProvider(new CrafatarSkinProvider());
        SkinProviderController.GetInstance().registerCapeProvider(new CrafatarCapeProvider());
    }
}
