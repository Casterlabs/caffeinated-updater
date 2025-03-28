package co.casterlabs.caffeinated.updater.window;

import java.awt.Image;

import javax.imageio.ImageIO;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

class Streamers {

    private static final String[] STREAMERS = {
            "stallion",
            "jcodude",
            "himichannel",
            "statice06",
            "dayoshi",
            "thebrophersgrimm",
    };

    private static @Getter String chosenStreamer = STREAMERS[0]; // Default is required for WindowBuilder.
    private static @Getter @Nullable Image chosenStreamerImage;

    static {
        try {
            chosenStreamer = STREAMERS[(int) Math.floor(Math.random() * STREAMERS.length)];
            chosenStreamerImage = ImageIO.read(FileUtil.loadResourceAsUrl(String.format("assets/streamers/%s.png", chosenStreamer)));
            FastLogger.logStatic("Chosen Streamer: %s", chosenStreamer);
        } catch (Exception e) {
            FastLogger.logException(e);
        }
    }

}
