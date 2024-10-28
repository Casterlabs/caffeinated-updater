package co.casterlabs.caffeinated.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.util.Scanner;

import co.casterlabs.caffeinated.updater.target.Target;
import co.casterlabs.caffeinated.updater.util.FileUtil;
import co.casterlabs.caffeinated.updater.util.WebUtil;
import co.casterlabs.caffeinated.updater.util.archive.ArchiveExtractor;
import co.casterlabs.caffeinated.updater.util.archive.Archives;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import net.harawata.appdirs.AppDirsFactory;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Updater {
    public static final int VERSION = 30;
    public static final String CHANNEL = System.getProperty("caffeinated.channel", "stable");
    public static final String CHANNEL_URL_BASE = "https://cdn.casterlabs.co/dist/" + CHANNEL;

    private static final String LAUNCHER_VERSION_URL = "https://cdn.casterlabs.co/dist/updater-version";
    private static final String REMOTE_COMMIT_URL = CHANNEL_URL_BASE + "/commit";

    public static String appDataDirectory = AppDirsFactory.getInstance().getUserDataDir("casterlabs-caffeinated", null, null, true);
    public static File appDirectory = new File(appDataDirectory, "app");

    public static final Target target = Target.get();

    public static boolean isLauncherOutOfDate() throws IOException, InterruptedException {
        int remoteLauncherVersion = Integer.parseInt(WebUtil.sendHttpRequest(HttpRequest.newBuilder().uri(URI.create(LAUNCHER_VERSION_URL))).trim());
        return VERSION < remoteLauncherVersion;
    }

    public static boolean isPlatformSupported() {
        return target != null;
    }

    static {
        appDirectory.mkdirs();
    }

    public static void borkInstall() {
        new File(appDirectory, "current_build_info.json").delete();
    }

    public static boolean needsUpdate() {
        try {
            File buildInfoFile = new File(appDirectory, "current_build_info.json");

            // Check for existence of files.
            if (!buildInfoFile.exists()) {
                FastLogger.logStatic("Build was not healthy, forcing redownload.");
                return true;
            }

            JsonObject buildInfo = Rson.DEFAULT.fromJson(FileUtil.readFile(buildInfoFile), JsonObject.class);

            // Check the version.
            String installedChannel = buildInfo.getString("buildChannel");
            if (!installedChannel.equals(CHANNEL)) return true;

            String installedCommit = buildInfo.getString("commit");
            String remoteCommit = WebUtil.sendHttpRequest(HttpRequest.newBuilder().uri(URI.create(REMOTE_COMMIT_URL))).trim();
            if (!remoteCommit.equals(installedCommit)) return true;

            return false;
        } catch (IOException | InterruptedException e) {
            FastLogger.logException(e);
            return true;
        }
    }

    public static void downloadAndInstallUpdate(UpdaterDialog dialog) throws UpdaterException {
        FileUtil.emptyDirectory(appDirectory);

        File updateFile = new File(appDirectory, target.getDownloadName());

        try {
            HttpResponse<InputStream> response = WebUtil.sendRawHttpRequest(
                HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%s/%s", CHANNEL_URL_BASE, target.getDownloadName()))),
                BodyHandlers.ofInputStream()
            );

            // Download archive.
            dialog.setStatus("Downloading updates...");

            try (InputStream source = response.body(); OutputStream dest = new FileOutputStream(updateFile)) {
                double totalSize = Long.parseLong(response.headers().firstValue("Content-Length").orElse("0"));
                int totalRead = 0;

                byte[] buffer = new byte[2048];
                int read = 0;

                while ((read = source.read(buffer)) != -1) {
                    dest.write(buffer, 0, read);
                    totalRead += read;

                    double progress = totalRead / totalSize;

                    dialog.setStatus(String.format("Downloading updates... (%.0f%%)", progress * 100));
                    dialog.setProgress(progress);
                }

                dest.flush();
                dialog.setProgress(-1);
            }

            // Extract archive.
            {
                dialog.setStatus("Installing updates...");
                ArchiveExtractor.extract(Archives.probeFormat(updateFile), updateFile, appDirectory);

                updateFile.delete();

                if (Platform.osDistribution == OSDistribution.MACOS) {
                    // Unquarantine the app on MacOS.
                    String app = '"' + appDirectory.getAbsolutePath() + "/Casterlabs-Caffeinated.app" + '"';
                    String command = "xattr -rd com.apple.quarantine " + app + " && chmod -R u+x " + app;

                    dialog.setStatus("Waiting for permission...");
                    FastLogger.logStatic("Trying to unquarantine the app...");

                    new ProcessBuilder()
                        .command(
                            "osascript",
                            "-e",
                            "do shell script \"" + command.replace("\"", "\\\"") + "\" with prompt \"Casterlabs Caffeinated would like to make changes.\" with administrator privileges"
                        )
                        .inheritIO()
                        .start()

                        // Wait for exit.
                        .waitFor();
                }

                File executable = new File(appDirectory, target.getLaunchCommand());
                executable.setExecutable(true);
            }
        } catch (Exception e) {
            throw new UpdaterException(UpdaterException.Error.DOWNLOAD_FAILED, "Update failed :(", e);
        }
    }

    public static void launch(UpdaterDialog dialog) throws UpdaterException {
        try {
            String updaterCommandLine = co.casterlabs.commons.platform.ProcessUtil.tryGetCommandLine(co.casterlabs.commons.platform.ProcessUtil.getPid());
            FastLogger.logStatic("Updater CommandLine: %s", updaterCommandLine);

            File expectUpdaterFile = new File(appDirectory, "expect-updater");
            expectUpdaterFile.createNewFile();
            Files.writeString(expectUpdaterFile.toPath(), updaterCommandLine);

            ProcessBuilder pb = new ProcessBuilder()
                .directory(appDirectory)
                .command(target.getLaunchCommand(), "--started-by-updater");

            // TODO look for the build_ok file before trusting the process. (kill & let
            // the user know it's dead)

            if (Platform.osDistribution == OSDistribution.MACOS) {
                // On MacOS we do not want to keep the updater process open as it'll stick in
                // the dock. So we start the process and kill the updater to make sure that
                // doesn't happen.
                FastLogger.logStatic(LogLevel.INFO, "The process will now exit, this is so the updater's icon doesn't stick in the dock.");
                pb.start();
                dialog.close();
                System.exit(0);
                return;
            }

            Process proc = pb
                .redirectOutput(Redirect.PIPE)
                .start();

            try (Scanner in = new Scanner(proc.getInputStream())) {
                boolean hasAlreadyStarted = false;
                while (true) {
                    String line = in.nextLine();
                    System.out.println(line);

                    if (!hasAlreadyStarted && line.contains("Starting the UI")) {
                        // Look for "Starting the UI" before we close the dialog.
                        FastLogger.logStatic(LogLevel.INFO, "UI Started!");
                        dialog.close();
                        System.exit(0);
                    }
                }
            } catch (Exception ignored) {}
        } catch (Exception e) {
            throw new UpdaterException(UpdaterException.Error.LAUNCH_FAILED, "Could not launch update :(", e);
        }
    }

}
