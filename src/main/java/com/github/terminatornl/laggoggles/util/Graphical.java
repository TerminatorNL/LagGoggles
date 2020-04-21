package com.github.terminatornl.laggoggles.util;

import com.github.terminatornl.laggoggles.client.ClientConfig;

public class Graphical {

    public static final String mu = "\u00B5";

    public static String formatClassName(String in){
        return in.startsWith("class ") ? in.substring(6) : in;
    }

    public static final int RED_CHANNEL   = 0;
    public static final int GREEN_CHANNEL = 1;
    public static final int BLUE_CHANNEL  = 2;

    public static double[] heatToColor(double heat){
        return ClientConfig.COLORS.heatToColor(heat);
    }

    public static int RGBtoInt(double[] rgb){
        int R = (int) (rgb[0] * 255);
        R = (R << 8) + (int) (rgb[1] * 255);
        return (R << 8) + (int) (rgb[2] * 255);
    }
}
