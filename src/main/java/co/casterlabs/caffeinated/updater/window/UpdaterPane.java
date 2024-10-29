package co.casterlabs.caffeinated.updater.window;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import co.casterlabs.caffeinated.updater.window.animations.BlankAnimation;
import co.casterlabs.caffeinated.updater.window.animations.DialogAnimation;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class UpdaterPane extends JPanel {
    private static final long serialVersionUID = -4429924866600191261L;

    private static final String[] STREAMERS = {
            "stallion",
            "flankthomas",
            "jcodude",
            "himichannel",
            "statice06",
    };

    public static String chosenStreamer = STREAMERS[0]; // Default is required for WindowBuilder.
    private static Image chosenStreamerImage;

    static {
        try {
            chosenStreamer = STREAMERS[(int) Math.floor(Math.random() * STREAMERS.length)];
            chosenStreamerImage = ImageIO.read(FileUtil.loadResourceAsUrl(String.format("assets/streamers/%s.png", chosenStreamer)));
            FastLogger.logStatic("Chosen Streamer: %s", chosenStreamer);
        } catch (Exception e) {
            FastLogger.logException(e);
        }
    }

    private @Setter @NonNull DialogAnimation currentAnimation = new BlankAnimation();

    private @Getter UpdaterUI ui;

    public UpdaterPane(UpdaterDialog dialog, DialogAnimation animation) throws IOException {
        this.currentAnimation = animation;

        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);

        this.setBackground(UpdaterDialog.TRANSPARENT_COLOR);
        this.setOpaque(false);

        this.ui = new UpdaterUI(dialog, animation);
        layout.putConstraint(SpringLayout.NORTH, ui, 0, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, ui, 0, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.SOUTH, ui, 0, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.EAST, ui, 0, SpringLayout.EAST, this);
        this.add(this.ui);

        AnimationContext
            .getRenderables()
            .add(this::repaint);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Enable antialiasing.
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Paint the background color
//        g2d.clearRect(0, 0, WIDTH, HEIGHT);
        g2d.setBackground(UpdaterDialog.BACKGROUND_COLOR);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Paint the animation (background)
        this.currentAnimation.paintOnBackground(g2d);

        // Paint the background image if set
        if (chosenStreamerImage != null) {
            // The image is same size as the window.
            g2d.drawImage(chosenStreamerImage, 0, 0, null);
        }

        // Paint the animation (over background)
        this.currentAnimation.paintOverBackground(g2d);

        super.paint(g2d);

        // Paint the animation (foreground)
        this.currentAnimation.paintOnForeground(g2d);
    }

}
