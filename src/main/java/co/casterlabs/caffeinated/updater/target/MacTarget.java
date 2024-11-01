package co.casterlabs.caffeinated.updater.target;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import co.casterlabs.caffeinated.updater.Updater;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class MacTarget implements Target {

    @Override
    public OSDistribution supportedOS() {
        return OSDistribution.MACOS;
    }

    @Override
    public List<String> supportedTargets() {
        return Arrays.asList("aarch64", "x86_64");
    }

    @Override
    public String getDownloadName() {
        return String.format("Casterlabs-Caffeinated-macos-%s.tar.gz", Platform.archTarget);
    }

    @Override
    public void updateUpdater(UpdaterDialog dialog) throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getUpdaterLaunchFile() {
        return new File("../MacOS/Casterlabs-Caffeinated");
    }

    @Override
    public void finalizeUpdate(UpdaterDialog dialog, File appDirectory) throws InterruptedException, IOException {
        // Unquarantine the app on MacOS.
        String app = '"' + new File(appDirectory, "Casterlabs-Caffeinated.app").getAbsolutePath() + '"';
        String command = "xattr -rd com.apple.quarantine " + app;

        dialog.setStatus("Waiting for permission...");
        FastLogger.logStatic("Trying to unquarantine the app...");

        new ProcessBuilder()
            .command(
                "osascript",
                "-e",
                "do shell script \"" + command.replace("\"", "\\\"") + "\" with prompt \"Casterlabs Caffeinated would like to make changes.\" with administrator privileges"
            )
            .inheritIO()
            .start()
            .waitFor();
    }

    @Override
    public File getResourcesDirectory() {
        return new File(Updater.appDirectory, "Casterlabs-Caffeinated.app/Contents/Resources");
    }

}
