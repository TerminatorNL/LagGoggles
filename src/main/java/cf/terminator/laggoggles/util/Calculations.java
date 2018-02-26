package cf.terminator.laggoggles.util;

import cf.terminator.laggoggles.client.ClientConfig;
import cf.terminator.laggoggles.profiler.ProfileResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static cf.terminator.laggoggles.util.Graphical.mu;

@SideOnly(Side.CLIENT)
public class Calculations {

    public static final double NANOS_IN_A_TICK = 50000000;

    public static double heat(long nanos, ProfileResult result) {
        return Math.min((muPerTick(nanos, result) / ClientConfig.GRADIENT_MAXED_OUT_AT_MICROSECONDS) * 100, 100);
    }

    public static double heatNF(long nanos, ProfileResult result) {
        return Math.min(((double) nanos/(double) result.getTotalFrames() / (double) ClientConfig.GRADIENT_MAXED_OUT_AT_NANOSECONDS_FPS) * 100D, 100);
    }

    public static String NFString(long nanos, long frames) {
        long nf = nanos/frames;
        if(nf > 1000) {
            return nf/1000+"k ns/F";
        }else{
            return nf +" ns/F";
        }
    }

    public static String NFStringSimple(long nanos, long frames) {
        return nanos/frames + " ns/F";
    }

    public static String tickPercent(long nanos, ProfileResult result) {
        if(result == null || result.getTickCount() == 0){
            return "?";
        }
        return Math.floor((nanos / result.getTickCount()) / NANOS_IN_A_TICK * 10000) / 100d + "%";
    }

    public static String nfPercent(long nanos, ProfileResult result) {
        if(result == null || result.getTotalFrames() == 0){
            return "?";
        }
        return Math.floor((nanos / (double) result.getTotalTime()) * 10000D) / 100D + "%";
    }

    public static double muPerTick(long nanos, ProfileResult result) {
        if(result == null){
            return 0;
        }
        return (nanos / result.getTickCount()) / 1000;
    }

    public static double muPerTickCustomTotals(long nanos, long totalTicks) {
        return (nanos / totalTicks) / 1000;
    }

    public static String muPerTickString(long nanos, ProfileResult result) {
        if(result == null){
            return "?";
        }
        return Double.valueOf((nanos / result.getTickCount()) / 1000).intValue() + " " + mu + "s/t";
    }

}