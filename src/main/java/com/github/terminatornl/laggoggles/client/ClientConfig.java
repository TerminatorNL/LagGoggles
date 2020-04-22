package com.github.terminatornl.laggoggles.client;

import com.github.terminatornl.laggoggles.Main;
import com.github.terminatornl.laggoggles.util.ColorBlindMode;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

@Config(modid = Main.MODID_LOWER, name = Main.MODID + "-client")
public class ClientConfig {

    @Config.Comment("Define the number of microseconds at which an object is marked with a deep red colour for WORLD lag.")
    public static int GRADIENT_MAXED_OUT_AT_MICROSECONDS = 25;

    @Config.Comment("Define the number of microseconds at which an object is marked with a deep red colour for FPS lag.")
    public static int GRADIENT_MAXED_OUT_AT_NANOSECONDS_FPS = 50000;

    @Config.Comment("What is the minimum amount of microseconds required before an object is tracked in the client?\n" +
                    "This is only for WORLD lag.\n" +
                    "This also affects the analyze results window")
    public static int MINIMUM_AMOUNT_OF_MICROSECONDS_THRESHOLD = 1;

    @Config.Comment("If you're colorblind, change this to fit your needs.\n" +
                    "Available options:\n" +
                    "- GREEN_TO_RED\n" +
                    "- BLUE_TO_RED\n" +
                    "- GREEN_TO_BLUE")
    public static ColorBlindMode COLORS = ColorBlindMode.GREEN_TO_RED;

    @Mod.EventBusSubscriber
    public static class ConfigurationHolder {

        public static MethodHandle findFieldGetter(Class<?> clazz, String... fieldNames) {
            final Field field = ReflectionHelper.findField(clazz, fieldNames);

            try {
                return MethodHandles.lookup().unreflectGetter(field);
            } catch (IllegalAccessException e) {
                throw new ReflectionHelper.UnableToAccessFieldException(fieldNames, e);
            }
        }
        private static final MethodHandle CONFIGS_GETTER = findFieldGetter(ConfigManager.class, "CONFIGS");
        private static Configuration configuration;
        public static Configuration getConfiguration() {
            if (configuration == null) {
                try {
                    final String fileName = Main.MODID + "-client.cfg";

                    @SuppressWarnings("unchecked")
                    final Map<String, Configuration> configsMap = (Map<String, Configuration>) CONFIGS_GETTER.invokeExact();

                    final Optional<Map.Entry<String, Configuration>> entryOptional = configsMap.entrySet().stream()
                            .filter(entry -> fileName.equals(new File(entry.getKey()).getName()))
                            .findFirst();

                    if (entryOptional.isPresent()) {
                        configuration = entryOptional.get().getValue();
                    }
                } catch (Throwable e) {
                    Main.LOGGER.error("Failed to get Configuration instance", e);
                }
            }

            return configuration;
        }

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Main.MODID_LOWER)) {
                ConfigManager.load(Main.MODID_LOWER, Config.Type.INSTANCE);
            }
        }
    }
}
