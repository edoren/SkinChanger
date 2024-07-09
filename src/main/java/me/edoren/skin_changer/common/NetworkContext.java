package me.edoren.skin_changer.common;

import me.edoren.skin_changer.client.ClientMessageHandler;
import me.edoren.skin_changer.common.messages.PlayerSkinRequestMessage;
import me.edoren.skin_changer.common.messages.PlayerSkinUpdateMessage;
import me.edoren.skin_changer.server.ServerMessageHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
//import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.fml.network.NetworkRegistry;
//import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkContext {
    private static NetworkContext singleInstance = null;

    public static final String MESSAGE_PROTOCOL_VERSION = "1.0";
    public static final ResourceLocation simpleChannelRL = new ResourceLocation("skin_changer", "mbechannel");

    public static final byte PLAYER_SKIN_UPDATE_MESSAGE_ID = 97;
    public static final byte PLAYER_SKIN_REQUEST_MESSAGE_ID = 98;

    SimpleChannel simpleChannel;

    public static NetworkContext GetInstance() {
        if (singleInstance == null)
            singleInstance = new NetworkContext();

        return singleInstance;
    }

    private NetworkContext() {
    }

    public void initialize() {
        simpleChannel = NetworkRegistry.ChannelBuilder.named(simpleChannelRL)
                .networkProtocolVersion(() -> MESSAGE_PROTOCOL_VERSION)
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

    public SimpleChannel getSimpleChannel() {
        return simpleChannel;
    }
}
