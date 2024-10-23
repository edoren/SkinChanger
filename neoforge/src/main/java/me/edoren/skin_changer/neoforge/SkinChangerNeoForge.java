package me.edoren.skin_changer.neoforge;

import me.edoren.skin_changer.SkinChanger;
import me.edoren.skin_changer.common.Constants;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class SkinChangerNeoForge {
    SkinChanger instance;

    @SuppressWarnings("unused")
    public SkinChangerNeoForge() {
        this.instance = new SkinChanger();
    }
}
