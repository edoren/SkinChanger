package me.edoren.skin_changer.server;

import com.mojang.authlib.GameProfile;
import dev.architectury.networking.NetworkManager.PacketContext;
import me.edoren.skin_changer.common.SharedPool;
import me.edoren.skin_changer.common.messages.PlayerSkinRequestMessage;
import net.fabricmc.api.EnvType;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;

import java.util.function.Supplier;

public class ServerMessageHandler {

    /**
     * Called when a message is received of the appropriate type.
     * CALLED BY THE NETWORK THREAD, NOT THE CLIENT THREAD
     */
    public static void onMessageReceived(final PlayerSkinRequestMessage message, Supplier<PacketContext> ctxSupplier) {
        PacketContext ctx = ctxSupplier.get();

        EnvType toPlatform = ctx.getEnvironment().toPlatform();
        if (toPlatform != EnvType.SERVER) {
            LogManager.getLogger().warn("PlayerSkinRequestMessage received on wrong side: {}", toPlatform);
            return;
        }

        if (!message.isMessageValid()) {
            LogManager.getLogger().warn("PlayerSkinRequestMessage was invalid: {}", message);
            return;
        }

        // we know for sure that this handler is only used on the server side, so it is ok to assume
        //  that the ctx handler is a serverhandler, and that ServerPlayerEntity exists
        // Packets received on the client side must be handled differently!  See MessageHandlerOnClient

        final ServerPlayer sendingPlayer = (ServerPlayer) ctx.getPlayer();
        if (sendingPlayer == null) {
            LogManager.getLogger().warn("EntityPlayer was null when PlayerSkinRequestMessage was received");
        }

        // This code creates a new task which will be executed by the server during the next tick,
        //  In this case, the task is to call messageHandlerOnServer.processMessage(message, sendingPlayer)
        ctx.queue(() -> // make sure it's only executed on the physical client
                processMessage(message, sendingPlayer)
        );
    }

    static void processMessage(PlayerSkinRequestMessage message, ServerPlayer sendingPlayer) {
        SharedPool.execute(() -> {
            GameProfile profile = message.getPlayer().toGameProfile();
            LogManager.getLogger().info("Requested skin for player {}[{}]", profile.getName(), profile.getId());
            if (!SkinProviderController.GetInstance().getPlayerSkin(profile, sendingPlayer)) {
                SkinProviderController.GetInstance().setPlayerSkinByName(profile, message.getPlayer().getName(), false);
            }
        });
    }
}