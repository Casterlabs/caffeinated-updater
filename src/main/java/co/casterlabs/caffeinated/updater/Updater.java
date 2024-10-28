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

import co.casterlabs.caffeinated.updater.util.FileUtil;
import co.casterlabs.caffeinated.updater.util.WebUtil;
import co.casterlabs.caffeinated.updater.util.ZipUtil;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import net.harawata.appdirs.AppDirsFactory;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Updater {
    private static final int VERSION = 30;
    private static final String CHANNEL = System.getProperty("caffeinated.channel", "stable");

    private static String REMOTE_ZIP_DOWNLOAD_URL = "https://cdn.casterlabs.co/dist/" + CHANNEL + "/";
    private static final String REMOTE_COMMIT_URL = "https://cdn.casterlabs.co/dist/" + CHANNEL + "/commit";
    private static final String LAUNCHER_VERSION_URL = "https://cdn.casterlabs.co/dist/updater-version";

    public static String appDataDirectory = AppDirsFactory.getInstance().getUserDataDir("casterlabs-caffeinated", null, null, true);
    private static File appDirectory = new File(appDataDirectory, "app");
    private static File updateFile = new File(appDirectory, "update.zip");
    private static File buildInfoFile = new File(appDirectory, "current_build_info.json");
    private static File expectUpdaterFile = new File(appDirectory, "expect-updater");

    private static @Getter boolean isLauncherOutOfDate = false;
    private static @Getter boolean isPlatformSupported = true;

    private static final String launchCommand;

    static {
        appDirectory.mkdirs();

        switch (Platform.osDistribution) {
            case MACOS:
                launchCommand = appDirectory + "/Casterlabs-Caffeinated.app/Contents/MacOS/Casterlabs-Caffeinated";
                REMOTE_ZIP_DOWNLOAD_URL += "macOS-amd64";
                break;

            case LINUX:
                launchCommand = appDirectory + "/Casterlabs-Caffeinated";
                REMOTE_ZIP_DOWNLOAD_URL += "Linux-amd64";
                break;

            case WINDOWS_NT:
                launchCommand = appDirectory + "/Casterlabs-Caffeinated.exe";
                REMOTE_ZIP_DOWNLOAD_URL += "Windows-amd64";
                break;

            default:
                launchCommand = null;
                isPlatformSupported = false;
                break;
        }

        REMOTE_ZIP_DOWNLOAD_URL += ".zip";

        try {
            int remoteLauncherVersion = Integer.parseInt(WebUtil.sendHttpRequest(HttpRequest.newBuilder().uri(URI.create(LAUNCHER_VERSION_URL))).trim());

            isLauncherOutOfDate = VERSION < remoteLauncherVersion;
        } catch (Exception e) {
            FastLogger.logException(e);
        }
    }

    public static void borkInstall() {
        buildInfoFile.delete();
    }

    public static boolean needsUpdate() {
        try {
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

        try {
            HttpResponse<InputStream> response = WebUtil.sendRawHttpRequest(HttpRequest.newBuilder().uri(URI.create(REMOTE_ZIP_DOWNLOAD_URL)), BodyHandlers.ofInputStream());

            // Download zip.
            {
                dialog.setStatus("Downloading updates...");

                InputStream source = response.body();
                OutputStream dest = new FileOutputStream(updateFile);

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

                source.close();
                dest.close();

                dialog.setProgress(-1);
            }

            // Extract zip
            {
                dialog.setStatus("Installing updates...");
                ZipUtil.unzip(updateFile, appDirectory);

                updateFile.delete();

                switch (Platform.osDistribution) {
                    case LINUX: {
                        // Make the executable... executable on Linux.
                        String executable = appDirectory.getAbsolutePath() + "/Casterlabs-Caffeinated";

                        new ProcessBuilder()
                            .command(
                                "chmod", "+x", executable
                            )
                            .inheritIO()
                            .start()

                            // Wait for exit.
                            .waitFor();
                        break;
                    }

                    case MACOS: {
                        // Unquarantine the app on MacOS.
                        String app = '"' + appDirectory.getAbsolutePath() + "/Casterlabs-Caffeinated.app" + '"';
                        String command = "xattr -rd com.apple.quarantine " + app + " && chmod -R u+x " + app;

                        dialog.setStatus("Waiting for permission...");

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
                        break;
                    }

                    default:
                        break;
                }
            }
        } catch (Exception e) {
            throw new UpdaterException(UpdaterException.Error.DOWNLOAD_FAILED, "Update failed :(", e);
        }
    }

    public static void launch(UpdaterDialog dialog) throws UpdaterException {
        try {
            String updaterCommandLine = co.casterlabs.commons.platform.ProcessUtil.tryGetCommandLine(co.casterlabs.commons.platform.ProcessUtil.getPid());
            FastLogger.logStatic("Updater CommandLine: %s", updaterCommandLine);
            expectUpdaterFile.createNewFile();
            Files.writeString(expectUpdaterFile.toPath(), updaterCommandLine);

            ProcessBuilder pb = new ProcessBuilder()
                .directory(appDirectory)
                .command(launchCommand, "--started-by-updater");

            // TODO look for the build info file before trusting the process. (kill & let
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
