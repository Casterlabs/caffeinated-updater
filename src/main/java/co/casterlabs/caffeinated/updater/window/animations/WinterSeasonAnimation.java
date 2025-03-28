package co.casterlabs.caffeinated.updater.window.animations;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.concurrent.ThreadLocalRandom;

import co.casterlabs.caffeinated.updater.window.AnimationContext;

class WinterSeasonAnimation extends AbstractSnowflakeAnimation {
    private static final int FLAKE_COUNT = 60; // MUST BE DIVISIBLE BY 3

    private static final int MIN_FLAKE_DIAMETER = 2;
    private static final int MAX_FLAKE_DIAMETER = 5;

    private static final int HORIZONTAL_SPEED = 30; // Pixels/s
    private static final int VERTICAL_SPEED = 40; // Pixels/s

    public WinterSeasonAnimation(AnimationContext animationContext) {
        super(animationContext, FLAKE_COUNT);
    }

    @Override
    protected boolean isOutOfBounds(Snowflake snowflake) {
        return snowflake.x > maxBoundsX || snowflake.y > maxBoundsY;
    }

    @Override
    protected void resetSnowflake(Snowflake snowflake) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        snowflake.size = random.nextInt(MIN_FLAKE_DIAMETER, MAX_FLAKE_DIAMETER + 1); // Inclusive.

        snowflake.x = random.nextInt(minBoundsX, maxBoundsX);
        snowflake.y = -snowflake.size; // Above the top.
        snowflake.vx = HORIZONTAL_SPEED;
        snowflake.vy = VERTICAL_SPEED * snowflake.size; // Proportional to size, Pixels/s
    }

    @Override
    protected void paintSnowflake(Graphics2D g2d, Snowflake snowflake) {
        Ellipse2D.Double circle = new Ellipse2D.Double(
            snowflake.x, snowflake.y,
            snowflake.size, snowflake.size
        );
        g2d.fill(circle);
    }

}
