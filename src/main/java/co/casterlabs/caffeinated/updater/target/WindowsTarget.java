package co.casterlabs.caffeinated.updater.target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.updater.Updater;
import co.casterlabs.caffeinated.updater.util.WebUtil;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.commons.platform.OSDistribution;

public class WindowsTarget implements Target {

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
        return Updater.appDirectory + "/Casterlabs-Caffeinated.exe";
    }

    @Override
    public void updateUpdater(UpdaterDialog dialog) throws IOException, InterruptedException {
        HttpResponse<InputStream> response = WebUtil.sendRawHttpRequest(HttpRequest.newBuilder().uri(URI.create("https://cdn.casterlabs.co/dist/Caffeinated-Installer.exe")), BodyHandlers.ofInputStream());

        final File tempInstaller = new File(System.getProperty("java.io.tmpdir"), "Caffeinated-Installer.exe");

        tempInstaller.delete();
        tempInstaller.createNewFile();

        dialog.setStatus("Downloading installer...");

        try (InputStream source = response.body();
            OutputStream dest = new FileOutputStream(tempInstaller)) {

            double totalSize = Long.parseLong(response.headers().firstValue("Content-Length").orElse("0"));
            int totalRead = 0;

            byte[] buffer = new byte[2048];
            int read = 0;

            while ((read = source.read(buffer)) != -1) {
                dest.write(buffer, 0, read);
                totalRead += read;

                double progress = totalRead / totalSize;

                dialog.setStatus(String.format("Downloading installer... (%.0f%%)", progress * 100));
                dialog.setProgress(progress);
            }

            dest.flush();
            dialog.setProgress(-1);

            Runtime.getRuntime().exec(new String[] {
                    "powershell",
                    "-Command",
                    "\"Start-Process '" + tempInstaller.getCanonicalPath() + "' -Verb RunAs\""
            });
            TimeUnit.SECONDS.sleep(2);
            System.exit(0);
        }
    }

    @Override
    public File getUpdaterLaunchFile() {
        return new File("./Casterlabs-Caffeinated.exe");
    }

    @Override
    public void finalizeUpdate(UpdaterDialog dialog, File appDirectory) throws InterruptedException, IOException {
        // NOOP
    }

}
