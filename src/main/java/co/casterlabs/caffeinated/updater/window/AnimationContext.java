package co.casterlabs.caffeinated.updater.window;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.Window;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class AnimationContext implements Closeable {
    private volatile int refreshRate = 30;
    private volatile double frameInterval;
    private volatile long frameIntervalFloor;

    public final List<Animatable> toTick = new ArrayList<>();

    public boolean isAnimationFrame = false;

    private long lastAnimationMillis = 0;
    private long lastAnimationNanos = System.nanoTime();

    private boolean isClosed = false;

    private void recalculateIntervals() {
        this.frameInterval = 1000f / this.refreshRate;
        this.frameIntervalFloor = (long) Math.floor(this.frameInterval);
    }

    public AnimationContext(Window window) {
        this.recalculateIntervals();

        Thread refreshRateThread = new Thread(() -> {
            while (!this.isClosed) {
                GraphicsDevice gd = window.getGraphicsConfiguration().getDevice();
                int newRefreshRate = gd.getDisplayMode().getRefreshRate();
                if (newRefreshRate == DisplayMode.REFRESH_RATE_UNKNOWN) {
                    newRefreshRate = 30;
                }

                if (this.refreshRate != newRefreshRate) {
                    this.refreshRate = newRefreshRate;
                    FastLogger.logStatic("Refresh rate updated to %dhz", newRefreshRate);
                    this.recalculateIntervals();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            }
        });
        refreshRateThread.setName("Animation Thread - Refresh rate");
        refreshRateThread.setDaemon(true);
        refreshRateThread.start();

        Thread animationThread = new Thread(() -> {
            while (!this.isClosed) {
                this.waitToAnimate();
            }
        });
        animationThread.setName("Animation Thread");
        animationThread.setDaemon(true);
        animationThread.start();
    }

    private void waitToAnimate() {
        long timeSinceLastFrame = System.currentTimeMillis() - this.lastAnimationMillis;
        long timeToWait = this.frameIntervalFloor - timeSinceLastFrame;
        if (timeToWait > 0) {
            try {
                Thread.sleep(timeToWait);
            } catch (InterruptedException ignored) {}
        }

        if (!this.toTick.isEmpty()) {
            // Animate.
            this.isAnimationFrame = true;

            double deltaTime = (System.nanoTime() - this.lastAnimationNanos) / 1_000_000_000d;
            for (Animatable a : this.toTick.toArray(new Animatable[0])) {
                a.run(deltaTime);
            }

            this.lastAnimationMillis = System.currentTimeMillis();
            this.lastAnimationNanos = System.nanoTime();
            this.isAnimationFrame = false;
        }
    }

    @Override
    public void close() {
        this.isClosed = true;
    }

    @FunctionalInterface
    public static interface Animatable {

        /**
         * @param deltaTime the amount of seconds since the last frame.
         */
        public void run(double deltaTime);

    }

}
