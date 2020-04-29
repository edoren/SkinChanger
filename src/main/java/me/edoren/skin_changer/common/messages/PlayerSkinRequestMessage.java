package me.edoren.skin_changer.common.messages;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.PacketBuffer;
import org.apache.logging.log4j.LogManager;

import java.util.UUID;

public class PlayerSkinRequestMessage {
    private String playerName;
    private UUID playerUUID;
    private boolean messageIsValid;

    public PlayerSkinRequestMessage(GameProfile profile) {
        this(profile.getName(), profile.getId());
    }

    public PlayerSkinRequestMessage(String playerName, UUID playerUUID) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.messageIsValid = true;
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public boolean isMessageValid() {
        return messageIsValid;
    }

    // for use by the message handler only.
    PlayerSkinRequestMessage() {
        messageIsValid = false;
    }

    /**
     * Called by the network code once it has received the message bytes over the network.
     * Used to read the ByteBuf contents into your member variables
     */
    public static PlayerSkinRequestMessage decode(PacketBuffer buf) {
        PlayerSkinRequestMessage ret = new PlayerSkinRequestMessage();
        try {
            int bufferSize;
            bufferSize = buf.readInt();
            ret.playerName = buf.readString(bufferSize);
            bufferSize = buf.readInt();
            ret.playerUUID = UUID.fromString(buf.readString(bufferSize));
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            LogManager.getLogger().warn("Exception while reading PlayerSkinUpdateMessage: " + e);
            ret.messageIsValid = false;
            e.printStackTrace();
            return ret;
        }
        ret.messageIsValid = true;
        return ret;
    }

    /**
     * Called by the network code.
     * Used to write the contents of your message member variables into the ByteBuf, ready for transmission over the network.
     */
    public void encode(PacketBuffer buf) {
        if (!messageIsValid) return;
        buf.writeInt(playerName.length());
        buf.writeString(playerName);
        buf.writeInt(playerUUID.toString().length());
        buf.writeString(playerUUID.toString());
    }
}