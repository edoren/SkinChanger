package me.edoren.skin_changer.common.models;

import com.mojang.authlib.GameProfile;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class PlayerModel {
    private final String name; // Player Name
    private final String uuid; // Player UUID

    public PlayerModel(GameProfile profile) {
        String name = profile.getName();
        UUID id = profile.getId();
        this.name = name;
        this.uuid = id != null ? id.toString() : null;
    }

    public PlayerModel(String name, String uuid) {
        if (uuid == null && StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Name and UUID cannot both be blank");
        }
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
        if (name != null) {
            return name.toLowerCase().hashCode();
        } else {
            return uuid.toLowerCase().hashCode();
        }
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
