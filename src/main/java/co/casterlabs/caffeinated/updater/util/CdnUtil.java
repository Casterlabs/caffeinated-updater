package co.casterlabs.caffeinated.updater.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class CdnUtil {
    private static final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private static final String PRIMARY_SERVER = "https://cdn.casterlabs.co";
    private static final String FALLBACK_SERVER = "https://cdn.03011920.xyz";

    private static <T> HttpResponse<T> sendRawHttpRequest(String path, BodyHandler<T> handler) throws IOException {
        try {
            return client.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(PRIMARY_SERVER + path))
                    .build(),
                handler
            );
        } catch (IOException | InterruptedException e1) {
            FastLogger.logStatic(LogLevel.WARNING, "Couldn't connect to primary CDN server, falling back to secondary...");
            try {
                return client.send(
                    HttpRequest.newBuilder()
                        .uri(URI.create(FALLBACK_SERVER + path))
                        .build(),
                    handler
                );
            } catch (IOException | InterruptedException e2) {
                FastLogger.logStatic(LogLevel.SEVERE, "Both primary and fallback CDNs failed!\n%s\n%s", e1, e2);
                throw new IOException("Couldn't connect to CDN (primary & fallback failure)");
            }
        }
    }

    public static String string(String path) throws IOException, InterruptedException {
        return sendRawHttpRequest(path, BodyHandlers.ofString()).body();
    }

    public static byte[] bytes(String path) throws IOException, InterruptedException {
        return sendRawHttpRequest(path, BodyHandlers.ofByteArray()).body();
    }

    public static HttpResponse<InputStream> stream(String path) throws IOException, InterruptedException {
        return sendRawHttpRequest(path, BodyHandlers.ofInputStream());
    }

}
