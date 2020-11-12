package me.edoren.skin_changer.client;

import me.edoren.skin_changer.client.api.ISkinTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class CustomSkinTexture extends Texture implements ISkinTexture {
    private final ResourceLocation location;
    private final WeakReference<ByteBuffer> data;

    public CustomSkinTexture(ResourceLocation location, ByteBuffer data) {
        if (data == null)
            throw new IllegalArgumentException("buffer must not be null");

        this.location = location;
        this.data = new WeakReference<>(data);
    }

    @Override
    public ByteBuffer getData() {
        return data.get();
    }

    public ResourceLocation getLocation() {
        return location;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void loadTexture(IResourceManager manager) throws IOException {
        deleteGlTexture();

        ByteBuffer buf;
        if ((buf = data.get()) == null) // gc
            throw new FileNotFoundException(getLocation().toString());

        try (NativeImage image = NativeImage.read(buf)) {
            synchronized (this) {
                TextureUtil.prepareImage(getGlTextureId(), 0, image.getWidth(), image.getHeight());
                image.uploadTextureSub(0, 0, 0, false);
            }
        }
    }
}
