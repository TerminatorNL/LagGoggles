package cf.terminator.laggoggles.profiler;

import cf.terminator.laggoggles.util.ThreadChecker;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class TimingManager {
    private Map<Integer, WorldData> WORLD_TIMINGS = Collections.synchronizedMap(new HashMap<>());
    private Map<EventTimings,AtomicLong> EVENT_TIMES = Collections.synchronizedMap(new HashMap<>());
    private GuiTimings GUI_TIMINGS = new GuiTimings();

    public HashMap<Integer, WorldData> getTimings(){
        return new HashMap<>(WORLD_TIMINGS);
    }

    public HashMap<EventTimings, AtomicLong> getEventTimings(){
        return new HashMap<>(EVENT_TIMES);
    }

    public TreeMap<BlockPos, Long> getGuiBlockTimings(){
        return GUI_TIMINGS.getTileEntityTimes();
    }

    public TreeMap<UUID, Long> getGuiEntityTimings(){
        return GUI_TIMINGS.getEntityTimes();
    }

    public void addBlockTime(int dim, BlockPos pos, long time){
        if(WORLD_TIMINGS.containsKey(dim) == false){
            WorldData data = new WorldData();
            data.addBlockTime(pos, time);
            WORLD_TIMINGS.put(dim, data);
        }else{
            WORLD_TIMINGS.get(dim).addBlockTime(pos, time);
        }
    }

    public void addEntityTime(int dim, UUID uuid, long time){
        if(WORLD_TIMINGS.containsKey(dim) == false){
            WorldData data = new WorldData();
            data.addEntityTime(uuid, time);
            WORLD_TIMINGS.put(dim, data);
        }else{
            WORLD_TIMINGS.get(dim).addEntityTime(uuid, time);
        }
    }

    public void addEventTime(String listener, Event event, long time){
        EventTimings timings = new EventTimings(listener, event.getClass(), ThreadChecker.getThreadType());
        if(EVENT_TIMES.containsKey(timings) == false){
            EVENT_TIMES.put(timings, new AtomicLong(time));
        }else{
            EVENT_TIMES.get(timings).addAndGet(time);
        }
    }

    public void addGuiEntityTime(UUID uuid, long time){
        GUI_TIMINGS.addEntityTime(uuid, time);
    }

    public void addGuiBlockTime(BlockPos pos, long time){
        GUI_TIMINGS.addGuiBlockTime(pos, time);
    }

    public static class WorldData {
        private Map<BlockPos,AtomicLong>     blockTimes   = Collections.synchronizedMap(new HashMap<>());
        private Map<UUID,AtomicLong>         entityTimes  = Collections.synchronizedMap(new HashMap<>());

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
    }

    public static class GuiTimings{
        private Map<UUID, AtomicLong> ENTITY_TIMINGS = Collections.synchronizedMap(new HashMap<>());
        private Map<BlockPos, AtomicLong> TILE_TIMINGS = Collections.synchronizedMap(new HashMap<>());

        public void addGuiBlockTime(BlockPos pos, long time){
            if(TILE_TIMINGS.containsKey(pos) == false){
                TILE_TIMINGS.put(pos, new AtomicLong(time));
            }else{
                TILE_TIMINGS.get(pos).getAndAdd(time);
            }
        }

        public void addEntityTime(UUID uuid, long time){
            if(ENTITY_TIMINGS.containsKey(uuid) == false){
                ENTITY_TIMINGS.put(uuid, new AtomicLong(time));
            }else{
                ENTITY_TIMINGS.get(uuid).getAndAdd(time);
            }
        }

        public TreeMap<UUID, Long> getEntityTimes(){
            TreeMap<UUID, Long> data = new TreeMap<>();
            for(Map.Entry<UUID, AtomicLong> entry : ENTITY_TIMINGS.entrySet()){
                data.put(entry.getKey(), entry.getValue().longValue());
            }
            return data;
        }

        public TreeMap<BlockPos, Long> getTileEntityTimes(){
            TreeMap<BlockPos, Long> data = new TreeMap<>();
            for(Map.Entry<BlockPos, AtomicLong> entry : TILE_TIMINGS.entrySet()){
                data.put(entry.getKey(), entry.getValue().longValue());
            }
            return data;
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
