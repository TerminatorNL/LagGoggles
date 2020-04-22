package com.github.terminatornl.laggoggles.client;

import com.github.terminatornl.laggoggles.client.gui.GuiProfile;
import com.github.terminatornl.laggoggles.packet.SPacketServerData;
import com.github.terminatornl.laggoggles.util.Perms;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerDataPacketHandler implements IMessageHandler<SPacketServerData, IMessage> {

    public static Perms.Permission PERMISSION = Perms.Permission.NONE;
    public static boolean SERVER_HAS_RESULT = false;
    public static int MAX_SECONDS = Integer.MAX_VALUE;
    public static boolean RECEIVED_RESULT = false;
    public static boolean NON_OPS_CAN_SEE_EVENT_SUBSCRIBERS = false;

    @Override
    public IMessage onMessage(SPacketServerData msg, MessageContext messageContext) {
        SERVER_HAS_RESULT = msg.hasResult;
        PERMISSION = msg.permission;
        MAX_SECONDS = PERMISSION == Perms.Permission.FULL ? Integer.MAX_VALUE : msg.maxProfileTime;
        RECEIVED_RESULT = true;
        NON_OPS_CAN_SEE_EVENT_SUBSCRIBERS = msg.canSeeEventSubScribers;
        GuiProfile.update();
        return null;
    }
}
