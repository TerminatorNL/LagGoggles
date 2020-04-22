package com.github.terminatornl.laggoggles.server;

import com.github.terminatornl.laggoggles.Main;
import com.github.terminatornl.laggoggles.util.Perms;
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

@Config(modid = Main.MODID_LOWER, name = Main.MODID + "-server")
public class ServerConfig {

    @Config.Comment("What's the permission level available to non-operators (Normal players)?\n" +
                    "Please note that this ONLY works on dedicated servers. If you're playing singleplayer or LAN, the FULL permission is used.\n" +
                    "Available permissions in ascending order are:\n" +
                    "   'NONE'  No permissions are granted, all functionality is denied.\n" +
                    "   'GET'   Allow getting the latest scan result, this will be stripped down to the player's surroundings\n" +
                    "   'START' Allow starting the profiler\n" +
                    "   'FULL'  All permissions are granted, teleporting to entities, blocks")
    public static Perms.Permission NON_OP_PERMISSION_LEVEL = Perms.Permission.START;

    @Config.Comment("Allow normal users to see event subscribers?")
    public static boolean ALLOW_NON_OPS_TO_SEE_EVENT_SUBSCRIBERS = false;

    @Config.Comment("If normal users can start the profiler, what is the maximum time in seconds?")
    public static int NON_OPS_MAX_PROFILE_TIME = 20;

    @Config.Comment("If normal users can start the profiler, what is the cool-down between requests in seconds?")
    public static int NON_OPS_PROFILE_COOL_DOWN_SECONDS = 120;

    @Config.Comment("What is the maximum HORIZONTAL range in blocks normal users can get results for?")
    public static double NON_OPS_MAX_HORIZONTAL_RANGE = 50;

    @Config.Comment("What is the maximum VERTICAL range in blocks normal users can get results for?")
    public static double NON_OPS_MAX_VERTICAL_RANGE = 20;

    @Config.Comment("From where should we range-limit blocks vertically for normal users?\n" +
                    "This will override the MAX_VERTICAL_RANGE when the block is above this Y level")
    public static int NON_OPS_WHITELIST_HEIGHT_ABOVE = 64;

    @Config.Comment("How often can normal users request the latest scan result in seconds?")
    public static int NON_OPS_REQUEST_LAST_SCAN_DATA_TIMEOUT = 30;

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
                    final String fileName = Main.MODID + "-server.cfg";

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
