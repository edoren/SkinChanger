package me.edoren.skin_changer.client;

import com.mojang.authlib.GameProfile;
import me.edoren.skin_changer.client.api.SkinLoaderService;
import me.edoren.skin_changer.common.models.PlayerModel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SkullBlock;


@SuppressWarnings("unused")
public class ClientASMHooks {
    public static ResourceLocation getSkinTextureLocation(PlayerInfo player, ResourceLocation result) {
        ResourceLocation loc = ClientController.GetInstance().getLocationSkin(new PlayerModel(player.getProfile()));
        if (loc != null)
            return loc;
        return result;
    }

    public static ResourceLocation getCloakTextureLocation(PlayerInfo player, ResourceLocation result) {
        ResourceLocation loc = ClientController.GetInstance().getLocationCape(new PlayerModel(player.getProfile()));
        if (loc != null)
            return loc;
        return result;
    }

    public static String getModelName(PlayerInfo player, String result) {
        String type = ClientController.GetInstance().getSkinType(new PlayerModel(player.getProfile()));
        if (type != null)
            return type;
        return result;
    }

    public static ResourceLocation getRenderTypeSkull(SkullBlock.Type type, GameProfile profile, ResourceLocation result) {
        if (type == SkullBlock.Types.PLAYER && profile != null) {
            ResourceLocation loc = ClientController.GetInstance().getLocationSkin(new PlayerModel(profile));
            if (loc == null)
                SkinLoaderService.GetInstance().requestPlayerSkin(new PlayerModel(profile));
            else
                return loc;
        }
        return result;
    }
}
