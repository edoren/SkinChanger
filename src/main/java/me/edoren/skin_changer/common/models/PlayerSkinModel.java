package me.edoren.skin_changer.common.models;

public class PlayerSkinModel {
    private final PlayerModel player; // Player
    private final byte[] skin; // Skin PNG Data
    private final byte[] cape; // Skin PNG Data

    public PlayerSkinModel(PlayerModel uuid, byte[] data, byte[] cape) {
        this.player = uuid;
        this.skin = data;
        this.cape = cape;
    }

    public PlayerModel getPlayer() {
        return player;
    }

    public byte[] getSkin() {
        return skin;
    }

    public byte[] getCape() {
        return cape;
    }
}
