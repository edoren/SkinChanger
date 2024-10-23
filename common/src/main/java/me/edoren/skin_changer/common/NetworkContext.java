package me.edoren.skin_changer.common;

import dev.architectury.networking.NetworkManager;
import me.edoren.skin_changer.client.ClientMessageHandler;
import me.edoren.skin_changer.common.messages.PlayerSkinRequestMessage;
import me.edoren.skin_changer.common.messages.PlayerSkinUpdateMessage;
import me.edoren.skin_changer.server.ServerMessageHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class NetworkContext {
    private static NetworkContext singleInstance = null;

    private NetworkContext() {
    }

    public static NetworkContext GetInstance() {
        if (singleInstance == null)
            singleInstance = new NetworkContext();

        return singleInstance;
    }

    public <T extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, T payload) {
        NetworkManager.sendToPlayer(player, payload);
    }

    public <T extends CustomPacketPayload> void sendToPlayers(Iterable<ServerPlayer> players, T payload) {
        NetworkManager.sendToPlayers(players, payload);
    }

    @Environment(EnvType.CLIENT)
    public <T extends CustomPacketPayload> void sendToServer(T payload) {
        NetworkManager.sendToServer(payload);
    }

    public void initialize() {
        PlayerSkinUpdateMessage.register(ClientMessageHandler::onMessageReceived);
        PlayerSkinRequestMessage.register(ServerMessageHandler::onMessageReceived);

    }
}
