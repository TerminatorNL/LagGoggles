package cf.terminator.laggoggles.packet;

import cf.terminator.laggoggles.profiler.WorldTimingManager;
import cf.terminator.laggoggles.util.Coder;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import static cf.terminator.laggoggles.util.Graphical.formatClassName;

public class SPacketScanResult implements IMessage{

    public SPacketScanResult(){}

    public ArrayList<EntityData> DATA = new ArrayList<>();
    public long TOTAL_TICKS = 0L;
    public boolean hasMore = false;
    public long endTime;

    @Override
    public void fromBytes(ByteBuf buf) {
        TOTAL_TICKS = buf.readLong();
        hasMore = buf.readBoolean();
        endTime = buf.readLong();
        int size = buf.readInt();
        for(int i=0; i<size; i++){
            EntityData data = new EntityData();
            data.fromBytes(buf);
            DATA.add(data);
        }

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(TOTAL_TICKS);
        buf.writeBoolean(hasMore);
        buf.writeLong(endTime);
        buf.writeInt(DATA.size());
        for(EntityData data : DATA){
            data.toBytes(buf);
        }
    }

    public static class EntityData implements IMessage {

        private TreeMap<Entry, Object> data = new TreeMap<>();
        public Type type;

        EntityData(){}

        public enum Type{
            ENTITY,
            TILE_ENTITY,
            BLOCK,
            EVENT_BUS_LISTENER
        }

        public enum Entry{
            WORLD_ID(Coder.INTEGER),

            ENTITY_NAME(Coder.STRING),
            ENTITY_UUID(Coder.UUID),
            ENTITY_CLASS_NAME(Coder.STRING),

            BLOCK_NAME(Coder.STRING),
            BLOCK_POS_X(Coder.INTEGER),
            BLOCK_POS_Y(Coder.INTEGER),
            BLOCK_POS_Z(Coder.INTEGER),
            BLOCK_CLASS_NAME(Coder.STRING),

            EVENT_BUS_LISTENER_CLASS_NAME(Coder.STRING),
            EVENT_BUS_EVENT_CLASS_NAME(Coder.STRING),
            EVENT_BUS_THREAD_TYPE(Coder.INTEGER),

            NANOS(Coder.LONG);

            public final Coder coder;

            Entry(Coder d){
                this.coder = d;
            }
        }

        public EntityData(int worldID, String name, String className, UUID id, long nanos){
            type = Type.ENTITY;
            data.put(Entry.WORLD_ID, worldID);
            data.put(Entry.ENTITY_NAME, name);
            data.put(Entry.ENTITY_CLASS_NAME, className);
            data.put(Entry.ENTITY_UUID, id);
            data.put(Entry.NANOS, nanos);
        }

        public EntityData(int worldID, String name, String className, BlockPos pos, long nanos){
            type = Type.TILE_ENTITY;
            data.put(Entry.WORLD_ID, worldID);
            data.put(Entry.BLOCK_NAME, name);
            data.put(Entry.BLOCK_CLASS_NAME, className);
            data.put(Entry.BLOCK_POS_X, pos.getX());
            data.put(Entry.BLOCK_POS_Y, pos.getY());
            data.put(Entry.BLOCK_POS_Z, pos.getZ());
            data.put(Entry.NANOS, nanos);
        }

        public EntityData(WorldTimingManager.EventTimings eventTimings){
            type = Type.EVENT_BUS_LISTENER;
            data.put(Entry.EVENT_BUS_EVENT_CLASS_NAME, formatClassName(eventTimings.eventClass.toString()));
            data.put(Entry.EVENT_BUS_LISTENER_CLASS_NAME, formatClassName(eventTimings.listener));
            data.put(Entry.EVENT_BUS_THREAD_TYPE, eventTimings.threadType.ordinal());
            data.put(Entry.NANOS, eventTimings.get());
        }

        @SuppressWarnings("unchecked")
        public <T> T getValue(Entry entry){
            if(data.get(entry) == null){
                throw new IllegalArgumentException("Cant find the entry " + entry + " for " + type);
            }
            return (T) data.get(entry);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(type.ordinal());
            buf.writeInt(data.size());
            for(Map.Entry<Entry, Object> entry : data.entrySet()){
                buf.writeInt(entry.getKey().ordinal());
                entry.getKey().coder.write(entry.getValue(), buf);
            }
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            type = Type.values()[buf.readInt()];
            int size = buf.readInt();
            for(int i=0; i<size; i++){
                Entry entry = Entry.values()[buf.readInt()];
                data.put(entry, entry.coder.read(buf));
            }
        }
    }
}