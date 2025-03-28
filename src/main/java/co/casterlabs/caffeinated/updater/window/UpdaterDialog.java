package co.casterlabs.caffeinated.updater.window;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Taskbar;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import co.casterlabs.caffeinated.updater.window.animations.DialogAnimation;
import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class UpdaterDialog extends JFrame implements Closeable {
    private static final long serialVersionUID = 327804372803161092L;

    public static final int WIDTH = 500;
    public static final int HEIGHT = 320;
    public static final int ARC = 11;

    public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);
    public static final Color BACKGROUND_COLOR = new Color(18, 18, 18); // #121212
    public static final Color TEXT_COLOR = new Color(181, 181, 181); // #b5b5b5

    private @Getter UpdaterPane pane;
    private @Getter UpdaterUI ui;

    private int currentProgress = 0;

    public UpdaterDialog(DialogAnimation animation) throws IOException {
//        super((Window) null);

        this.pane = new UpdaterPane(this, animation);
        this.ui = this.pane.getUi();

        this.getContentPane().add(this.pane);

        // Window settings.
        this.setTitle("Caffeinated Updater");
        this.setType(Window.Type.POPUP);
        this.setAlwaysOnTop(true);
        this.setUndecorated(true);
        this.setResizable(false);

        // Colors.
        this.setBackground(BACKGROUND_COLOR);
        this.getContentPane().setBackground(BACKGROUND_COLOR);
        this.setForeground(TEXT_COLOR);

        // Size & Shape.
        this.setSize(WIDTH, HEIGHT);
        this.setShape(new RoundRectangle2D.Double(0, 0, WIDTH, HEIGHT, ARC, ARC));

        // Drag listener.
        {
            DragListener frameDragListener = new DragListener(this);

            this.addMouseListener(frameDragListener);
            this.addMouseMotionListener(frameDragListener);
        }

        // Set the location.
        {
            Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
            GraphicsDevice currentScreen = null;

            for (GraphicsDevice screen : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                if (screen.getDefaultConfiguration().getBounds().contains(mouseLoc)) {
                    currentScreen = screen;
                    break;
                }
            }

            if (currentScreen == null) {
                currentScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            }

            DisplayMode display = currentScreen.getDisplayMode();

            if (display != null) {
                int x = (display.getWidth() / 2) - (WIDTH / 2);
                int y = (display.getHeight() / 2) - (HEIGHT / 2);

                this.setLocation(x, y);
            }
        }

        // Set the icon.
        try {
            URL iconUrl = FileUtil.loadResourceAsUrl("assets/" + animation.getIcon());
            ImageIcon img = new ImageIcon(iconUrl);

            this.setIconImage(img.getImage());
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Could not set the dialog's icon.");
            FastLogger.logException(e);
        }

        // Handle the tray's close button.
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) {
            this.toFront();
            this.createBufferStrategy(2);
        }
    }

    @Override
    public void close() {
        this.dispose();
        System.exit(0);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public void setStatus(String status) {
        this.ui.setStatus(status);
    }

    public void setProgress(double progress) {
        if (!Taskbar.isTaskbarSupported()) return;

        Taskbar taskbar = Taskbar.getTaskbar();
        if (!taskbar.isSupported(Taskbar.Feature.PROGRESS_STATE_WINDOW)) return;

        if (progress < 0) {
            this.currentProgress = 0;
            taskbar.setWindowProgressValue(this, -1);
            taskbar.setWindowProgressState(this, Taskbar.State.OFF);
        } else {
            int percent = (int) Math.round(progress * 100); // 0-1 -> 0-100

            if (this.currentProgress == percent) {
                return;
            }

            this.currentProgress = percent;
            taskbar.setWindowProgressState(this, Taskbar.State.NORMAL);
            taskbar.setWindowProgressValue(this, percent);
        }
    }

    public void setLoading(boolean loading) {
        this.ui.setLoading(loading);
    }

}
