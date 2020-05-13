package me.edoren.skin_changer.common.models;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

public class PlayerModel {
    private final String name; // Player Name
    private final String uuid; // Player UUID

    public PlayerModel(GameProfile profile) {
        this.name = profile.getName();
        this.uuid = profile.getId().toString();
    }

    public PlayerModel(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return uuid;
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || this.getClass() != obj.getClass())
            return false;
        PlayerModel other = (PlayerModel) obj;
        return this.uuid.toLowerCase().equals(other.uuid.toLowerCase()) || this.name.toLowerCase().equals(other.name.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", name, uuid);
    }

    public GameProfile toGameProfile() {
        return new GameProfile(UUID.fromString(uuid), name);
    }
}
