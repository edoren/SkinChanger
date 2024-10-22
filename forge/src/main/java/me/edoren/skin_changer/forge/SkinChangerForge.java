package me.edoren.skin_changer.forge;

import dev.architectury.platform.forge.EventBuses;
import me.edoren.skin_changer.SkinChanger;
import me.edoren.skin_changer.common.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class SkinChangerForge {
    SkinChanger instance;

    @SuppressWarnings("unused")
    public SkinChangerForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Constants.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        this.instance = new SkinChanger();
    }
}
