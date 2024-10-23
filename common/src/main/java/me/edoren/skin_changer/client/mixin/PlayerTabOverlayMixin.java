package me.edoren.skin_changer.client.mixin;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 0, name = "bl")
    private boolean inject(boolean x) {
        return true;
    }
}
