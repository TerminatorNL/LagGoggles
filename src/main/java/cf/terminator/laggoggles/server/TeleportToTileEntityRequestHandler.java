package cf.terminator.laggoggles.server;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.packet.CPacketRequestTileEntityTeleport;
import cf.terminator.laggoggles.packet.SPacketMessage;
import cf.terminator.laggoggles.util.Perms;
import cf.terminator.laggoggles.util.Teleport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TeleportToTileEntityRequestHandler implements IMessageHandler<CPacketRequestTileEntityTeleport, IMessage> {

    @Override
    public IMessage onMessage(final CPacketRequestTileEntityTeleport message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if(Perms.hasPermission(player, Perms.Permission.FULL) == false){
            Main.LOGGER.info(player.getName() + " tried to teleport, but was denied to do so!");
            return new SPacketMessage("No permission");
        }
        Teleport.teleportPlayer(player, message.dim, message.x, message.y, message.z);
        return null;
    }
}