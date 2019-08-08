package cf.terminator.laggoggles.mixinhelper;

import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static cf.terminator.laggoggles.Main.LOGGER;
import static cf.terminator.laggoggles.mixinhelper.MixinConfigPlugin.MIXINS_TO_LOAD;

public class MixinValidator {

    public static void validate(){
        HashMap<String, String> FAILED_OR_UNLOADED_MIXINS = new HashMap<>(MIXINS_TO_LOAD);
        for(String target : new HashSet<>(FAILED_OR_UNLOADED_MIXINS.values())){
            try {
                LOGGER.info("Loading mixin target class: " + target);
                Class.forName(target);
            } catch (Exception e) {
                LOGGER.warn("Failed to load class: " + target + ". This is required to apply mixins!");
                e.printStackTrace();
            }
        }
        if(MIXINS_TO_LOAD.size() > 0){
            LOGGER.fatal("Not all required mixins have been applied!");
            LOGGER.fatal("To prevent you from wasting your time, the process has ended.");
            LOGGER.fatal("");
            LOGGER.fatal("Required mixins that have not been applied:");
            for(Map.Entry<String, String> entry : MIXINS_TO_LOAD.entrySet()){
                LOGGER.fatal("- " + entry.getKey() + " targeting: " + entry.getValue());
            }
            LOGGER.fatal("");
            LOGGER.fatal("This means that LagGoggles will not function properly.");
            LOGGER.fatal("Make sure your versions are correct for Forge as well as SpongeForge.");
            LOGGER.fatal("");

            ClassLoader thisLoader = MixinValidator.class.getClassLoader();
            if (thisLoader instanceof LaunchClassLoader == false){
                LOGGER.fatal("MixinValidator.class was NOT loaded by the expected class loader. Loader: " + thisLoader);
                return;
            }
            LaunchClassLoader loader = (LaunchClassLoader) thisLoader;

            for(String target : new HashSet<>(FAILED_OR_UNLOADED_MIXINS.values())){
                try {

                } catch (Exception e) {
                }
            }

            FMLCommonHandler.instance().exitJava(1, false);
        }else{
            LOGGER.info("All mixins have been applied. If they were not overridden by another mod, everything should work.");
        }
    }
}
