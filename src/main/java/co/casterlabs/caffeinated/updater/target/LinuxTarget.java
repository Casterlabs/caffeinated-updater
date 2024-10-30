package co.casterlabs.caffeinated.updater.target;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import co.casterlabs.caffeinated.updater.Updater;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;

public class LinuxTarget implements Target {

    @Override
    public OSDistribution supportedOS() {
        return OSDistribution.LINUX;
    }

    @Override
    public List<String> supportedTargets() {
        return Arrays.asList("aarch64", "arm", "ppc64le", "x86_64");
    }

    @Override
    public String getDownloadName() {
        return String.format("Linux-%s.tar.gz", Platform.archTarget);
    }

    @Override
    public String getLaunchCommand() {
        return Updater.appDirectory + "/Casterlabs-Caffeinated";
    }

    @Override
    public void updateUpdater(UpdaterDialog dialog) throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getUpdaterLaunchFile() {
        return new File("/Casterlabs-Caffeinated");
    }

    @Override
    public void kill(String processName) throws InterruptedException, IOException {
        Runtime.getRuntime().exec(new String[] {
                "pkill",
                processName
        }).waitFor();
    }

}
