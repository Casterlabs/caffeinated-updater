package co.casterlabs.caffeinated.updater.util;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;

import lombok.NonNull;

public class WebUtil {
    private static final HttpClient client = HttpClient.newHttpClient();

    public static <T> HttpResponse<T> sendRawHttpRequest(@NonNull HttpRequest.Builder builder, @NonNull BodyHandler<T> handler) throws IOException, InterruptedException {
        return client.send(builder.build(), handler);
    }

    public static String sendHttpRequest(@NonNull HttpRequest.Builder builder) throws IOException, InterruptedException {
        return sendRawHttpRequest(builder, BodyHandlers.ofString()).body();
    }

    public static byte[] sendHttpRequestBytes(@NonNull HttpRequest.Builder builder) throws IOException, InterruptedException {
        return sendRawHttpRequest(builder, BodyHandlers.ofByteArray()).body();
    }

}
