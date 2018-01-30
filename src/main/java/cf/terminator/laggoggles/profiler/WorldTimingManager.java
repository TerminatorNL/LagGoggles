package cf.terminator.laggoggles.profiler;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class WorldTimingManager {
    private HashMap<Integer, WorldData> TIMINGS = new HashMap<>();

    public HashMap<Integer, WorldData> getTimings(){
        return new HashMap<>(TIMINGS);
    }

    public void addBlockTime(int dim, BlockPos pos, long time){
        if(TIMINGS.containsKey(dim) == false){
            WorldData data = new WorldData();
            data.addBlockTime(pos, time);
            TIMINGS.put(dim, data);
        }else{
            TIMINGS.get(dim).addBlockTime(pos, time);
        }
    }

    public void addEntityTime(int dim, UUID uuid, long time){
        if(TIMINGS.containsKey(dim) == false){
            WorldData data = new WorldData();
            data.addEntityTime(uuid, time);
            TIMINGS.put(dim, data);
        }else{
            TIMINGS.get(dim).addEntityTime(uuid, time);
        }
    }

    public void addEventTime(String listener, Event event, long time){
        if(TIMINGS.containsKey(0) == false){
            WorldData data = new WorldData();
            data.addEventTime(listener, event, time);
            TIMINGS.put(0, data);
        }else{
            TIMINGS.get(0).addEventTime(listener, event, time);
        }
    }

    public class WorldData {
        private HashMap<BlockPos,AtomicLong> blockTimes   = new HashMap<>();
        private HashMap<UUID,AtomicLong>     entityTimes  = new HashMap<>();
        private HashMap<String,EventTimings> eventTimes   = new HashMap<>();

        public void addBlockTime(BlockPos pos, long time){
            if(blockTimes.containsKey(pos) == false){
                blockTimes.put(pos, new AtomicLong(time));
            }else{
                blockTimes.get(pos).addAndGet(time);
            }
        }

        public void addEntityTime(UUID entity, long time){
            if(entityTimes.containsKey(entity) == false){
                entityTimes.put(entity, new AtomicLong(time));
            }else{
                entityTimes.get(entity).addAndGet(time);
            }
        }

        public void addEventTime(String listener, Event event, long time){
            if(eventTimes.containsKey(listener) == false){
                EventTimings.ThreadType type = EventTimings.ThreadType.ASYNC;
                if(FMLCommonHandler.instance().getMinecraftServerInstance().isCallingFromMinecraftThread()){
                    type = EventTimings.ThreadType.SERVER;
                }else if(Minecraft.getMinecraft().isCallingFromMinecraftThread()){
                    type = EventTimings.ThreadType.CLIENT;
                }
                eventTimes.put(listener, new EventTimings(time, listener, event.getClass(), type));
            }else{
                eventTimes.get(listener).addAndGet(time);
            }
        }

        public TreeMap<BlockPos, Long> getBlockTimes(){
            TreeMap<BlockPos, Long> data = new TreeMap<>();
            for(Map.Entry<BlockPos, AtomicLong> entry : blockTimes.entrySet()){
                data.put(entry.getKey(), entry.getValue().longValue());
            }
            return data;
        }

        public TreeMap<UUID, Long> getEntityTimes(){
            TreeMap<UUID, Long> data = new TreeMap<>();
            for(Map.Entry<UUID, AtomicLong> entry : entityTimes.entrySet()){
                data.put(entry.getKey(), entry.getValue().longValue());
            }
            return data;
        }

        public HashMap<String, EventTimings> getEventTimes(){
            return new HashMap<>(eventTimes);
        }
    }

    public static class EventTimings extends AtomicLong{

        public enum ThreadType{
            SERVER,
            CLIENT,
            ASYNC
        }

        public final String listener;
        public final ThreadType threadType;
        public final Class eventClass;

        EventTimings(long val, String listener, Class eventClass, ThreadType threadType){
            super(val);
            this.listener = listener;
            this.threadType = threadType;
            this.eventClass = eventClass;
        }

        @Override
        public int hashCode(){
            return listener.hashCode();
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof EventTimings == false){
                return false;
            }
            EventTimings other = ((EventTimings) o);
            return listener.equals(other.listener) &&
                    threadType == other.threadType &&
                    eventClass.equals(other.eventClass);
        }
    }
}
