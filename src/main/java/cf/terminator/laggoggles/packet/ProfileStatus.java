package cf.terminator.laggoggles.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class ProfileStatus implements IMessage {

    public boolean isProfiling = true;
    public String issuedBy = "Unknown";
    public int length = 0;

    public ProfileStatus(){}

    @Override
    public void fromBytes(ByteBuf buf){
        isProfiling = buf.readBoolean();
        length = buf.readInt();
        issuedBy = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isProfiling);
        buf.writeInt(length);
        ByteBufUtils.writeUTF8String(buf, issuedBy);
    }
}
