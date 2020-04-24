package me.edoren.skin_changer.common;

import me.edoren.skin_changer.client.ClientController;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("unused")
public class Hooks {
    public static ResourceLocation getLocationCape(AbstractClientPlayerEntity player, ResourceLocation result) {
        ResourceLocation loc = ClientController.GetInstance().getLocationCape(player.getGameProfile());
        if (loc != null)
            return loc;
        return result;
    }

    public static ResourceLocation getLocationSkin(AbstractClientPlayerEntity player, ResourceLocation result) {
        ResourceLocation loc = ClientController.GetInstance().getLocationSkin(player.getGameProfile());
        if (loc != null)
            return loc;
        return result;
    }

    // public static ResourceLocation getLocationSkin_SkullRenderer(SkullBlock.ISkullType type, GameProfile profile, ResourceLocation result)
    // {
    //     if (SkullBlock.Types.PLAYER == type && profile != null)
    //     {
    //         ResourceLocation loc = Proxy.INSTANCE.getLocationSkin(profile);
    //         if (loc != null)
    //             return loc;
    //     }
    //     return result;
    // }

    // public static ResourceLocation getLocationSkin_TabOverlay(GameProfile profile, ResourceLocation result)
    // {
    //     ResourceLocation loc = Proxy.INSTANCE.getLocationSkin(profile);
    //     if (loc != null)
    //         return loc;
    //     return result;
    // }

    public static String getSkinType(AbstractClientPlayerEntity player, String result) {
        String type = ClientController.GetInstance().getSkinType(player.getGameProfile());
        if (type != null)
            return type;
        return result;
    }

}
