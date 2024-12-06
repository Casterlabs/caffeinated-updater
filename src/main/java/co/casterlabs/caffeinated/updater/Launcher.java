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

        // Trigger the file watcher(s).
        File dieFile = new File(Updater.ipcDirectory, "die");
        dieFile.createNewFile();
        Thread.sleep(1500);
        dieFile.delete();
        Thread.sleep(500);

//        try {
//            Updater.target.forceKillApp();
//        } catch (Throwable t) {
//            FastLogger.logStatic(LogLevel.WARNING, "Could not force kill the app, this is probably fine.\n%s", t);
//        }

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
            TimeUnit.SECONDS.sleep(1);

            switch (Updater.needsUpdate()) {
                case 0: // up-to-date
                    FastLogger.logStatic("You are up to date!");
                    dialog.setStatus("You are up to date!");
                    break;

                case 1: // needs update
                    FastLogger.logStatic("Downloading updates.");
                    Updater.downloadAndInstallUpdate(dialog);
                    dialog.setStatus("Done!");
                    break;

                case 2: // error
                    dialog.setStatus("Failed to check for updates, launching anyway.");
                    break;
            }

            TimeUnit.SECONDS.sleep(2);
            dialog.setStatus("Launching Caffeinated...");
            Updater.launch(dialog);
        } catch (UpdaterException e) {
            dialog.setStatus(e.getMessage());

            FastLogger.logStatic(LogLevel.SEVERE, e.getMessage());
            FastLogger.logException(e.getCause());

            TimeUnit.SECONDS.sleep(10);

            switch (e.getError()) {
                case LAUNCH_FAILED:
                    Updater.borkInstall();
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
