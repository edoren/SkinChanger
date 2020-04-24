package me.edoren.skin_changer.common.models;

public class PlayerSkinData {
    private final String uuid; // Player UUID
    private final byte[] skin; // Skin PNG Data
    private final byte[] cape; // Skin PNG Data

    public PlayerSkinData(String uuid, byte[] data, byte[] cape) {
        this.uuid = uuid;
        this.skin = data;
        this.cape = cape;
    }

    public String getUUID() {
        return uuid;
    }

    public byte[] getSkin() {
        return skin;
    }

    public byte[] getCape() {
        return cape;
    }
}
