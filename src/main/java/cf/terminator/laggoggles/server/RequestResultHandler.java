package cf.terminator.laggoggles.server;

import cf.terminator.laggoggles.CommonProxy;
import cf.terminator.laggoggles.packet.CPacketRequestResult;
import cf.terminator.laggoggles.packet.SPacketMessage;
import cf.terminator.laggoggles.util.Perms;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.UUID;

public class RequestResultHandler implements IMessageHandler<CPacketRequestResult, IMessage> {

    private HashMap<UUID, Long> LAST_RESULT_REQUEST = new HashMap<>();

    @Override
    public IMessage onMessage(CPacketRequestResult CPacketRequestResult, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;

        if(Perms.getPermission(player).ordinal() < Perms.Permission.GET.ordinal()){
            return new SPacketMessage("No permission");
        }
        if(ScanRequestHandler.LAST_RESULT == null){
            return new SPacketMessage("No data available");
        }
        if(Perms.getPermission(player).ordinal() < Perms.Permission.FULL.ordinal()){
            long lastRequest = LAST_RESULT_REQUEST.getOrDefault(player.getGameProfile().getId(), 0L);
            long secondsLeft = ServerConfig.NON_OPS_REQUEST_LAST_SCAN_DATA_TIMEOUT - ((System.currentTimeMillis() - lastRequest)/1000);
            if(secondsLeft > 0){
                return new SPacketMessage("Please wait " + secondsLeft + " seconds.");
            }
            LAST_RESULT_REQUEST.put(player.getGameProfile().getId(), System.currentTimeMillis());
        }
        CommonProxy.sendTo(Perms.getResultFor(player, ScanRequestHandler.LAST_RESULT), player);
        return null;
    }

}
