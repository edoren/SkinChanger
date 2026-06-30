package me.edoren.skin_changer.server.providers;

import me.edoren.skin_changer.common.NetworkUtils;

public class CrafatarSkinProvider implements ISkinProvider {
    private static final String[] MIRRORS = {
        "https://skins.manacube.com/skins/%s",
        "https://crafatar.imthespyke.fr/skins/%s",
        "https://crafatar-pub.neodium.fr/skins/%s"
    };

    @Override
    public byte[] getSkin(String playerName) {
        String uuid = NetworkUtils.getPlayerUUID(playerName);
        if (uuid == null) return null;
        for (String mirror : MIRRORS) {
            byte[] data = NetworkUtils.downloadFile(String.format(mirror, uuid), null, 1);
            if (data != null) return data;
        }
        return null;
    }
}
