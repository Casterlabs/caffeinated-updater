package co.casterlabs.caffeinated.updater;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import javax.swing.UIManager;

import co.casterlabs.caffeinated.updater.animations.DialogAnimation;
import co.casterlabs.caffeinated.updater.util.WebUtil;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.commons.platform.OSFamily;
import co.casterlabs.commons.platform.Platform;
import lombok.Getter;
import lombok.Setter;
import xyz.e3ndr.fastloggingframework.FastLogHandler;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogColor;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Launcher {

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        final File logsDir = new File(Updater.appDataDirectory, "logs");
        final File logFile = new File(logsDir, "updater.log");

        try {
            logsDir.mkdirs();
            logFile.createNewFile();

            @SuppressWarnings("resource")
            final FileOutputStream logOut = new FileOutputStream(logFile, true);

            FastLoggingFramework.setLogHandler(new FastLogHandler() {
                @Override
                protected void log(String name, LogLevel level, String formatted) {
                    System.out.println(LogColor.translateToAnsi(formatted));

                    String stripped = LogColor.strip(formatted);

                    try {
                        logOut.write(stripped.getBytes());
                        logOut.write('\n');
                        logOut.flush();
                    } catch (IOException e) {
                        FastLogger.logException(e);
                    }
                }
            });

            logOut.write(
                String.format("\n\n---------- %s ----------\n", Instant.now().toString())
                    .getBytes()
            );

            FastLogger.logStatic(LogLevel.INFO, "App Directory: %s", Updater.appDataDirectory);
            FastLogger.logStatic("Log file: %s", logFile);
        } catch (IOException e) {
            FastLogger.logException(e);
        }
    }

    private static @Getter Thread updaterThread;
    private static UpdaterDialog dialog;
    private static @Setter UpdaterMode mode = UpdaterMode.NORMAL;

    public static void main(String[] args) throws Exception {
        updaterThread = Thread.currentThread();

        dialog = new UpdaterDialog(DialogAnimation.getCurrentAnimation());

        dialog.setStatus("");
        dialog.setVisible(true);

        String[] kill;
        if (Platform.osFamily == OSFamily.WINDOWS) {
            kill = new String[] {
                    "taskkill",
                    "/F",
                    "/IM",
                    "Casterlabs-Caffeinated.exe"
            };
        } else {
            kill = new String[] {
                    "pkill",
                    "Casterlabs-Caffeinated"
            };
        }
        Runtime.getRuntime().exec(kill);

        doChecks();
    }

    public static void doChecks() throws Exception {
        if (System.getProperty("caffeinated.donotupdate", "false").equalsIgnoreCase("true")) {
            dialog.setStatus("Twiddling my thumbs...");
            Thread.sleep(Long.MAX_VALUE);
        }

        dialog.setStatus("Checking for updates...");

        if (Updater.isLauncherOutOfDate()) {
            TimeUnit.SECONDS.sleep(1);

            // Try to autodownload the installer and run it.
            if (Platform.osFamily == OSFamily.WINDOWS) {
                try {
                    updateUpdaterWindows();
                    return;
                } catch (Exception e) {
                    FastLogger.logStatic("Couldn't automagically update the updater (defaulting to the normal message):\n%s", e);
                }
            }

            // TODO display this message better and give a button to download.
            dialog.setLoading(false);
            dialog.setStatus("Your launcher is out of date! (Download from casterlabs.co)");

            Desktop.getDesktop().browse(new URI("https://casterlabs.co"));
        } else {
            checkForUpdates();
        }
    }

    private static void updateUpdaterWindows() throws Exception {
        HttpResponse<InputStream> response = WebUtil.sendRawHttpRequest(HttpRequest.newBuilder().uri(URI.create("https://cdn.casterlabs.co/dist/Caffeinated-Installer.exe")), BodyHandlers.ofInputStream());

        final File tempInstaller = new File(System.getProperty("java.io.tmpdir"), "Caffeinated-Installer.exe");

        tempInstaller.delete();
        tempInstaller.createNewFile();

        dialog.setStatus("Downloading installer...");

        InputStream source = response.body();
        OutputStream dest = new FileOutputStream(tempInstaller);

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

        source.close();
        dest.close();

        dialog.setProgress(-1);

        Runtime.getRuntime().exec(new String[] {
                "powershell",
                "-Command",
                "\"Start-Process '" + tempInstaller.getCanonicalPath() + "' -Verb RunAs\""
        });
        TimeUnit.SECONDS.sleep(2);
        System.exit(0);
    }

    private static void checkForUpdates() throws Exception {
        try {
            // Artificial delay added in here because it'd be too jarring otherwise.
            // Heh, JARring, haha.

            if (Updater.needsUpdate() || (mode == UpdaterMode.FORCE)) {
                FastLogger.logStatic("Downloading updates.");
                Updater.downloadAndInstallUpdate(dialog);
                dialog.setStatus("Done!");
            } else {
                TimeUnit.SECONDS.sleep(1);
                FastLogger.logStatic("You are up to date!");
                dialog.setStatus("You are up to date!");
            }

            TimeUnit.SECONDS.sleep(2);
            dialog.setStatus("Launching Caffeinated...");
            Updater.launch(dialog);
        } catch (UpdaterException e) {
            dialog.setStatus(e.getMessage());

            FastLogger.logStatic(LogLevel.SEVERE, e.getMessage());
            FastLogger.logException(e.getCause());

            switch (e.getError()) {
                case LAUNCH_FAILED:
                    Updater.borkInstall();
                    TimeUnit.SECONDS.sleep(2);
                    dialog.setStatus("Could not launch update, redownloading in 3.");
                    TimeUnit.SECONDS.sleep(1);
                    dialog.setStatus("Could not launch update, redownloading in 2.");
                    TimeUnit.SECONDS.sleep(1);
                    dialog.setStatus("Could not launch update, redownloading in 1.");
                    TimeUnit.SECONDS.sleep(1);
                    checkForUpdates();
                    return;

                case DOWNLOAD_FAILED:
                    Updater.borkInstall();
                    TimeUnit.SECONDS.sleep(2);
                    dialog.setStatus("Update failed, retrying in 3.");
                    TimeUnit.SECONDS.sleep(1);
                    dialog.setStatus("Update failed, retrying in 2.");
                    TimeUnit.SECONDS.sleep(1);
                    dialog.setStatus("Update failed, retrying in 1.");
                    TimeUnit.SECONDS.sleep(1);
                    checkForUpdates();
                    return;

                default:
                    return;
            }
        }
    }

}
