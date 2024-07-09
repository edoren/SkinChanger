package me.edoren.skin_changer.common;

import me.edoren.skin_changer.client.ClientMessageHandler;
import me.edoren.skin_changer.common.messages.PlayerSkinRequestMessage;
import me.edoren.skin_changer.common.messages.PlayerSkinUpdateMessage;
import me.edoren.skin_changer.server.ServerMessageHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public class NetworkContext {
    public static final int MESSAGE_PROTOCOL_VERSION = 1;
    public static final ResourceLocation simpleChannelRL = new ResourceLocation(Constants.MOD_ID, "mbechannel");
    public static final byte PLAYER_SKIN_UPDATE_MESSAGE_ID = 97;
    public static final byte PLAYER_SKIN_REQUEST_MESSAGE_ID = 98;
    private static NetworkContext singleInstance = null;
    SimpleChannel simpleChannel;

    private NetworkContext() {
        simpleChannel = ChannelBuilder.named(simpleChannelRL)
                .networkProtocolVersion(MESSAGE_PROTOCOL_VERSION)
                .clientAcceptedVersions(ClientMessageHandler::isThisProtocolAcceptedByClient)
                .serverAcceptedVersions(ServerMessageHandler::isThisProtocolAcceptedByServer).simpleChannel();

        simpleChannel.messageBuilder(PlayerSkinUpdateMessage.class, PLAYER_SKIN_UPDATE_MESSAGE_ID, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PlayerSkinUpdateMessage::encode)
                .decoder(PlayerSkinUpdateMessage::decode)
                .consumerMainThread(ClientMessageHandler::onMessageReceived)
                .add();

        simpleChannel.messageBuilder(PlayerSkinRequestMessage.class, PLAYER_SKIN_REQUEST_MESSAGE_ID, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PlayerSkinRequestMessage::encode)
                .decoder(PlayerSkinRequestMessage::decode)
                .consumerMainThread(ServerMessageHandler::onMessageReceived)
                .add();
    }

    public static NetworkContext GetInstance() {
        if (singleInstance == null)
            singleInstance = new NetworkContext();

        return singleInstance;
    }

    public SimpleChannel getSimpleChannel() {
        return simpleChannel;
    }
}
