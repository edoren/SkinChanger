package me.edoren.skin_changer.client;

import com.mojang.authlib.GameProfile;
import me.edoren.skin_changer.client.api.SkinLoaderService;
import me.edoren.skin_changer.common.models.PlayerModel;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("unused")
public class ClientASMHooks {
    public static ResourceLocation getLocationCape(AbstractClientPlayerEntity player, ResourceLocation result) {
        ResourceLocation loc = ClientController.GetInstance().getLocationCape(new PlayerModel(player.getGameProfile()));
        if (loc != null)
            return loc;
        return result;
    }

    public static ResourceLocation getLocationSkin(AbstractClientPlayerEntity player, ResourceLocation result) {
        ResourceLocation loc = ClientController.GetInstance().getLocationSkin(new PlayerModel(player.getGameProfile()));
        if (loc != null)
            return loc;
        return result;
    }

    public static RenderType getRenderTypeSkull(SkullBlock.ISkullType type, GameProfile profile, RenderType result) {
        if (SkullBlock.Types.PLAYER == type && profile != null) {
            ResourceLocation loc = ClientController.GetInstance().getLocationSkin(new PlayerModel(profile));
            if (loc == null)
                SkinLoaderService.GetInstance().requestPlayerSkin(new PlayerModel(profile));
            else
                return RenderType.getEntityTranslucent(loc);
        }
        return result;
    }

    public static ResourceLocation getLocationTabOverlaySkin(GameProfile profile, ResourceLocation result) {
        ResourceLocation loc = ClientController.GetInstance().getLocationSkin(new PlayerModel(profile));
        if (loc != null)
            return loc;
        return result;
    }

    public static String getSkinType(AbstractClientPlayerEntity player, String result) {
        String type = ClientController.GetInstance().getSkinType(new PlayerModel(player.getGameProfile()));
        if (type != null)
            return type;
        return result;
    }
}
