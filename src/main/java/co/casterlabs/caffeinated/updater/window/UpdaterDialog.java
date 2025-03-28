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
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import co.casterlabs.caffeinated.updater.window.animations.AbstractDialogAnimation;
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

    private static final String WINDOW_TITLE = "Caffeinated Updater";

    private AnimationContext animationContext = new AnimationContext(this);
    private UpdaterPane pane;

    private int currentProgress = 0;

    public UpdaterDialog() {
//        super((Window) null);

        AbstractDialogAnimation animation = AbstractDialogAnimation.getCurrentAnimation(this.animationContext);
        this.pane = new UpdaterPane(this, animation, this.animationContext);

        this.getContentPane().add(this.pane);

        // Window settings.
        this.setTitle(WINDOW_TITLE);
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
        this.pane.ui.setStatus(status);
    }

    public void setProgress(double progress) {
        if (progress < 0) {
            this.currentProgress = -1;
            this.pane.progress = -1;
        } else {
            int percent = (int) Math.round(progress * 100); // 0-1 -> 0-100
            this.pane.progress = progress;

            if (this.currentProgress == percent) {
                // Optimization to avoid calling setWindowProgressValue unnecessarily.
                return;
            }

            this.currentProgress = percent;
        }

        try {
            Taskbar taskbar = Taskbar.getTaskbar();

            if (this.currentProgress < 0) {
                taskbar.setWindowProgressValue(this, -1);
                taskbar.setWindowProgressState(this, Taskbar.State.OFF);
            } else {
                taskbar.setWindowProgressState(this, Taskbar.State.NORMAL);
                taskbar.setWindowProgressValue(this, this.currentProgress);
            }
        } catch (UnsupportedOperationException e) {
            if (this.currentProgress < 0) {
                this.setTitle(WINDOW_TITLE);
            } else {
                this.setTitle(String.format("%s (%d%%)", WINDOW_TITLE, this.currentProgress));
            }
        }
    }

    public void setLoading(boolean loading) {
        this.pane.ui.setLoading(loading);
    }

}
