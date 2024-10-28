package co.casterlabs.caffeinated.updater.window.animations;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import lombok.SneakyThrows;

public class PrideAnimation extends DialogAnimation {
    private BufferedImage image;

    @SneakyThrows
    public PrideAnimation() {
        this.image = ImageIO.read(FileUtil.loadResourceAsUrl("assets/animation/pride.png"));
    }

    @Override
    public void paintOnBackground(Graphics2D g2d) {
        g2d.drawImage(this.image, 0, 0, null);
    }

    @Override
    public boolean shouldShowCasterlabsBanner() {
        return false;
    }

    @Override
    public String getIcon() {
        return "pride.png";
    }

}
