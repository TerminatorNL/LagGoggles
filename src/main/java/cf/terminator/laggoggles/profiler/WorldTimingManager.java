package cf.terminator.laggoggles.profiler;

import net.minecraft.util.math.BlockPos;

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

    public class WorldData {
        private HashMap<BlockPos, AtomicLong> blockTimes = new HashMap<>();
        private HashMap<UUID,    AtomicLong> entityTimes = new HashMap<>();

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
}
