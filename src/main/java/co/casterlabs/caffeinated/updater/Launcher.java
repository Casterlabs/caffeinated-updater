package co.casterlabs.caffeinated.updater;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import javax.swing.UIManager;

import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.caffeinated.updater.window.animations.DialogAnimation;
import co.casterlabs.commons.platform.OSFamily;
import co.casterlabs.commons.platform.Platform;
import lombok.Getter;
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

            try {
                Updater.target.updateUpdater(dialog);
            } catch (Exception e) {
                FastLogger.logException(e);

                // TODO display this message better and give a button to download.
                dialog.setLoading(false);
                dialog.setStatus("Your launcher is out of date! (Download from casterlabs.co)");

                Desktop.getDesktop().browse(new URI("https://casterlabs.co"));
            }
        } else {
            checkForUpdates();
        }
    }

    private static void checkForUpdates() throws Exception {
        try {
            // Artificial delay added in here because it'd be too jarring otherwise.
            // Heh, JARring, haha.

            if (Updater.needsUpdate()) {
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
