package cf.terminator.laggoggles.util;

import static cf.terminator.laggoggles.util.Graphical.*;

public enum ColorBlindMode {

    GREEN_TO_RED(GREEN_CHANNEL, RED_CHANNEL, BLUE_CHANNEL),
    BLUE_TO_RED(BLUE_CHANNEL, RED_CHANNEL, GREEN_CHANNEL),
    GREEN_TO_BLUE(GREEN_CHANNEL, BLUE_CHANNEL, RED_CHANNEL);

    private final int bad;
    private final int good;
    private final int neutral;

    ColorBlindMode(int good, int bad, int neutral){
        this.bad = bad;
        this.good = good;
        this.neutral = neutral;
    }

    public double[] heatToColor(double heat){
        double[] rgb = new double[3];
        rgb[neutral] = 0;

        if(heat < 50){
            rgb[bad] = (heat / 50);
            rgb[good] = 1;
            return rgb;
        }else if(heat == 50){
            rgb[bad] = 1;
            rgb[good] = 1;
            return rgb;
        }else{
            rgb[bad] = 1;
            rgb[good] = 1 - ((heat-50) / 50);
            return rgb;
        }
    }

}
