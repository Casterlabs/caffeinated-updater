package co.casterlabs.caffeinated.updater.target;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import co.casterlabs.caffeinated.updater.Updater;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;

class WindowsTarget implements Target {

    @Override
    public void forceKillApp() throws IOException, InterruptedException {
        Runtime.getRuntime().exec(new String[] {
                "taskkill",
                "/F",
                "/IM",
                "Casterlabs-Caffeinated.exe"
        }).waitFor();
    }

    @Override
    public boolean supported() {
        return Arrays.asList(OSDistribution.WINDOWS_NT).contains(Platform.osDistribution) &&
            Arrays.asList("x86_64").contains(Platform.archTarget);
    }

    @Override
    public String getDownloadName() {
        return "Casterlabs-Caffeinated-windows-x86_64.zip";
    }

    @Override
    public String getLaunchCommand() {
        return new File(Updater.appDirectory, "Casterlabs-Caffeinated.exe").getAbsolutePath();
    }

    @Override
    public File getUpdaterLaunchFile() {
        return new File("./Casterlabs-Caffeinated.exe");
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
