package com.github.terminatornl.laggoggles.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SPacketMessage implements IMessage{

    public String message;
    public int seconds = 3;

    public SPacketMessage(){}
    public SPacketMessage(String msg){
        message = msg;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        message = ByteBufUtils.readUTF8String(buf);
        seconds = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, message);
        buf.writeInt(seconds);
    }
}
