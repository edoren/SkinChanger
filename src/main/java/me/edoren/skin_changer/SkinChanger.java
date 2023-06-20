package me.edoren.skin_changer;

import me.edoren.skin_changer.client.ClientController;
import me.edoren.skin_changer.common.Constants;
import me.edoren.skin_changer.common.NetworkContext;
import me.edoren.skin_changer.server.ServerController;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class SkinChanger {
    public SkinChanger() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onForgePreInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);

        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
    }

    private void onForgePreInit(final FMLCommonSetupEvent event) {
        NetworkContext.GetInstance().initialize();
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        // LogManager.getLogger().info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
        ClientController.GetInstance().initialize();
    }

    private void onServerStarted(ServerAboutToStartEvent event) {
        ServerController.GetInstance().initialize(event.getServer());
    }
}
