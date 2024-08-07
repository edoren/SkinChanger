package me.edoren.skin_changer.client;

import me.edoren.skin_changer.client.api.SkinLoaderService;
import me.edoren.skin_changer.common.NetworkContext;
import me.edoren.skin_changer.common.messages.PlayerSkinUpdateMessage;
import me.edoren.skin_changer.common.models.PlayerSkinModel;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.Channel;
import org.apache.logging.log4j.LogManager;

import java.util.Optional;
import java.util.Vector;

public class ClientMessageHandler {

    /**
     * Called when a message is received of the appropriate type.
     * CALLED BY THE NETWORK THREAD, NOT THE CLIENT THREAD
     */
    public static void onMessageReceived(final PlayerSkinUpdateMessage message, CustomPayloadEvent.Context ctx) {
        boolean isServerSide = ctx.isServerSide();
        if (isServerSide) {
            LogManager.getLogger().warn("PlayerSkinUpdateMessage received on wrong side, expected Client");
            return;
        }

        ctx.setPacketHandled(true);

        if (!message.isMessageValid()) {
            LogManager.getLogger().warn("PlayerSkinUpdateMessage was invalid: {}", message);
            return;
        }

        // We know for sure that this handler is only used on the client side, so it is ok to assume
        // that the ctx handler is a client, and that Minecraft exists.
        Optional<Level> clientWorld = LogicalSidedProvider.CLIENTWORLD.get(LogicalSide.CLIENT);
        if (clientWorld.isEmpty()) {
            LogManager.getLogger().warn("PlayerSkinUpdateMessage context could not provide a Level.");
            return;
        }

        // This code creates a new task which will be executed by the client during the next tick
        ctx.enqueueWork(() -> // make sure it's only executed on the client
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> processMessage(message))
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

    public static boolean isThisProtocolAcceptedByClient(Channel.VersionTest.Status ignoredStatus, int protocolVersion) {
        return NetworkContext.MESSAGE_PROTOCOL_VERSION == protocolVersion;
    }
}