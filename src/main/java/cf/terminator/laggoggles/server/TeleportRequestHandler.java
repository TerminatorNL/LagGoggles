package cf.terminator.laggoggles.server;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.packet.CPacketRequestEntityTeleport;
import cf.terminator.laggoggles.packet.SPacketMessage;
import cf.terminator.laggoggles.util.Perms;
import cf.terminator.laggoggles.util.RunInServerThread;
import cf.terminator.laggoggles.util.Teleport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TeleportRequestHandler implements IMessageHandler<CPacketRequestEntityTeleport, IMessage> {

    @Override
    public IMessage onMessage(CPacketRequestEntityTeleport message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if(Perms.hasPermission(player, Perms.Permission.FULL) == false){
            Main.LOGGER.info(player.getName() + " tried to teleport, but was denied to do so!");
            return new SPacketMessage("No permission");
        }
        new RunInServerThread(new Runnable() {
            @Override
            public void run() {
                Entity e = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityFromUuid(message.uuid);
                if(e == null){
                    player.sendMessage(new TextComponentString(TextFormatting.RED + "Woops! This entity no longer exists!"));
                    return;
                }
                Teleport.teleportPlayer(player, e.dimension, e.posX, e.posY, e.posZ);
            }
        });
        return null;
    }
}
