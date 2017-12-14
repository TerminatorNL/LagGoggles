package cf.terminator.laggoggles.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public class TeleportRequest implements IMessage {

    public UUID uuid;

    @Override
    public void fromBytes(ByteBuf buf){
        uuid = UUID.fromString(new String(buf.readBytes(36).array()));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(uuid.toString().getBytes());
    }
}
