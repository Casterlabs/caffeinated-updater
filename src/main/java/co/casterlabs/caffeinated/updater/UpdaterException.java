package co.casterlabs.caffeinated.updater;

import lombok.Getter;

public class UpdaterException extends Exception {
    private static final long serialVersionUID = -7563002341328958687L;

    private @Getter Error error;

    public UpdaterException(Error error, String message, Exception cause) {
        super(
            createMessage(message, cause),
            cause
        );
        this.error = error;
    }

    public static enum Error {
        DOWNLOAD_FAILED,
        LAUNCH_FAILED;
    }

    private static String createMessage(String message, Exception cause) {
        if (cause == null || cause.getMessage() == null) {
            return message;
        }

        String causeMessage = cause.getMessage();
        if (causeMessage.contains(":\\") && causeMessage.contains("(")) {
            // C:\...\app\Casterlabs-Caffeinated.exe (windows message)
            causeMessage = causeMessage.substring(causeMessage.indexOf("("));
        }

        return String.format("%s\n%s", message, causeMessage);
    }

}
