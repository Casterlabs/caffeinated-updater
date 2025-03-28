package co.casterlabs.caffeinated.updater.window;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

import co.casterlabs.caffeinated.updater.window.animations.AbstractDialogAnimation;

class UpdaterPane extends JPanel {
    private static final long serialVersionUID = -4429924866600191261L;

    private static final int PROGRESS_SIZE = 5;

    private AbstractDialogAnimation currentAnimation;

    UpdaterUI ui;
    volatile double progress;

    public UpdaterPane(UpdaterDialog dialog, AbstractDialogAnimation animation, AnimationContext animationContext) {
        this.currentAnimation = animation;

        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);

        this.setBackground(UpdaterDialog.TRANSPARENT_COLOR);
        this.setOpaque(false);

        this.ui = new UpdaterUI(dialog, animation, animationContext);
        layout.putConstraint(SpringLayout.NORTH, this.ui, 0, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, this.ui, 0, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.SOUTH, this.ui, 0, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.EAST, this.ui, 0, SpringLayout.EAST, this);
        this.add(this.ui);

        animationContext.toTick
            .add((_unused) -> this.repaint());
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Enable antialiasing.
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Paint the background color
//        g2d.clearRect(0, 0, UpdaterDialog.WIDTH, UpdaterDialog.HEIGHT);
        g2d.setBackground(UpdaterDialog.BACKGROUND_COLOR);
        g2d.fillRect(0, 0, UpdaterDialog.WIDTH, UpdaterDialog.HEIGHT);

        // Paint the animation (background)
        this.currentAnimation.paintOnBackground(g2d);

        // Paint the background image if set
        if (Streamers.getChosenStreamerImage() != null) {
            // The image is same size as the window.
            g2d.drawImage(Streamers.getChosenStreamerImage(), 0, 0, null);
        }

        // Paint the animation (over background)
        this.currentAnimation.paintOverBackground(g2d);

        super.paint(g2d);
        if (this.progress > 0) {
            g2d.setColor(UpdaterDialog.TEXT_COLOR);

            int width = (int) (UpdaterDialog.WIDTH * this.progress);
            g2d.fillRect(0, UpdaterDialog.HEIGHT - PROGRESS_SIZE, width, PROGRESS_SIZE);
        }

        // Paint the animation (foreground)
        this.currentAnimation.paintOnForeground(g2d);
    }

}
