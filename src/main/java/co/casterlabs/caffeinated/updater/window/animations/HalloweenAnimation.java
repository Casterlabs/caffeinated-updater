package co.casterlabs.caffeinated.updater.window.animations;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import lombok.SneakyThrows;

class HalloweenAnimation extends AbstractDialogAnimation {
    private BufferedImage image;

    @SneakyThrows
    public HalloweenAnimation() {
        this.image = ImageIO.read(FileUtil.loadResourceAsUrl("assets/animation/halloween.png"));
    }

    @Override
    public void paintOnBackground(Graphics2D g2d) {
        g2d.drawImage(this.image, 0, 0, null);
    }

}
