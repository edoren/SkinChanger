package me.edoren.skin_changer.client.mixin;

import me.edoren.skin_changer.client.ClientController;
import me.edoren.skin_changer.common.models.PlayerModel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void injected(CallbackInfoReturnable<PlayerSkin> cir) {
        PlayerInfo player = (PlayerInfo) (Object) this;
        PlayerSkin result = cir.getReturnValue();
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
            cir.setReturnValue(new PlayerSkin(skin, null, cape, null, type, false));
        } else {
            cir.setReturnValue(result);
        }
    }
}
