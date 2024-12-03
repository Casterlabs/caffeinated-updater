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
    public void forceKillApp() throws IOException, InterruptedException {
        Runtime.getRuntime().exec(new String[] {
                "pkill",
                "Casterlabs-Caffeinated"
        }).waitFor();
    }

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
        return String.format("Casterlabs-Caffeinated-gnulinux-%s.tar.gz", Platform.archTarget);
    }

    @Override
    public String getLaunchCommand() {
        return new File(Updater.appDirectory, "Casterlabs-Caffeinated").getAbsolutePath();
    }

    @Override
    public void updateUpdater(UpdaterDialog dialog) throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getUpdaterLaunchFile() {
        return new File("./Casterlabs-Caffeinated");
    }

    @Override
    public void finalizeUpdate(UpdaterDialog dialog, File appDirectory) throws InterruptedException, IOException {
        // NOOP
    }

    @Override
    public File getResourcesDirectory() {
        return Updater.appDirectory;
    }

}
