package co.casterlabs.caffeinated.updater.window;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import co.casterlabs.caffeinated.updater.window.animations.AbstractDialogAnimation;
import lombok.NonNull;

class UpdaterUI extends JPanel {
    private static final long serialVersionUID = -6590073036152631171L;

    public static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

    private LoadingSpinner loadingSpinner;
    private JLabel statusText;

    public UpdaterUI(UpdaterDialog dialog, AbstractDialogAnimation animation, AnimationContext animationContext) {
        SpringLayout layout = new SpringLayout();

        this.setBackground(UpdaterDialog.BACKGROUND_COLOR);
        this.setSize(UpdaterDialog.WIDTH, UpdaterDialog.HEIGHT);
        this.setLayout(layout);

        // Check for this variable, so we can actually *see* what we're
        // doing in WindowBuilder.
        if (dialog != null) {
            this.setOpaque(false);
            this.setBackground(new Color(0, 0, 0, 0));
        }

        this.statusText = new JLabel();
        layout.putConstraint(SpringLayout.SOUTH, this.statusText, -32, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.EAST, this.statusText, -10, SpringLayout.EAST, this);
        this.statusText.setFont(FONT);
        this.statusText.setForeground(UpdaterDialog.TEXT_COLOR);
        this.statusText.setOpaque(false);
        this.setStatus("Checking for updates...");
        this.add(this.statusText);

        JLabel streamerText = new JLabel();
        layout.putConstraint(SpringLayout.SOUTH, streamerText, -9, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.EAST, streamerText, -10, SpringLayout.EAST, this);
        streamerText.setFont(FONT);
        streamerText.setHorizontalAlignment(JLabel.RIGHT);
        streamerText.setForeground(UpdaterDialog.TEXT_COLOR);
        streamerText.setOpaque(false);
        streamerText.setText("@" + Streamers.getChosenStreamer());
        this.add(streamerText);

        this.loadingSpinner = new LoadingSpinner(animationContext);
        layout.putConstraint(SpringLayout.NORTH, this.statusText, 13, SpringLayout.NORTH, this.loadingSpinner);
        layout.putConstraint(SpringLayout.WEST, this.statusText, 6, SpringLayout.EAST, this.loadingSpinner);
        layout.putConstraint(SpringLayout.NORTH, this.loadingSpinner, 255, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.SOUTH, this.loadingSpinner, -19, SpringLayout.SOUTH, this);
        layout.putConstraint(SpringLayout.WEST, this.loadingSpinner, 20, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.EAST, this.loadingSpinner, 70, SpringLayout.WEST, this);
        this.add(this.loadingSpinner);

        ImageButton closeButton = new ImageButton("close.png", dialog::close);
        layout.putConstraint(SpringLayout.NORTH, closeButton, 10, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, closeButton, -43, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.SOUTH, closeButton, 45, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, closeButton, -10, SpringLayout.EAST, this);
        this.add(closeButton);

        if (animation.shouldShowCasterlabsBanner()) {
            ImageButton casterlabsBanner = new ImageButton("banner_white.png", null);
            layout.putConstraint(SpringLayout.NORTH, casterlabsBanner, 10, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.WEST, casterlabsBanner, 10, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.SOUTH, casterlabsBanner, 93, SpringLayout.NORTH, this);
            layout.putConstraint(SpringLayout.EAST, casterlabsBanner, 265, SpringLayout.WEST, this);
            this.add(casterlabsBanner);
        }

    }

    public void setLoading(boolean loading) {
        this.loadingSpinner.setVisible(loading);
    }

    public void setStatus(@NonNull String status) {
        String[] splitStatus = status.split("\n", 2);
        if (splitStatus.length == 1) {
            this.statusText.setText(status);
            this.statusText.setToolTipText(null);
        } else {
            this.statusText.setText("<html>" + splitStatus[0] + " <span color=\"#6495ed\">🛈</span></html>");
            this.statusText.setToolTipText(splitStatus[1]);
        }

        this.repaint();
    }

}
