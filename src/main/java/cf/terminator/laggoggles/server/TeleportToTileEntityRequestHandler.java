package cf.terminator.laggoggles.server;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.packet.TeleportToTileEntityRequest;
import cf.terminator.laggoggles.util.Perms;
import cf.terminator.laggoggles.util.Teleport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TeleportToTileEntityRequestHandler implements IMessageHandler<TeleportToTileEntityRequest, IMessage> {

    @Override
    public IMessage onMessage(final TeleportToTileEntityRequest message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if(Perms.isOP(player) == false){
            Main.LOGGER.info(player.getName() + " tried to teleport, but was denied to do so!");
            return null;
        }
        Teleport.teleportPlayer(player, message.dim, message.x, message.y, message.z);
        return null;
    }
}