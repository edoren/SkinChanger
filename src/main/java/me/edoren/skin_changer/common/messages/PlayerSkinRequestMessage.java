package me.edoren.skin_changer.common.messages;

import me.edoren.skin_changer.common.models.PlayerModel;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;

public class PlayerSkinRequestMessage {
    private PlayerModel player;
    private boolean messageIsValid;

    public PlayerSkinRequestMessage(PlayerModel player) {
        this.player = player;
        this.messageIsValid = true;
    }

    public PlayerModel getPlayer() {
        return player;
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
    public static PlayerSkinRequestMessage decode(FriendlyByteBuf buf) {
        PlayerSkinRequestMessage ret = new PlayerSkinRequestMessage();
        try {
            int bufferSize;
            bufferSize = buf.readInt();
            String name = buf.readUtf(bufferSize);
            bufferSize = buf.readInt();
            String uuid = buf.readUtf(bufferSize);
            ret.player = new PlayerModel(name, uuid);
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
    public void encode(FriendlyByteBuf buf) {
        if (!messageIsValid) return;
        buf.writeInt(player.getName().length());
        buf.writeUtf(player.getName());
        buf.writeInt(player.getId().length());
        buf.writeUtf(player.getId());
    }
}