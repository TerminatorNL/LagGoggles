package com.github.terminatornl.laggoggles.packet;

import com.github.terminatornl.laggoggles.profiler.ScanType;
import com.github.terminatornl.laggoggles.util.Side;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;

public class SPacketScanResult implements IMessage{

    public SPacketScanResult(){}

    public ArrayList<ObjectData> DATA = new ArrayList<>();
    public boolean hasMore = false;
    public long startTime;
    public long endTime;
    public long totalTime;
    public long tickCount;
    public Side side;
    public ScanType type;
    public long totalFrames = 0;

    @Override
    public void fromBytes(ByteBuf buf) {
        tickCount = buf.readLong();
        hasMore = buf.readBoolean();
        endTime = buf.readLong();
        startTime = buf.readLong();
        totalTime = buf.readLong();
        totalFrames = buf.readLong();
        side = Side.values()[buf.readInt()];
        type = ScanType.values()[buf.readInt()];

        int size = buf.readInt();
        for(int i=0; i<size; i++){
            ObjectData data = new ObjectData();
            data.fromBytes(buf);
            DATA.add(data);
        }

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(tickCount);
        buf.writeBoolean(hasMore);
        buf.writeLong(endTime);
        buf.writeLong(startTime);
        buf.writeLong(totalTime);
        buf.writeLong(totalFrames);
        buf.writeInt(side.ordinal());
        buf.writeInt(type.ordinal());

        buf.writeInt(DATA.size());
        for(ObjectData data : DATA){
            data.toBytes(buf);
        }
    }

}