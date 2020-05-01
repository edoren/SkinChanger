package me.edoren.skin_changer.common;

import com.google.gson.Gson;
import me.edoren.skin_changer.common.messages.PlayerDBModel;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class NetworkUtils {
    public static CompletableFuture<byte[]> downloadFileAsync(String resource, Proxy proxy, int maxRetries) {
        try {
            URL url = new URL(resource);
            return downloadFileAsync(url, proxy, maxRetries);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
    }

    public static CompletableFuture<byte[]> downloadFileAsync(URL url, Proxy proxy, int maxRetries) {
        return CompletableFuture.supplyAsync(() -> downloadFile(url, proxy, maxRetries), SharedPool.get());
    }

    public static byte[] downloadFile(String resource, Proxy proxy, int maxRetries) {
        try {
            URL url = new URL(resource);
            return downloadFile(url, proxy, maxRetries);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] downloadFile(URL url, Proxy proxy, int maxRetries) {
        LogManager.getLogger().info("Downloading file {}", url);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (int i = 0; i < maxRetries; i++) {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) (proxy == null ? url.openConnection() : url.openConnection(proxy));
                urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/75.0");
                urlConnection.setReadTimeout(5000);
                urlConnection.setConnectTimeout(5000);
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());

                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    stream.write(dataBuffer, 0, bytesRead);
                }

                LogManager.getLogger().info("File {} downloaded", url);
                return stream.toByteArray();
            } catch (FileNotFoundException ignored) {
                return null;
            } catch (IOException ignored) {
            }
        }
        LogManager.getLogger().info("Error downloading file {}", url);
        return null;
    }

    public static String getPlayerUUID(String name) {
        byte[] jsonData = NetworkUtils.downloadFile(String.format("https://playerdb.co/api/player/minecraft/%s", name), null, 2);
        if (jsonData == null) return null;
        String jsonStr = new String(jsonData);
        Gson gson = new Gson();
        PlayerDBModel playerDBModel = gson.fromJson(jsonStr, PlayerDBModel.class);
        if (!playerDBModel.success) {
            return null;
        }
        return playerDBModel.data.player.id;
    }
}
