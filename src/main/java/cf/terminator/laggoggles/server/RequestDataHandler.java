package cf.terminator.laggoggles.server;

import cf.terminator.laggoggles.packet.CPacketRequestServerData;
import cf.terminator.laggoggles.packet.SPacketServerData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;

public class RequestDataHandler implements IMessageHandler<CPacketRequestServerData,SPacketServerData>{

    public static final ArrayList<EntityPlayerMP> playersWithLagGoggles = new ArrayList<>();

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent e){
        playersWithLagGoggles.remove(e.player);
    }

    @Override
    public SPacketServerData onMessage(CPacketRequestServerData cPacketRequestServerData, MessageContext ctx){
        if(playersWithLagGoggles.contains(ctx.getServerHandler().player) == false) {
            playersWithLagGoggles.add(ctx.getServerHandler().player);
        }
        return new SPacketServerData(ctx.getServerHandler().player);
    }
}
