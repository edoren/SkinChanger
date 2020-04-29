package me.edoren.skin_changer.common.messages;

import me.edoren.skin_changer.common.models.PlayerSkinData;
import net.minecraft.network.PacketBuffer;
import org.apache.logging.log4j.LogManager;

import java.util.Vector;

public class PlayerSkinUpdateMessage {
    private Vector<PlayerSkinData> playerSkinData;
    private boolean messageIsValid;

    public PlayerSkinUpdateMessage(Vector<PlayerSkinData> playerSkinData) {
        this.playerSkinData = playerSkinData;
        this.messageIsValid = true;
    }

    public Vector<PlayerSkinData> getAllSkinData() {
        return playerSkinData;
    }

    public boolean isMessageValid() {
        return messageIsValid;
    }

    // for use by the message handler only.
    PlayerSkinUpdateMessage() {
        messageIsValid = false;
    }

    /**
     * Called by the network code once it has received the message bytes over the network.
     * Used to read the ByteBuf contents into your member variables
     */
    public static PlayerSkinUpdateMessage decode(PacketBuffer buf) {
        PlayerSkinUpdateMessage ret = new PlayerSkinUpdateMessage();
        try {
            int size = buf.readInt();
            ret.playerSkinData = new Vector<>();
            ret.playerSkinData.ensureCapacity(size);
            for (int i = 0; i != size; i++) {
                int bufferSize;

                bufferSize = buf.readInt();
                String uuid = buf.readString(bufferSize);

                byte[] skin = null;
                bufferSize = buf.readInt();
                if (bufferSize > 0) {
                    skin = new byte[bufferSize];
                    buf.readBytes(skin);
                }

                byte[] cape = null;
                bufferSize = buf.readInt();
                if (bufferSize > 0) {
                    cape = new byte[bufferSize];
                    buf.readBytes(cape);
                }

                ret.playerSkinData.add(new PlayerSkinData(uuid, skin, cape));
            }
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
        buf.writeInt(playerSkinData.size());
        for (int i = 0; i != playerSkinData.size(); i++) {
            int bufferSize;

            PlayerSkinData player = playerSkinData.get(i);

            buf.writeInt(player.getUUID().length());
            buf.writeString(player.getUUID());

            bufferSize = player.getSkin() != null ? player.getSkin().length : 0;
            buf.writeInt(bufferSize);
            if (bufferSize > 0)
                buf.writeBytes(player.getSkin());

            bufferSize = player.getCape() != null ? player.getCape().length : 0;
            buf.writeInt(bufferSize);
            if (bufferSize > 0)
                buf.writeBytes(player.getCape());
        }
    }
}