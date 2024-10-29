package co.casterlabs.caffeinated.updater.target;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import co.casterlabs.caffeinated.updater.Updater;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;

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
        return String.format("macOS-%s.tar.gz", Platform.archTarget);
    }

    @Override
    public String getLaunchCommand() {
        return Updater.appDirectory + "/Casterlabs-Caffeinated.app/Contents/MacOS/Casterlabs-Caffeinated";
    }

    @Override
    public void updateUpdater(UpdaterDialog dialog) throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getUpdaterLaunchFile() {
        return new File("../MacOS/Casterlabs-Caffeinated");
    }

}
