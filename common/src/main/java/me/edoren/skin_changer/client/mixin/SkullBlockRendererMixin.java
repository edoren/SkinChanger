package me.edoren.skin_changer.client.mixin;

import me.edoren.skin_changer.client.ClientController;
import me.edoren.skin_changer.client.api.SkinLoaderService;
import me.edoren.skin_changer.common.models.PlayerModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(SkullBlockRenderer.class)
public class SkullBlockRendererMixin {
    @Inject(method = "getRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;entityTranslucent(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;", shift = At.Shift.AFTER), cancellable = true)
    private static void entityTranslucentInjected(SkullBlock.Type type, ResolvableProfile resolvableProfile, CallbackInfoReturnable<RenderType> cir) {
        ResourceLocation loc = ClientController.GetInstance().getLocationSkin(new PlayerModel(resolvableProfile.gameProfile()));
        if (loc != null) {
            cir.setReturnValue(RenderType.entityTranslucent(loc));
        } else {
            SkinLoaderService.GetInstance().requestPlayerSkin(new PlayerModel(resolvableProfile.gameProfile()));
        }
    }
}
