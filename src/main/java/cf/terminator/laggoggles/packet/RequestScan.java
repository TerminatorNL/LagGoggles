package cf.terminator.laggoggles.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RequestScan implements IMessage{

    public RequestScan(){
        shareResult = true;
        length = 5;
    }

    public int length;
    public boolean shareResult;

    @Override
    public void fromBytes(ByteBuf buf) {
        shareResult = buf.readBoolean();
        length = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(shareResult);
        buf.writeInt(length);
    }
}
