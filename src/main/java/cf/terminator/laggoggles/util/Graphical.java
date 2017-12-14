package cf.terminator.laggoggles.util;

public class Graphical {

    public static final String µ = "\u00B5";

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

    public static int[] heatToColor(int heat){
        int[] map = new int[3];
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

    public static int RGBtoInt(int[] rgb){
        int R = rgb[0];
        R = (R << 8) + rgb[1];
        return (R << 8) + rgb[2];
    }

    public static int RGBtoInt(double[] rgb){
        int R = (int) rgb[0] * 255;
        R = (R << 8) + (int) rgb[1] * 255;
        return (R << 8) + (int) rgb[2] * 255;
    }
}
