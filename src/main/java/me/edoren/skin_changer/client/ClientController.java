package me.edoren.skin_changer.client;

import me.edoren.skin_changer.client.api.ISkin;
import me.edoren.skin_changer.client.api.SkinLoaderService;
import me.edoren.skin_changer.common.Constants;
import me.edoren.skin_changer.common.models.PlayerModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class ClientController {
    private static ClientController singleInstance = null;

    final Map<ByteBuffer, CustomSkinTexture> textures = new WeakHashMap<>();

    private ClientController() {
    }

    public static ClientController GetInstance() {
        if (singleInstance == null)
            singleInstance = new ClientController();

        return singleInstance;
    }

    public void initialize() {
        MinecraftForge.EVENT_BUS.unregister(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public ResourceLocation getLocationCape(PlayerModel model) {
        ISkin cape = SkinLoaderService.GetInstance().getCape(model);
        if (cape != null && cape.isDataReady())
            return getOrCreateTexture(cape.getData(), cape).getLocation();
        return null;
    }

    public ResourceLocation getLocationSkin(PlayerModel model) {
        ISkin skin = SkinLoaderService.GetInstance().getSkin(model);
        if (skin != null && skin.isDataReady())
            return getOrCreateTexture(skin.getData(), skin).getLocation();
        return null;
    }

    public PlayerSkin.Model getSkinType(PlayerModel model) {
        ResourceLocation location = getLocationSkin(model);
        if (location != null) {
            ISkin skin = SkinLoaderService.GetInstance().getSkin(model);
            if (skin != null && skin.isDataReady())
                return skin.getSkinType();
        }
        return null;
    }

    public CustomSkinTexture getOrCreateTexture(ByteBuffer data, ISkin skin) {
        if (!textures.containsKey(data)) {
            CustomSkinTexture texture = new CustomSkinTexture(generateRandomLocation(), data);
            Minecraft.getInstance().getTextureManager().register(texture.getLocation(), texture);
            textures.put(data, texture);

            if (skin != null) {
                skin.setRemovalListener(s -> {
                    if (data == s.getData()) {
                        Minecraft.getInstance().execute(() -> {
                            Minecraft.getInstance().getTextureManager().release(texture.getLocation());
                            textures.remove(data);
                        });
                    }
                });
            }
        }
        return textures.get(data);
    }

    @SubscribeEvent
    public void onClientTickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ClientLevel world = Minecraft.getInstance().level;
            if (world != null) {
                for (AbstractClientPlayer player : world.players()) {
                    PlayerModel model = new PlayerModel(player.getGameProfile());
                    SkinLoaderService.GetInstance().getSkin(model);
                    SkinLoaderService.GetInstance().getCape(model);
                }
            }
        }
    }

    private ResourceLocation generateRandomLocation() {
        return new ResourceLocation(Constants.MOD_ID, String.format("textures/generated/%s", UUID.randomUUID()));
    }
}
