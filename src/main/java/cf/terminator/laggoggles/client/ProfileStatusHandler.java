package cf.terminator.laggoggles.client;

import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.packet.SPacketProfileStatus;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ProfileStatusHandler implements IMessageHandler<SPacketProfileStatus, IMessage> {

    @Override
    public IMessage onMessage(SPacketProfileStatus message, MessageContext ctx) {
        GuiProfile.PROFILING_PLAYER = message.issuedBy;
        if(message.isProfiling == true) {
            GuiProfile.PROFILE_END_TIME = System.currentTimeMillis() + (message.length * 1000);
        }else{
            GuiProfile.PROFILE_END_TIME = System.currentTimeMillis();
        }
        GuiProfile.update();
        return null;
    }
}
