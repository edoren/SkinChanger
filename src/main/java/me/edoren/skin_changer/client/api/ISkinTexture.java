package me.edoren.skin_changer.client.api;

import java.nio.ByteBuffer;

public interface ISkinTexture {

    /**
     * @return the ByteBuffer for this ISkinTexture. may be null.
     */
    ByteBuffer getData();

}
