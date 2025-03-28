package co.casterlabs.caffeinated.updater.window.animations;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.concurrent.ThreadLocalRandom;

import co.casterlabs.caffeinated.updater.window.AnimationContext;
import co.casterlabs.caffeinated.updater.window.UpdaterDialog;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

abstract class AbstractSnowflakeAnimation extends AbstractDialogAnimation {
    protected static final int minBoundsX = 0;
    protected static final int minBoundsY = 0;
    protected static final int maxBoundsX = UpdaterDialog.WIDTH;
    protected static final int maxBoundsY = UpdaterDialog.HEIGHT;

    private final int layerSnowflakeCount;

    private final Snowflake[] snowflakes;

    /**
     * @param snowflakeCount Must be divisible by 3!
     */
    public AbstractSnowflakeAnimation(AnimationContext animationContext, int snowflakeCount) {
        this.layerSnowflakeCount = snowflakeCount / 3;

        this.snowflakes = new Snowflake[snowflakeCount];

        // Randomize the positions
        for (int snowflakeId = 0; snowflakeId < snowflakeCount; snowflakeId++) {
            Snowflake snowflake = this.snowflakes[snowflakeId] = new Snowflake(snowflakeId);

            this.resetSnowflake(snowflake);

            // Specially set the y for our first render.
            int y = ThreadLocalRandom.current().nextInt(minBoundsY, maxBoundsY);
            snowflake.y = y;
        }

        animationContext.toTick
            .add((deltaTime) -> {
                for (int snowflakeId = 0; snowflakeId < snowflakeCount; snowflakeId++) {
                    this.moveSnowflake(this.snowflakes[snowflakeId], deltaTime);
                }
            });
    }

    protected abstract boolean isOutOfBounds(Snowflake snowflake);

    protected abstract void resetSnowflake(Snowflake snowflake);

    protected abstract void paintSnowflake(Graphics2D g2d, Snowflake snowflake);

    @Override
    public void paintOnBackground(Graphics2D g2d) {
        this.paint(g2d, 0, this.layerSnowflakeCount);
    }

    @Override
    public void paintOverBackground(Graphics2D g2d) {
        this.paint(g2d, this.layerSnowflakeCount, this.layerSnowflakeCount * 2);
    }

    @Override
    public void paintOnForeground(Graphics2D g2d) {
        this.paint(g2d, this.layerSnowflakeCount * 2, this.layerSnowflakeCount * 3);
    }

    // The layering system gives the animation some depth.
    // Just a cool little thing, I guess.
    private void paint(Graphics2D g2d, int layerStart, int layerEnd) {
        Color lastColor = g2d.getColor();

        g2d.setColor(Color.WHITE);

        // Loop over the snowflakes on this layer, paint their location, and move them.
        for (int snowflakeId = layerStart; snowflakeId < layerEnd; snowflakeId++) {
            Snowflake snowflake = this.snowflakes[snowflakeId];
            this.paintSnowflake(g2d, snowflake);
        }

        // Restore the color, just in case it's needed again.
        g2d.setColor(lastColor);
    }

    private void moveSnowflake(Snowflake snowflake, double deltaTime) {
        snowflake.x += snowflake.vx * deltaTime;
        snowflake.y += snowflake.vy * deltaTime;

//        System.out.println(deltaTime);
//        System.out.println(snowflake);

        // Check that the snowflake is in bounds and reset it if necessary.
        if (this.isOutOfBounds(snowflake)) {
            this.resetSnowflake(snowflake);
        }
    }

    @ToString
    @RequiredArgsConstructor
    protected class Snowflake {
        public final int id;

        public double x;
        public double y;

        /**
         * pixels/s
         */
        public double vx;
        /**
         * pixels/s
         */
        public double vy;

        public int size;
    }

}
