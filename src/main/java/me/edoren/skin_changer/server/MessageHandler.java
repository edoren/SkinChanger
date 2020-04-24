package me.edoren.skin_changer.server;

import me.edoren.skin_changer.common.NetworkContext;

public class MessageHandler {
    public static boolean isThisProtocolAcceptedByServer(String protocolVersion) {
        return NetworkContext.MESSAGE_PROTOCOL_VERSION.equals(protocolVersion);
    }
}