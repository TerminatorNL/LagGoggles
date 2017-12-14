package cf.terminator.laggoggles.client;

import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.packet.ProfileStatus;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ProfileStatusHandler implements IMessageHandler<ProfileStatus, IMessage> {

    @Override
    public IMessage onMessage(ProfileStatus message, MessageContext ctx) {
        GuiProfile.LAST_STATUS = message;
        GuiProfile.update();
        return null;
    }
}
