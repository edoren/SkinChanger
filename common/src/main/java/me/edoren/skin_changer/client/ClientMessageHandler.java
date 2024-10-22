package me.edoren.skin_changer.client;

import dev.architectury.networking.NetworkManager.PacketContext;
import me.edoren.skin_changer.client.api.SkinLoaderService;
import me.edoren.skin_changer.common.messages.PlayerSkinUpdateMessage;
import me.edoren.skin_changer.common.models.PlayerSkinModel;
import net.fabricmc.api.EnvType;
import org.apache.logging.log4j.LogManager;

import java.util.Vector;
import java.util.function.Supplier;

public class ClientMessageHandler {

    /**
     * Called when a message is received of the appropriate type.
     * CALLED BY THE NETWORK THREAD, NOT THE CLIENT THREAD
     */
    public static void onMessageReceived(final PlayerSkinUpdateMessage message, Supplier<PacketContext> ctxSupplier) {
        PacketContext ctx = ctxSupplier.get();

        EnvType toPlatform = ctx.getEnvironment().toPlatform();
        if (toPlatform != EnvType.CLIENT) {
            LogManager.getLogger().warn("PlayerSkinUpdateMessage received on wrong side: {}", toPlatform);
            return;
        }

        if (!message.isMessageValid()) {
            LogManager.getLogger().warn("PlayerSkinUpdateMessage was invalid: {}", message);
            return;
        }

        // We know for sure that this handler is only used on the client side, so it is ok to assume
        // that the ctx handler is a client, and that Minecraft exists.
//        Optional<Level> clientWorld = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
//        if (clientWorld.isEmpty()) {
//            LogManager.getLogger().warn("PlayerSkinUpdateMessage context could not provide a Level.");
//            return;
//        }

        // This code creates a new task which will be executed by the client during the next tick
        ctx.queue(() -> // make sure it's only executed on the client
            processMessage(message)
        );
    }

    private static void processMessage(PlayerSkinUpdateMessage message) {
        Vector<PlayerSkinModel> skinData = message.getAllSkinData();
        if (skinData.isEmpty()) {
            SkinLoaderService.GetInstance().clear();
            return;
        }

        for (PlayerSkinModel playerData : skinData) {
            SkinLoaderService.GetInstance().loadPlayerSkin(playerData.getPlayer(), playerData.getSkin());
            SkinLoaderService.GetInstance().loadPlayerCape(playerData.getPlayer(), playerData.getCape());
        }
    }
}
