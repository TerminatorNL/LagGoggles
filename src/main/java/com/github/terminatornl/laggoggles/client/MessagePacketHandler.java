package com.github.terminatornl.laggoggles.client;

import com.github.terminatornl.laggoggles.Main;
import com.github.terminatornl.laggoggles.client.gui.GuiProfile;
import com.github.terminatornl.laggoggles.packet.CPacketRequestServerData;
import com.github.terminatornl.laggoggles.packet.SPacketMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePacketHandler implements IMessageHandler<SPacketMessage, CPacketRequestServerData> {

    @Override
    public CPacketRequestServerData onMessage(SPacketMessage msg, MessageContext messageContext) {
        GuiProfile.MESSAGE = msg;
        GuiProfile.MESSAGE_END_TIME = System.currentTimeMillis() + (msg.seconds * 1000);
        GuiProfile.update();
        Main.LOGGER.info("message received from server: " + msg.message);
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.RED + msg.message));
        return new CPacketRequestServerData();
    }
}
