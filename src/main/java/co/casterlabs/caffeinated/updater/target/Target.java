package co.casterlabs.caffeinated.updater.target;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import co.casterlabs.caffeinated.updater.window.UpdaterDialog;

public interface Target {

    public void forceKillApp() throws IOException, InterruptedException;

    public boolean isSupported();

    public String getDownloadName();

    public String getLaunchCommand();

    public File getUpdaterLaunchFile();

    public void finalizeUpdate(UpdaterDialog dialog, File appDirectory) throws InterruptedException, IOException;

    public File getResourcesDirectory();

    public static Target get() {
        List<Target> targets = Arrays.asList(new LinuxTarget(), new MacTarget(), new WindowsTarget());

        for (Target target : targets) {
            if (target.isSupported()) {
                return target;
            }
        }

        return targets.get(0);
    }

}
