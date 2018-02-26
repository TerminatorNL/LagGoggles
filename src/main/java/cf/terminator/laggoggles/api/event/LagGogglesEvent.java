package cf.terminator.laggoggles.api.event;

import cf.terminator.laggoggles.profiler.ProfileResult;
import net.minecraftforge.fml.common.eventhandler.Event;

public class LagGogglesEvent extends Event {

    private final ProfileResult profileResult;

    /**
     * The base event. Use this if you need to catch any profile result.
     * @param result The profile result
     */
    public LagGogglesEvent(ProfileResult result){
        this.profileResult = result;
    }

    public ProfileResult getProfileResult() {
        return profileResult;
    }

    /**
     * When the client receives a result, this event is created.
     * It runs on connection thread, meaning that it
     * doesn't run on any of the minecraft threads. (Async)
     *
     * If you need to perform any action based on this result, make sure
     * that you do it in the Minecraft thread, and NOT this one.
     *
     * Fired on the MinecraftForge.EVENT_BUS.
     */
    public static class ReceivedFromServer extends LagGogglesEvent{
        public ReceivedFromServer(ProfileResult result){
            super(result);
        }
    }

    /**
     * When the profiler is finished, this event is created.
     * It runs on the thread that created the profiler, meaning that it
     * doesn't run on any of the minecraft threads. (Async)
     *
     * If you need to perform any action based on this result, make sure
     * that you do it in the Minecraft thread, and NOT this one.
     *
     * Fired on the MinecraftForge.EVENT_BUS.
     */
    public static class LocalResult extends LagGogglesEvent{
        public LocalResult(ProfileResult result){
            super(result);
        }
    }
}
