package me.edoren.skin_changer;

import me.edoren.skin_changer.client.ClientController;
import me.edoren.skin_changer.common.NetworkContext;
import me.edoren.skin_changer.server.ServerController;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("skin_changer")
public class SkinChanger {
    public SkinChanger() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onForgePreInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onForgePreInit(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkContext.GetInstance();
        });
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientController.GetInstance();
        });
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        ServerController.GetInstance().setServer(event.getServer());
    }
}
