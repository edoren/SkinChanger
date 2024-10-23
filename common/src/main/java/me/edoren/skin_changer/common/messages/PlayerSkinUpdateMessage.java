package me.edoren.skin_changer.common.messages;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import me.edoren.skin_changer.common.Constants;
import me.edoren.skin_changer.common.models.PlayerModel;
import me.edoren.skin_changer.common.models.PlayerSkinModel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PlayerSkinUpdateMessage implements CustomPacketPayload {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "player_skin_update_message");
    private static final CustomPacketPayload.Type<PlayerSkinUpdateMessage> TYPE = new CustomPacketPayload.Type<>(ID);
    private static final StreamCodec<RegistryFriendlyByteBuf, PlayerSkinUpdateMessage> CODEC = CustomPacketPayload.codec(PlayerSkinUpdateMessage::encode, PlayerSkinUpdateMessage::new);

    private final Vector<PlayerSkinModel> playerSkinData;

    public static void register(BiConsumer<PlayerSkinUpdateMessage, Supplier<NetworkManager.PacketContext>> messageConsumer) {
        if (Platform.getEnvironment() == Env.CLIENT) {
            NetworkManager.NetworkReceiver<PlayerSkinUpdateMessage> receiver = (packet, context) -> {
                messageConsumer.accept(packet, () -> {
                    return context;
                });
            };
            NetworkManager.registerReceiver(NetworkManager.s2c(), TYPE, CODEC, receiver);
        } else {
            NetworkManager.registerS2CPayloadType(TYPE, CODEC);
        }
    }

    public PlayerSkinUpdateMessage(Vector<PlayerSkinModel> playerSkinData) {
        this.playerSkinData = playerSkinData;
    }

    public PlayerSkinUpdateMessage(RegistryFriendlyByteBuf buf) {
        Vector<PlayerSkinModel> playerSkinData = new Vector<>();
        try {
            int size = buf.readInt();
            playerSkinData.ensureCapacity(size);
            for (int i = 0; i != size; i++) {
                int bufferSize;

                bufferSize = buf.readInt();
                String name = buf.readUtf(bufferSize);

                bufferSize = buf.readInt();
                String uuid = buf.readUtf(bufferSize);

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

                playerSkinData.add(new PlayerSkinModel(new PlayerModel(name, uuid), skin, cape));
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            LogManager.getLogger().warn("Exception while reading PlayerSkinUpdateMessage", e);
            playerSkinData = null;
        }

        this.playerSkinData = playerSkinData;
    }

    public Vector<PlayerSkinModel> getAllSkinData() {
        return this.playerSkinData;
    }

    public boolean isMessageValid() {
        return this.playerSkinData != null;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        if (this.playerSkinData == null) return;
        buf.writeInt(this.playerSkinData.size());
        for (int i = 0; i != this.playerSkinData.size(); i++) {
            int bufferSize;

            PlayerSkinModel player = this.playerSkinData.get(i);

            buf.writeInt(player.getPlayer().getName().length());
            buf.writeUtf(player.getPlayer().getName());

            buf.writeInt(player.getPlayer().getId().length());
            buf.writeUtf(player.getPlayer().getId());

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
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
