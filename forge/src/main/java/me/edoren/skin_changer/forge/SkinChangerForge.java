package me.edoren.skin_changer.forge;

import me.edoren.skin_changer.SkinChanger;
import me.edoren.skin_changer.common.Constants;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class SkinChangerForge {
    SkinChanger instance;

    @SuppressWarnings("unused")
    public SkinChangerForge() {
        this.instance = new SkinChanger();
    }
}
