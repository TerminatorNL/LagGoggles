package cf.terminator.laggoggles;

import net.minecraftforge.common.config.Config;

@Config(modid = Main.MODID_LOWER)
public class ConfigData {

    @Config.Comment("Define the number of microseconds at which an entity is marked with a deep red colour")
    public static int GRADIENT_MAXED_OUT_AT_MICROSECONDS = 25;
}
