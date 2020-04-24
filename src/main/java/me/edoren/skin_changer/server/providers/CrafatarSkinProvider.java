package me.edoren.skin_changer.server.providers;

import me.edoren.skin_changer.common.NetworkUtils;

public class CrafatarSkinProvider implements ISkinProvider {
    @Override
    public byte[] getSkin(String playerName) {
        String uuid = NetworkUtils.getPlayerUUID(playerName);
        if (uuid == null) return null;
        return NetworkUtils.downloadFile(String.format("https://crafatar.com/skins/%s", uuid), null, 2);
    }
}
