package co.casterlabs.caffeinated.updater.window.animations;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import co.casterlabs.caffeinated.updater.util.FileUtil;
import co.casterlabs.caffeinated.updater.window.AnimationContext;
import lombok.SneakyThrows;

// https://www.city.semboku.akita.jp/en/sightseeing/spot/06_kamifuusen.html
// https://en.wikipedia.org/wiki/Kamif%C5%ABsen
class KamifusenAnimation extends AbstractSnowflakeAnimation {
    private static final int FLAKE_COUNT = 9; // MUST BE DIVISIBLE BY 3

    private static final int HORIZONTAL_SPEED = 25; // Pixels/s
    private static final int VERTICAL_SPEED = -35; // Pixels/s

    private static final int FLAKE_SIZE = 24;

    private BufferedImage image;

    @SneakyThrows
    public KamifusenAnimation(AnimationContext animationContext) {
        super(animationContext, FLAKE_COUNT);
        this.image = ImageIO.read(FileUtil.loadResourceAsUrl("assets/animation/floating_lantern.png"));
    }

    @Override
    protected boolean isOutOfBounds(Snowflake snowflake) {
        return snowflake.x > maxBoundsX || snowflake.y < minBoundsY - FLAKE_SIZE;
    }

    @Override
    protected void resetSnowflake(Snowflake snowflake) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        snowflake.size = FLAKE_SIZE;

        snowflake.x = random.nextInt(minBoundsX, maxBoundsX);
        snowflake.y = maxBoundsY;
        snowflake.vx = HORIZONTAL_SPEED;
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
