package co.casterlabs.caffeinated.updater.window;

import java.awt.Image;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@SuppressWarnings("unchecked")
class Streamers {

    // @formatter:off
    private static final Map<String, String> STREAMERS = Map.of(
        "BrophersGrimm", "https://twitch.tv/thebrophersgrimm",
        "Dayoshi",       "https://twitch.tv/dayoshi",
        "SammySnow",     "https://beacons.ai/sammysnow",
        "Stallion",      "https://linktr.ee/imstallion",
        "StaticE06",     "https://linktr.ee/statice06"
    );
    // @formatter:on

    private static Map.Entry<String, String> chosenStreamer;
    private static @Getter @Nullable Image chosenStreamerImage;

    public static String getChosenStreamer() {
        return chosenStreamer.getKey();
    }

    public static String getChosenStreamerURL() {
        return chosenStreamer.getValue();
    }

    static {
        try {
            chosenStreamer = STREAMERS
                .entrySet()
                .stream()
                .filter(e -> e.getKey().equalsIgnoreCase(System.getProperty("caffeinated.streamer", "")))
                .findFirst()
                // If no streamer is specified, choose a random streamer
                .orElse(STREAMERS.entrySet().toArray(new Map.Entry[0])[(int) Math.floor(Math.random() * STREAMERS.size())]);

            chosenStreamerImage = ImageIO.read(FileUtil.loadResourceAsUrl(String.format("assets/streamers/%s.png", getChosenStreamer())));
            FastLogger.logStatic("Chosen Streamer: %s", chosenStreamer);
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.WARNING, "Failed to load streamer image for %s\n%s", chosenStreamer, e);
        }
    }

}
