package me.edoren.skin_changer.common;

import me.edoren.skin_changer.client.ClientMessageHandler;
import me.edoren.skin_changer.common.messages.PlayerSkinRequestMessage;
import me.edoren.skin_changer.common.messages.PlayerSkinUpdateMessage;
import me.edoren.skin_changer.server.ServerMessageHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
        simpleChannel = NetworkRegistry.newSimpleChannel(simpleChannelRL, () -> MESSAGE_PROTOCOL_VERSION,
                ClientMessageHandler::isThisProtocolAcceptedByClient,
                ServerMessageHandler::isThisProtocolAcceptedByServer);

        simpleChannel.registerMessage(PLAYER_SKIN_UPDATE_MESSAGE_ID, PlayerSkinUpdateMessage.class,
                PlayerSkinUpdateMessage::encode, PlayerSkinUpdateMessage::decode,
                ClientMessageHandler::onMessageReceived);

        simpleChannel.registerMessage(PLAYER_SKIN_REQUEST_MESSAGE_ID, PlayerSkinRequestMessage.class,
                PlayerSkinRequestMessage::encode, PlayerSkinRequestMessage::decode,
                ServerMessageHandler::onMessageReceived);
    }

    public SimpleChannel getSimpleChannel() {
        return simpleChannel;
    }
}
