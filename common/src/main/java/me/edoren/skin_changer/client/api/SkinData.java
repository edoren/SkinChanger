package me.edoren.skin_changer.client.api;

import net.minecraft.client.resources.PlayerSkin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class SkinData implements ISkin {
    private final Collection<Consumer<ISkin>> listeners = new CopyOnWriteArrayList<>();
    private final Collection<Function<ByteBuffer, ByteBuffer>> filters = new CopyOnWriteArrayList<>();
    private ByteBuffer data;
    private PlayerSkin.Model type;

    public static ByteBuffer toBuffer(byte[] data) {
        ByteBuffer buf = ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder());
        buf.put(data);
        buf.rewind();
        return buf;
    }

    @Override
    public ByteBuffer getData() {
        return data;
    }

    @Override
    public PlayerSkin.Model getSkinType() {
        return type;
    }

    @Override
    public boolean isDataReady() {
        return data != null;
    }

    @Override
    public synchronized void onRemoval() {
        for (Consumer<ISkin> listener : listeners)
            listener.accept(this);

        data = null;
        type = null;
    }

    public synchronized void put(byte[] data, PlayerSkin.Model type) {
        ByteBuffer buf = null;
        if (data != null) {
            buf = toBuffer(data);
            for (Function<ByteBuffer, ByteBuffer> filter : filters)
                if ((buf = filter.apply(buf)) == null)
                    break;
        }

        this.data = buf;
        this.type = type;
    }

    @Override
    public boolean setRemovalListener(Consumer<ISkin> listener) {
        if (listener == null || listeners.contains(listener))
            return false;
        return listeners.add(listener);
    }

    @Override
    public boolean setSkinFilter(Function<ByteBuffer, ByteBuffer> filter) {
        if (filter == null || filters.contains(filter))
            return false;
        return filters.add(filter);
    }

}
