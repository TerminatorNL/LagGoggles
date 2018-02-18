package cf.terminator.laggoggles.profiler;

import cf.terminator.laggoggles.util.ThreadChecker;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class WorldTimingManager {
    private Map<Integer, WorldData> TIMINGS = Collections.synchronizedMap(new HashMap<>());

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
        private Map<BlockPos,AtomicLong>     blockTimes   = Collections.synchronizedMap(new HashMap<>());
        private Map<UUID,AtomicLong>         entityTimes  = Collections.synchronizedMap(new HashMap<>());
        private Map<EventTimings,AtomicLong> eventTimes   = Collections.synchronizedMap(new HashMap<>());

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
            EventTimings timings = new EventTimings(listener, event.getClass(), ThreadChecker.getThreadType());
            if(eventTimes.containsKey(timings) == false){
                eventTimes.put(timings, new AtomicLong(time));
            }else{
                eventTimes.get(timings).addAndGet(time);
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

        public HashMap<EventTimings, AtomicLong> getEventTimes(){
            return new HashMap<>(eventTimes);
        }
    }

    public static class EventTimings{

        public enum ThreadType{
            SERVER,
            CLIENT,
            ASYNC
        }

        public final String listener;
        public final ThreadType threadType;
        public final Class eventClass;

        EventTimings(String listener, Class eventClass, ThreadType threadType){
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
