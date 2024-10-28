package co.casterlabs.caffeinated.updater.util;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;

import lombok.NonNull;
import lombok.SneakyThrows;

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

//    public static String escapeHtml(@NonNull String str) {
//        return str
//            .codePoints()
//            .mapToObj(c -> c > 127 || "\"'<>&".indexOf(c) != -1 ? "&#" + c + ";" : new String(Character.toChars(c)))
//            .collect(Collectors.joining());
//    }

    @SneakyThrows
    public static String decodeURIComponent(@NonNull String s) {
        return URLDecoder.decode(s, "UTF-8");
    }

    @SneakyThrows
    public static String encodeURIComponent(@NonNull String s) {
        return URLEncoder.encode(s, "UTF-8")
            .replaceAll("\\+", "%20")
            .replaceAll("\\%21", "!")
            .replaceAll("\\%27", "'")
            .replaceAll("\\%28", "(")
            .replaceAll("\\%29", ")")
            .replaceAll("\\%7E", "~");
    }

}
