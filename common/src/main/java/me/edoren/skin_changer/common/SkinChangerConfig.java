package me.edoren.skin_changer.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class SkinChangerConfig {
    public boolean showChatMessages = true;

    private static SkinChangerConfig instance;

    public static SkinChangerConfig get() {
        if (instance == null) instance = new SkinChangerConfig();
        return instance;
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve(Constants.MOD_ID + ".json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                SkinChangerConfig parsed = gson.fromJson(r, SkinChangerConfig.class);
                instance = parsed != null ? parsed : new SkinChangerConfig();
            } catch (IOException e) {
                LogManager.getLogger().warn("Failed to read {}, using defaults", file, e);
                instance = new SkinChangerConfig();
            }
        } else {
            instance = new SkinChangerConfig();
        }

        try (Writer w = Files.newBufferedWriter(file)) {
            gson.toJson(instance, w);
        } catch (IOException e) {
            LogManager.getLogger().warn("Failed to write {}", file, e);
        }
    }
}
