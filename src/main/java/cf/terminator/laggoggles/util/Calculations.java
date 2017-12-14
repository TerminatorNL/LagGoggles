package cf.terminator.laggoggles.util;

import cf.terminator.laggoggles.ConfigData;

import static cf.terminator.laggoggles.client.ClientProxy.LAST_SCAN_RESULT;
import static cf.terminator.laggoggles.util.Graphical.µ;

public class Calculations {

    public static final double NANOS_IN_A_TICK = 50000000;

    public static double heat(long nanos){
        return Math.min((µPerTick(nanos)/ ConfigData.GRADIENT_MAXED_OUT_AT_MICROSECONDS)*100, 100);
    }

    public static String tickPercent(long nanos){
        return (int) Math.floor((nanos/LAST_SCAN_RESULT.TOTAL_TICKS)/NANOS_IN_A_TICK*10000)/100 + "%";
    }

    public static double nanosPerTick(long nanos){
        return nanos/LAST_SCAN_RESULT.TOTAL_TICKS;
    }

    public static double µPerTick(long nanos){
        return (nanos/LAST_SCAN_RESULT.TOTAL_TICKS)/1000;
    }

    public static String µPerTickString(double nanos){
        return Double.valueOf((nanos/LAST_SCAN_RESULT.TOTAL_TICKS)/1000).intValue() + " " + µ + "s/t";
    }
}
