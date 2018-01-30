package cf.terminator.laggoggles.util;

import cf.terminator.laggoggles.client.ClientConfig;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static cf.terminator.laggoggles.client.ClientProxy.LAST_SCAN_RESULT;
import static cf.terminator.laggoggles.util.Graphical.mu;

@SideOnly(Side.CLIENT)
public class Calculations {

    public static final double NANOS_IN_A_TICK = 50000000;

    public static double heat(long nanos) {
        return Math.min((muPerTick(nanos) / ClientConfig.GRADIENT_MAXED_OUT_AT_MICROSECONDS) * 100, 100);
    }

    public static String tickPercent(long nanos) {
        return Math.floor((nanos / LAST_SCAN_RESULT.TOTAL_TICKS) / NANOS_IN_A_TICK * 10000) / 100d + "%";
    }

    public static double muPerTick(long nanos) {
        return (nanos / LAST_SCAN_RESULT.TOTAL_TICKS) / 1000;
    }

    public static double muPerTickCustomTotals(long nanos, long totalTicks) {
        return (nanos / totalTicks) / 1000;
    }

    public static String muPerTickString(long nanos) {
        return Double.valueOf((nanos / LAST_SCAN_RESULT.TOTAL_TICKS) / 1000).intValue() + " " + mu + "s/t";
    }

}