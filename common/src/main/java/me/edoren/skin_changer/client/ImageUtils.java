package me.edoren.skin_changer.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.resources.PlayerSkin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;

public class ImageUtils {

    public static PlayerSkin.Model judgeSkinType(byte[] data) {
        try (NativeImage image = NativeImage.read(new ByteArrayInputStream(data))) {
            int w = image.getWidth();
            int h = image.getHeight();
            if (w == h * 2)
                return PlayerSkin.Model.WIDE; // it's actually "legacy", but there will always be a filter to convert them into "default".
            if (w == h) {
                int r = Math.max(w / 64, 1);
                if (((image.getPixelRGBA(55 * r, 20 * r) & 0xFF000000) >>> 24) == 0)
                    return PlayerSkin.Model.SLIM;
                return PlayerSkin.Model.WIDE;
            }
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    public static ByteBuffer legacyFilter(ByteBuffer buffer) {
        try (NativeImage input = NativeImage.read(buffer); NativeImage output = new NativeImage(input.getWidth(), input.getWidth(), true)) {
            int r = Math.max(input.getWidth() / 64, 1);
            boolean f = input.getWidth() == input.getHeight() * 2;
            output.copyFrom(input);
            if (f) {
                output.fillRect(0 * r, 32 * r, 64 * r, 32 * r, 0);
                output.copyRect(4 * r, 16 * r, 16 * r, 32 * r, 4 * r, 4 * r, true, false);
                output.copyRect(8 * r, 16 * r, 16 * r, 32 * r, 4 * r, 4 * r, true, false);
                output.copyRect(0 * r, 20 * r, 24 * r, 32 * r, 4 * r, 12 * r, true, false);
                output.copyRect(4 * r, 20 * r, 16 * r, 32 * r, 4 * r, 12 * r, true, false);
                output.copyRect(8 * r, 20 * r, 8 * r, 32 * r, 4 * r, 12 * r, true, false);
                output.copyRect(12 * r, 20 * r, 16 * r, 32 * r, 4 * r, 12 * r, true, false);
                output.copyRect(44 * r, 16 * r, -8 * r, 32 * r, 4 * r, 4 * r, true, false);
                output.copyRect(48 * r, 16 * r, -8 * r, 32 * r, 4 * r, 4 * r, true, false);
                output.copyRect(40 * r, 20 * r, 0 * r, 32 * r, 4 * r, 12 * r, true, false);
                output.copyRect(44 * r, 20 * r, -8 * r, 32 * r, 4 * r, 12 * r, true, false);
                output.copyRect(48 * r, 20 * r, -16 * r, 32 * r, 4 * r, 12 * r, true, false);
                output.copyRect(52 * r, 20 * r, -8 * r, 32 * r, 4 * r, 12 * r, true, false);
            }

            setAreaOpaque(output, 0 * r, 0 * r, 32 * r, 16 * r);
            if (f)
                setAreaTransparent(output, 32 * r, 0 * r, 64 * r, 32 * r);
            setAreaOpaque(output, 0 * r, 16 * r, 64 * r, 32 * r);
            setAreaOpaque(output, 16 * r, 48 * r, 48 * r, 64 * r);

            File tmp = null;
            try {
                output.writeToFile(tmp = Files.createTempFile(null, null).toFile());
                byte[] data = Files.readAllBytes(tmp.toPath());
                ByteBuffer buf = ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder());
                buf.put(data);
                buf.rewind();
                return buf;
            } finally {
                if (tmp != null && tmp.exists() && !tmp.delete())
                    tmp.deleteOnExit();
            }
        } catch (Throwable t) {
            return buffer;
        }
    }

    private static void setAreaOpaque(NativeImage image, int x, int y, int width, int height) {
        for (int i = x; i < width; i++)
            for (int j = y; j < height; j++)
                image.setPixelRGBA(i, j, image.getPixelRGBA(i, j) | 0xFF000000);
    }

    private static void setAreaTransparent(NativeImage image, int x, int y, int width, int height) {
        for (int i = x; i < width; i++)
            for (int j = y; j < height; j++)
                if ((image.getPixelRGBA(i, j) >> 24 & 0xFF) < 128)
                    return;

        for (int l = x; l < width; l++)
            for (int i1 = y; i1 < height; i1++)
                image.setPixelRGBA(l, i1, image.getPixelRGBA(l, i1) & 0xFFFFFF);
    }

    public static boolean isNotValidData(byte[] data) {
        try {
            NativeImage.read(new ByteArrayInputStream(data));
            return false;
        } catch (Throwable t) {
            return true;
        }
    }
}
