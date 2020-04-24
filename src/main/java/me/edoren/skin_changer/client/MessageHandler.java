package me.edoren.skin_changer.client;

import me.edoren.skin_changer.client.api.SkinLoaderService;
import me.edoren.skin_changer.common.NetworkContext;
import me.edoren.skin_changer.common.messages.PlayerSkinUpdateMessage;
import me.edoren.skin_changer.common.models.PlayerSkinData;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Vector;
import java.util.function.Supplier;

public class MessageHandler {

    /**
     * Called when a message is received of the appropriate type.
     * CALLED BY THE NETWORK THREAD, NOT THE CLIENT THREAD
     */
    public static void onMessageReceived(final PlayerSkinUpdateMessage message, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);

        if (sideReceived != LogicalSide.CLIENT) {
            LOGGER.warn("PlayerSkinUpdateMessage received on wrong side:" + ctx.getDirection().getReceptionSide());
            return;
        }

        if (!message.isMessageValid()) {
            LOGGER.warn("PlayerSkinUpdateMessage was invalid" + message.toString());
            return;
        }

        // We know for sure that this handler is only used on the client side, so it is ok to assume
        // that the ctx handler is a client, and that Minecraft exists.
        Optional<ClientWorld> clientWorld = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
        if (!clientWorld.isPresent()) {
            LOGGER.warn("PlayerSkinUpdateMessage context could not provide a ClientWorld.");
            return;
        }

        // This code creates a new task which will be executed by the client during the next tick
        // In this case, the task is to call messageHandlerOnClient.processMessage(worldclient, message)
        ctx.enqueueWork(() -> processMessage(clientWorld.get(), message));
    }

    private static void processMessage(ClientWorld worldClient, PlayerSkinUpdateMessage message) {
        Vector<PlayerSkinData> skinData = message.getAllSkinData();
        if (skinData.isEmpty()) {
            SkinLoaderService.GetInstance().clear();
            return;
        }

        for (PlayerSkinData playerData : skinData) {
            SkinLoaderService.GetInstance().loadPlayerSkin(playerData.getUUID(), playerData.getSkin());
            SkinLoaderService.GetInstance().loadPlayerCape(playerData.getUUID(), playerData.getCape());
        }
    }

    public static boolean isThisProtocolAcceptedByClient(String protocolVersion) {
        return NetworkContext.MESSAGE_PROTOCOL_VERSION.equals(protocolVersion);
    }

    private static final Logger LOGGER = LogManager.getLogger();
}