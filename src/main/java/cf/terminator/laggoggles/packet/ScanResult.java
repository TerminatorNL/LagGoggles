package cf.terminator.laggoggles.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.UUID;

public class ScanResult implements IMessage{

    public ScanResult(){}

    public ArrayList<EntityData> DATA = new ArrayList<>();
    public long TOTAL_TICKS = 0L;
    public boolean hasMore = false;

    @Override
    public void fromBytes(ByteBuf buf) {
        TOTAL_TICKS = buf.readLong();
        hasMore = buf.readBoolean();
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
        buf.writeInt(DATA.size());
        for(EntityData data : DATA){
            data.toBytes(buf);
        }

    }

    public static class EntityData implements IMessage {

        public String name;
        public String className;
        public UUID id;
        public int x;
        public int y;
        public int z;
        public int worldID;
        public boolean isTileEntity;
        public long nanos;

        public EntityData(int worldID, String name, String className, UUID id, long nanos){
            this.worldID = worldID;
            this.name = name;
            this.className = className;
            this.id = id;
            this.isTileEntity = false;
            this.nanos = nanos;
        }

        public EntityData(int worldID, String name, String className, BlockPos pos, long nanos){
            this.isTileEntity = true;
            this.name = name;
            this.className = className;
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            this.worldID = worldID;
            this.nanos = nanos;
        }

        EntityData(){}

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(worldID);
            buf.writeLong(nanos);
            ByteBufUtils.writeUTF8String(buf, name);
            ByteBufUtils.writeUTF8String(buf, className);
            buf.writeBoolean(isTileEntity);
            if(isTileEntity){
                buf.writeInt(x);
                buf.writeInt(y);
                buf.writeInt(z);
            }else {
                ByteBufUtils.writeUTF8String(buf, id.toString());
            }
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            worldID = buf.readInt();
            nanos = buf.readLong();
            name = ByteBufUtils.readUTF8String(buf);
            className = ByteBufUtils.readUTF8String(buf);
            isTileEntity = buf.readBoolean();
            if(isTileEntity){
                x = buf.readInt();
                y = buf.readInt();
                z = buf.readInt();
            }else {
                String unchecked = ByteBufUtils.readUTF8String(buf);
                try {
                    id = UUID.fromString(unchecked);
                }catch (java.lang.IllegalArgumentException e){
                    id = UUID.nameUUIDFromBytes(unchecked.getBytes());
                }
            }
        }

    }
}