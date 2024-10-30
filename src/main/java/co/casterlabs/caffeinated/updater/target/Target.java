package co.casterlabs.caffeinated.updater.target;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;

public interface Target {

    public OSDistribution supportedOS();

    public List<String> supportedTargets();

    public String getDownloadName();

    public String getLaunchCommand();

    public void updateUpdater(UpdaterDialog dialog) throws IOException, InterruptedException;

    public File getUpdaterLaunchFile();

    public void finalizeUpdate(UpdaterDialog dialog, File appDirectory) throws InterruptedException, IOException;

    public static Target get() {
        List<Target> targets = Arrays.asList(new LinuxTarget(), new MacTarget(), new WindowsTarget());

        for (Target target : targets) {
            if (target.supportedOS() == Platform.osDistribution && target.supportedTargets().contains(Platform.archTarget)) {
                return target;
            }
        }

        return targets.get(0);
    }

}
