package me.edoren.skin_changer.common;

import dev.architectury.networking.NetworkChannel;
import me.edoren.skin_changer.client.ClientMessageHandler;
import me.edoren.skin_changer.common.messages.PlayerSkinRequestMessage;
import me.edoren.skin_changer.common.messages.PlayerSkinUpdateMessage;
import me.edoren.skin_changer.server.ServerMessageHandler;
import net.minecraft.resources.ResourceLocation;

public class NetworkContext {
    public static final int MESSAGE_PROTOCOL_VERSION = 1;
    public static final ResourceLocation simpleChannelRL = new ResourceLocation(Constants.MOD_ID, "mbechannel");
    public static final byte PLAYER_SKIN_UPDATE_MESSAGE_ID = 97;
    public static final byte PLAYER_SKIN_REQUEST_MESSAGE_ID = 98;
    private static NetworkContext singleInstance = null;
    NetworkChannel simpleChannel = null;

    private NetworkContext() {
    }

    public static NetworkContext GetInstance() {
        if (singleInstance == null)
            singleInstance = new NetworkContext();

        return singleInstance;
    }

    public void initialize() {
        this.simpleChannel = NetworkChannel.create(simpleChannelRL);

        this.simpleChannel.register(PlayerSkinUpdateMessage.class,
                PlayerSkinUpdateMessage::encode,
                PlayerSkinUpdateMessage::decode,
                ClientMessageHandler::onMessageReceived);

        this.simpleChannel.register(PlayerSkinRequestMessage.class,
                PlayerSkinRequestMessage::encode,
                PlayerSkinRequestMessage::decode,
                ServerMessageHandler::onMessageReceived);
    }

    public NetworkChannel getSimpleChannel() {
        return simpleChannel;
    }
}
