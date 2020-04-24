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
        PlayerSkinUpdateMessage retval = new PlayerSkinUpdateMessage();
        try {
            int size = buf.readInt();
            retval.playerSkinData = new Vector<>();
            retval.playerSkinData.ensureCapacity(size);
            for (int i = 0; i != size; i++) {
                int bufferSize;

                String uuid = buf.readString();

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

                retval.playerSkinData.add(new PlayerSkinData(uuid, skin, cape));
            }

            // these methods may also be of use for your code:
            // for Itemstacks - ByteBufUtils.readItemStack()
            // for NBT tags ByteBufUtils.readTag();
            // for Strings: ByteBufUtils.readUTF8String();
            // NB that PacketBuffer is a derived class of ByteBuf

        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            LogManager.getLogger().warn("Exception while reading PlayerSkinUpdateMessage: " + e);
            e.printStackTrace();
            return retval;
        }
        retval.messageIsValid = true;
        return retval;
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

    @Override
    public String toString() {
        return "TargetEffectMessageToClient[targetCoordinates=" + playerSkinData + "]";
    }
}