package co.casterlabs.caffeinated.updater.target;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import co.casterlabs.caffeinated.updater.Updater;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

class MacTarget implements Target {

    @Override
    public void forceKillApp() throws IOException, InterruptedException {
        Runtime.getRuntime().exec(new String[] {
                "pkill",
                "Casterlabs-Caffeinated"
        }).waitFor();
    }

    @Override
    public boolean isSupported() {
        return Arrays.asList(OSDistribution.MACOS).contains(Platform.osDistribution) &&
            Arrays.asList("aarch64", "x86_64").contains(Platform.archTarget);
    }

    @Override
    public String getDownloadName() {
        return String.format("Casterlabs-Caffeinated-macos-%s.tar.gz", Platform.archTarget);
    }

    @Override
    public String getLaunchCommand() {
        return new File(Updater.appDirectory, "Casterlabs-Caffeinated.app/Contents/MacOS/Casterlabs-Caffeinated").getAbsolutePath();
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
