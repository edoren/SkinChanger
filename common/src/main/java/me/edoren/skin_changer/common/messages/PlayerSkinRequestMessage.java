package me.edoren.skin_changer.common.messages;

import dev.architectury.networking.NetworkManager;
import me.edoren.skin_changer.common.Constants;
import me.edoren.skin_changer.common.models.PlayerModel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PlayerSkinRequestMessage implements CustomPacketPayload {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "player_skin_request_message");
    private static final CustomPacketPayload.Type<PlayerSkinRequestMessage> TYPE = new CustomPacketPayload.Type<>(ID);
    private static final StreamCodec<RegistryFriendlyByteBuf, PlayerSkinRequestMessage> CODEC = CustomPacketPayload.codec(PlayerSkinRequestMessage::encode, PlayerSkinRequestMessage::new);

    private final PlayerModel player;

    public static void register(BiConsumer<PlayerSkinRequestMessage, Supplier<NetworkManager.PacketContext>> messageConsumer) {
        NetworkManager.NetworkReceiver<PlayerSkinRequestMessage> receiver = (packet, context) -> {
            messageConsumer.accept(packet, () -> {
                return context;
            });
        };
        NetworkManager.registerReceiver(NetworkManager.c2s(), TYPE, CODEC, receiver);
    }

    public PlayerSkinRequestMessage(PlayerModel player) {
        this.player = player;
    }

    public PlayerSkinRequestMessage(RegistryFriendlyByteBuf buf) {
        PlayerModel player;
        try {
            int bufferSize;
            bufferSize = buf.readInt();
            String name = buf.readUtf(bufferSize);
            bufferSize = buf.readInt();
            String uuid = buf.readUtf(bufferSize);
            player = new PlayerModel(name, uuid);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            LogManager.getLogger().error("Exception while reading PlayerSkinRequestMessage", e);
            player = null;
        }

        this.player = player;
    }

    public PlayerModel getPlayer() {
        return player;
    }

    public boolean isMessageValid() {
        return this.player != null;
    }

    /**
     * Called by the network code.
     * Used to write the contents of your message member variables into the ByteBuf, ready for transmission over the network.
     */
    public void encode(FriendlyByteBuf buf) {
        if (this.player == null) return;
        buf.writeInt(player.getName().length());
        buf.writeUtf(player.getName());
        buf.writeInt(player.getId().length());
        buf.writeUtf(player.getId());
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
