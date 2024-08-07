package me.edoren.skin_changer.client;

import me.edoren.skin_changer.client.api.SkinLoaderService;
import me.edoren.skin_changer.common.models.PlayerModel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;


@SuppressWarnings("unused")
public class ClientASMHooks {
    public static PlayerSkin getSkin(PlayerInfo player, PlayerSkin result) {
        PlayerModel playerModel = new PlayerModel(player.getProfile());
        ResourceLocation skin = ClientController.GetInstance().getLocationSkin(playerModel);
        ResourceLocation cape = ClientController.GetInstance().getLocationCape(playerModel);
        if (skin != null || cape != null) {
            if (skin == null) {
                skin = result.texture();
            }
            if (cape == null) {
                cape = result.capeTexture();
            }
            PlayerSkin.Model type = ClientController.GetInstance().getSkinType(playerModel);
            if (type == null) {
                type = result.model();
            }
            return new PlayerSkin(skin, null, cape, null, type, false);
        } else {
            return result;
        }
    }

    public static ResourceLocation getRenderTypeSkull(SkullBlock.Type type, ResolvableProfile profile, ResourceLocation result) {
        if (type == SkullBlock.Types.PLAYER && profile != null) {
            ResourceLocation loc = ClientController.GetInstance().getLocationSkin(new PlayerModel(profile.gameProfile()));
            if (loc == null)
                SkinLoaderService.GetInstance().requestPlayerSkin(new PlayerModel(profile.gameProfile()));
            else
                return loc;
        }
        return result;
    }
}
