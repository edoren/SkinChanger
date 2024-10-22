package me.edoren.skin_changer.client.mixin;

import me.edoren.skin_changer.client.ClientController;
import me.edoren.skin_changer.common.models.PlayerModel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {
    @Inject(method = "getSkinLocation", at = @At("RETURN"), cancellable = true)
    private void injectedSkinLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        PlayerInfo player = (PlayerInfo) (Object) this;
        PlayerModel playerModel = new PlayerModel(player.getProfile());
        ResourceLocation loc = ClientController.GetInstance().getLocationSkin(playerModel);
        if (loc != null) {
            cir.setReturnValue(loc);
        }
    }

    @Inject(method = "getCapeLocation", at = @At("RETURN"), cancellable = true)
    private void injectedCapeLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        PlayerInfo player = (PlayerInfo) (Object) this;
        PlayerModel playerModel = new PlayerModel(player.getProfile());
        ResourceLocation loc = ClientController.GetInstance().getLocationCape(playerModel);
        if (loc != null) {
            cir.setReturnValue(loc);
        }
    }

    @Inject(method = "getModelName", at = @At("RETURN"), cancellable = true)
    private void injectedModelName(CallbackInfoReturnable<String> cir) {
        PlayerInfo player = (PlayerInfo) (Object) this;
        PlayerModel playerModel = new PlayerModel(player.getProfile());
        String type = ClientController.GetInstance().getSkinType(playerModel);
        if (type != null) {
            cir.setReturnValue(type);
        }
    }
}
