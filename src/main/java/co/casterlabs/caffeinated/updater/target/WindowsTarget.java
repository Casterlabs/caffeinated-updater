package co.casterlabs.caffeinated.updater.target;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import co.casterlabs.caffeinated.updater.Updater;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.commons.platform.OSDistribution;

public class WindowsTarget implements Target {

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
    public OSDistribution supportedOS() {
        return OSDistribution.WINDOWS_NT;
    }

    @Override
    public List<String> supportedTargets() {
        return Arrays.asList("x86_64");
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
