package me.edoren.skin_changer.client;

import me.edoren.skin_changer.client.api.SkinLoaderService;
import me.edoren.skin_changer.common.NetworkContext;
import me.edoren.skin_changer.common.messages.PlayerSkinUpdateMessage;
import me.edoren.skin_changer.common.models.PlayerSkinModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;

import java.util.Optional;
import java.util.Vector;
import java.util.function.Supplier;

public class ClientMessageHandler {

    /**
     * Called when a message is received of the appropriate type.
     * CALLED BY THE NETWORK THREAD, NOT THE CLIENT THREAD
     */
    public static void onMessageReceived(final PlayerSkinUpdateMessage message, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);

        if (sideReceived != LogicalSide.CLIENT) {
            LogManager.getLogger().warn("PlayerSkinUpdateMessage received on wrong side:" + ctx.getDirection().getReceptionSide());
            return;
        }

        if (!message.isMessageValid()) {
            LogManager.getLogger().warn("PlayerSkinUpdateMessage was invalid" + message.toString());
            return;
        }

        // We know for sure that this handler is only used on the client side, so it is ok to assume
        // that the ctx handler is a client, and that Minecraft exists.
        Optional<ClientWorld> clientWorld = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
        if (!clientWorld.isPresent()) {
            LogManager.getLogger().warn("PlayerSkinUpdateMessage context could not provide a ClientWorld.");
            return;
        }

        // This code creates a new task which will be executed by the client during the next tick
        // In this case, the task is to call messageHandlerOnClient.processMessage(worldclient, message)
        ctx.enqueueWork(() -> processMessage(message));
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

    public static boolean isThisProtocolAcceptedByClient(String protocolVersion) {
        return NetworkContext.MESSAGE_PROTOCOL_VERSION.equals(protocolVersion);
    }
}