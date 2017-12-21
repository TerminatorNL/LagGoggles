package cf.terminator.laggoggles.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public class TeleportRequest implements IMessage {

    public UUID uuid;

    @Override
    public void fromBytes(ByteBuf buf){
        long most = buf.readLong();
        long least = buf.readLong();
        uuid = new UUID(most, least);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }
}
