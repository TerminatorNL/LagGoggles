package cf.terminator.laggoggles.mixinhelper;

import cf.terminator.laggoggles.Main;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;

public class MixinConfigPlugin implements IMixinConfigPlugin{

    public static boolean spongePresent = false;
    public static String spongeForgeVersion = null;
    public static boolean hasClientClasses = true;
    public static boolean MIXIN_CONFIG_PLUGIN_WAS_LOADED = false;
    public static HashMap<String, String> MIXINS_TO_LOAD = new HashMap<>();
    public static final Logger LOGGER = LogManager.getLogger("LagGoggles-Boot");

    /*
     * Snippet straight out of SpongeForges' source.
     */
    public static boolean isProductionEnvironment(){
        return System.getProperty("net.minecraftforge.gradle.GradleStart.csvDir") == null;
    }

    private void setup(){
        MixinEnvironment.getDefaultEnvironment().setOption(MixinEnvironment.Option.DEBUG_ALL, true);
        if(MIXIN_CONFIG_PLUGIN_WAS_LOADED == false){
            File thisJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getFile());
            LOGGER.info("I am located here: " + thisJar);
            LOGGER.info("I am designed for Forge version: ${forge_version}");
            MIXIN_CONFIG_PLUGIN_WAS_LOADED = true;

            for(StackTraceElement element : Thread.currentThread().getStackTrace()){
                if(element.getClassName().equals("net.minecraftforge.fml.relauncher.ServerLaunchWrapper")){
                    hasClientClasses = false;
                    break;
                }else if(element.getClassName().equals("GradleStartServer")){
                    hasClientClasses = false;
                    break;
                }
            }

            if(getClass().getClassLoader().getResource("net/minecraft/client/main/Main.class") == null){
                hasClientClasses = false;
            }

            if(hasClientClasses){
                LOGGER.info("Loading client classes");
            }else{
                LOGGER.info("Loading server classes");
            }

            try {
                Class spongeForgeClass = Class.forName("org.spongepowered.mod.SpongeCoremod", true, getClass().getClassLoader());
                LOGGER.info("SpongeForge is present!");
                LOGGER.info("I am designed for SpongeForge version: ${spongeforge_version}");
                spongePresent = true;

                File spongeForgeFile = new File(URLDecoder.decode(spongeForgeClass.getProtectionDomain().getCodeSource().getLocation().getFile(),"UTF-8"));
                JarInputStream SpongeForgeStream = new JarInputStream(new FileInputStream(spongeForgeFile));
                Attributes spongeForgeMainAttributes = SpongeForgeStream.getManifest().getMainAttributes();
                for(Map.Entry<Object, Object> e : spongeForgeMainAttributes.entrySet()){
                    if("Implementation-Version".equals(String.valueOf(e.getKey()))){
                        spongeForgeVersion = String.valueOf(e.getValue());
                    }
                }
                if(spongeForgeVersion == null){
                    LOGGER.warn("Unable to determine SpongeForge version. Use at your own risk.");
                }else{
                    if(spongeForgeVersion.equals("${spongeforge_version}")){
                        LOGGER.info("SpongeForge version is a match!");
                    }else{
                        LOGGER.warn("SpongeForge version is different than expected!");
                        LOGGER.warn("This could result in undefined behavior.");
                        LOGGER.warn("");
                        LOGGER.warn("Expected: '${spongeforge_version}', but you have: '" + spongeForgeVersion + "' installed.");
                    }
                }
            }catch (IOException ignored){
                LOGGER.warn("Failed to get your currently active Sponge version.");
            } catch (ClassNotFoundException ignored) {
                LOGGER.info("SpongeForge is not present!");
                spongePresent = false;
                try{
                    Class.forName("org.spongepowered.asm.launch.MixinTweaker", false, getClass().getClassLoader());
                } catch (ClassNotFoundException ignored_1) {
                    LOGGER.info("Oh no! It looks like you also do not have Mixin installed. Please use the FAT version of LagGoggles.");
                    FMLCommonHandler.instance().exitJava(1, true);
                }
            }

            if(isProductionEnvironment()){
                LOGGER.info("We're running in a production environment.");
            }else{
                LOGGER.info("We're running in a deobfuscated environment.");
            }
        }
    }

    private boolean shouldApplyMixin(String mixin){
        if(spongePresent == true){
            switch (mixin){
                case "cf.terminator.laggoggles.mixin.MixinWorldServerForge":
                    return false;
            }
        }else{
            switch (mixin){
                case "cf.terminator.laggoggles.mixin.MixinWorldServerSponge":
                    return false;
            }
        }
        if(hasClientClasses == false){
            switch (mixin) {
                case "cf.terminator.laggoggles.mixin.MixinRenderManager":
                case "cf.terminator.laggoggles.mixin.MixinTileEntityRendererDispatcher":
                    return false;
            }
        }
        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {
        setup();
    }

    @Override
    public String getRefMapperConfig() {
        return "mixins.laggoggles.refmap.json";
    }


    @Override
    public boolean shouldApplyMixin(String target, String mixin) {
        boolean shouldApply = shouldApplyMixin(mixin);
        LOGGER.info((shouldApply ? "Enabling: " : "Skipping: ") + mixin);
        if(shouldApply){
            MIXINS_TO_LOAD.put(mixin, target);
        }
        return shouldApply;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override
    public List<String> getMixins() {
        List<String> list = new ArrayList<>();
        //list.add("");
        return list;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String mixin, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String target, ClassNode classNode, String mixin, IMixinInfo iMixinInfo) {
        LOGGER.info("Applied mixin: " + mixin);
        MIXINS_TO_LOAD.remove(mixin);
    }
}
