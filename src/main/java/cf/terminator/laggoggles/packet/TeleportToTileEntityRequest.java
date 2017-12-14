package cf.terminator.laggoggles.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class TeleportToTileEntityRequest implements IMessage{

    public int dim;
    public int x;
    public int y;
    public int z;

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