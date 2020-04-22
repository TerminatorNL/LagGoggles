package com.github.terminatornl.laggoggles.api;

import com.github.terminatornl.laggoggles.profiler.ProfileManager;
import com.github.terminatornl.laggoggles.profiler.ProfileResult;
import com.github.terminatornl.laggoggles.profiler.ScanType;
import net.minecraft.command.ICommandSender;

import javax.annotation.Nullable;

import static com.github.terminatornl.laggoggles.profiler.ProfileManager.LAST_PROFILE_RESULT;

@SuppressWarnings({"WeakerAccess","unused"})
public class Profiler {

    /**
     * Checks if the profiler is already running.
     * if this value returns true, you shouldn't start profiling.
     */
    public static boolean isProfiling(){
        return ProfileManager.PROFILER_ENABLED_UPDATE_SAFE;
    }

    /**
     * Checks if you can start the profiler.
     * In future updates, complexity may increase.
     *
     * This method will update accordingly.
     */
    public static boolean canProfile(){
        return ProfileManager.PROFILER_ENABLED_UPDATE_SAFE == false;
    }

    /**
     * Starts the profiler, and runs it in THIS THREAD!.
     * This is a blocking method, and should NEVER EVER EVER
     * be ran on a minecraft thread. EVER!!!!
     *
     * @param seconds how many seconds to profile
     * @param type the profiling type, either WORLD or FPS
     * @return the result, after the profiler is done.
     * @throws IllegalStateException if the profiler is already running, you should use {@link #canProfile()} before doing this
     */
    public static ProfileResult runProfiler(int seconds, ScanType type, ICommandSender sender) throws IllegalStateException{
        return ProfileManager.runProfiler(seconds, type, sender);
    }

    /**
     * Gets the latest scan result from the profiler. This can be any scan, of any length started by anyone.
     *
     * @return the last scan result, or null, if no scan is performed yet
     */
    public static @Nullable ProfileResult getLatestResult(){
        return LAST_PROFILE_RESULT.get();
    }

}
