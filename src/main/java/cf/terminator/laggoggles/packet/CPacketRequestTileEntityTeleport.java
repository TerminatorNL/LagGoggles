package cf.terminator.laggoggles.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CPacketRequestTileEntityTeleport implements IMessage{

    public int dim;
    public int x;
    public int y;
    public int z;

    public CPacketRequestTileEntityTeleport(){}
    public CPacketRequestTileEntityTeleport(SPacketScanResult.EntityData data){
        dim = data.getValue(SPacketScanResult.EntityData.Entry.WORLD_ID);
        x =   data.getValue(SPacketScanResult.EntityData.Entry.BLOCK_POS_X);
        y =   data.getValue(SPacketScanResult.EntityData.Entry.BLOCK_POS_Y);
        z =   data.getValue(SPacketScanResult.EntityData.Entry.BLOCK_POS_Z);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        dim = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dim);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }
}