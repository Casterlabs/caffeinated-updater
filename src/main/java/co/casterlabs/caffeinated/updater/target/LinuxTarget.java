package co.casterlabs.caffeinated.updater.target;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import co.casterlabs.caffeinated.updater.Updater;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.commons.platform.LinuxLibC;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import lombok.SneakyThrows;

class LinuxTarget implements Target {

    @Override
    public void forceKillApp() throws IOException, InterruptedException {
        Runtime.getRuntime().exec(new String[] {
                "pkill",
                "Casterlabs-Caffeinated"
        }).waitFor();
    }

    @SneakyThrows
    @Override
    public boolean isSupported() {
        return Arrays.asList(OSDistribution.LINUX).contains(Platform.osDistribution) &&
            Arrays.asList("aarch64", "arm", "ppc64le", "x86_64").contains(Platform.archTarget) &&
            LinuxLibC.isGNU();
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
