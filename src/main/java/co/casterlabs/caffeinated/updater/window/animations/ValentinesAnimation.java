package co.casterlabs.caffeinated.updater.window.animations;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import co.casterlabs.caffeinated.updater.window.AnimationContext;
import lombok.SneakyThrows;

class ValentinesAnimation extends AbstractSnowflakeAnimation {
    private static final int FLAKE_COUNT = 27; // MUST BE DIVISIBLE BY 3

    private static final int HORIZONTAL_SPEED = 10; // Pixels/s
    private static final int VERTICAL_SPEED = 100; // Pixels/s

    private static final int FLAKE_SIZE = 24;

    private BufferedImage image;

    @SneakyThrows
    public ValentinesAnimation(AnimationContext animationContext) {
        super(animationContext, FLAKE_COUNT);
        this.image = ImageIO.read(FileUtil.loadResourceAsUrl("assets/animation/heart.png"));
    }

    @Override
    protected boolean isOutOfBounds(Snowflake snowflake) {
        return snowflake.x > maxBoundsX || snowflake.y > maxBoundsY;
    }

    @Override
    protected void resetSnowflake(Snowflake snowflake) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        snowflake.size = FLAKE_SIZE;

        snowflake.x = random.nextInt(minBoundsX, maxBoundsX);
        snowflake.y = minBoundsY - snowflake.size;
        snowflake.vx = HORIZONTAL_SPEED / random.nextDouble(-1, 1);
        snowflake.vy = VERTICAL_SPEED;
    }

    @Override
    protected void paintSnowflake(Graphics2D g2d, Snowflake snowflake) {
        int x = (int) snowflake.x;
        int y = (int) snowflake.y;
        int size = snowflake.size;

        g2d.drawImage(this.image, x, y, size, size, null);
    }

}
