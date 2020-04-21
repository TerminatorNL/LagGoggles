package com.github.terminatornl.laggoggles.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CPacketRequestScan implements IMessage{

    public CPacketRequestScan(){
        length = 5;
    }

    public int length;

    @Override
    public void fromBytes(ByteBuf buf) {
        length = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(length);
    }
}
