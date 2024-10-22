package me.edoren.skin_changer.fabric;

import me.edoren.skin_changer.SkinChanger;
import net.fabricmc.api.ModInitializer;

public final class SkinChangerFabric implements ModInitializer {
    SkinChanger instance;

    @Override
    public void onInitialize() {
        this.instance = new SkinChanger();
    }
}
