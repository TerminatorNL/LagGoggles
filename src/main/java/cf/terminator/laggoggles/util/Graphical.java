package cf.terminator.laggoggles.util;

public class Graphical {

    public static final String mu = "\u00B5";

    public static String formatClassName(String in){
        return in.startsWith("class ") ? in.substring(6) : in;
    }

    public static double[] heatToColor(double heat){
        double[] map = new double[3];
        map[2] = 0;

        if(heat < 50){
            map[0] = (heat / 50);
            map[1] = 1;
            return map;
        }else if(heat == 50){
            map[0] = 1;
            map[1] = 1;
            return map;
        }else{
            map[0] = 1;
            map[1] = 1 - ((heat-50) / 50);
            return map;
        }
    }

    public static int RGBtoInt(double[] rgb){
        int R = (int) (rgb[0] * 255);
        R = (R << 8) + (int) (rgb[1] * 255);
        return (R << 8) + (int) (rgb[2] * 255);
    }
}
