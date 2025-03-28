package co.casterlabs.caffeinated.updater.window.animations;

import java.awt.Graphics2D;
import java.util.Calendar;

import co.casterlabs.caffeinated.updater.window.AnimationContext;

/**
 * All paints are animation-safe.
 */
public abstract class AbstractDialogAnimation {

    public void paintOnForeground(Graphics2D g2d) {}

    // Above the background, below the UI elements (foreground)
    public void paintOverBackground(Graphics2D g2d) {}

    public void paintOnBackground(Graphics2D g2d) {}

    public boolean shouldShowCasterlabsBanner() {
        return true;
    }

    public String getIcon() {
        return "icon.png";
    }

    public static AbstractDialogAnimation getCurrentAnimation(AnimationContext animationContext) {
        AbstractDialogAnimation animation = new BlankAnimation();

        Calendar calendar = Calendar.getInstance();
        int calendarMonth = calendar.get(Calendar.MONTH);
        int calendarDate = calendar.get(Calendar.DATE);

        // Enable the winter season animation between NOV 25 - JAN 15.
        {
            boolean isDecember = calendarMonth == Calendar.DECEMBER;
            boolean isNovemberTimeframe = (calendarMonth == Calendar.NOVEMBER) && (calendarDate >= 25);
            boolean isJanuaryTimeframe = (calendarMonth == Calendar.JANUARY) && (calendarDate <= 15);

            if (isDecember || isNovemberTimeframe || isJanuaryTimeframe) {
                animation = new WinterSeasonAnimation(animationContext);
            }
        }

        // Enable the Halloween animation on OCT 31.
        {
            boolean isOctober = calendarMonth == Calendar.OCTOBER;
            boolean isTheThirtyFirst = calendarDate == 31;

            if (isOctober && isTheThirtyFirst) {
                animation = new HalloweenAnimation();
            }
        }

        // Enable the Kamihinokinai animation on FEB 10.
        {
            boolean isFeburay = calendarMonth == Calendar.FEBRUARY;
            boolean isTheTenth = calendarDate == 10;

            if (isFeburay && isTheTenth) {
                animation = new KamihinokinaiAnimation(animationContext);
            }
        }

        // Enable the Valentine's animation on FEB 14.
        {
            boolean isFeburay = calendarMonth == Calendar.FEBRUARY;
            boolean isTheFourteenth = calendarDate == 14;

            if (isFeburay && isTheFourteenth) {
                animation = new ValentinesAnimation(animationContext);
            }
        }

        // Enable the Pride month animation during June.
//        {
//            boolean isJune = calendarMonth == Calendar.JUNE;
//
//            if (isJune) {
//                animation = new PrideAnimation();
//            }
//        }

        return animation;
    }

}
