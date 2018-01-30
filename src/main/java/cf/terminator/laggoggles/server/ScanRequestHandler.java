package cf.terminator.laggoggles.server;

import cf.terminator.laggoggles.CommonProxy;
import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.packet.*;
import cf.terminator.laggoggles.profiler.world.ProfileManager;
import cf.terminator.laggoggles.util.Perms;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.UUID;

import static cf.terminator.laggoggles.profiler.world.ProfileManager.PROFILE_ENABLED;
import static cf.terminator.laggoggles.server.ServerConfig.NON_OPS_PROFILE_COOL_DOWN_SECONDS;

public class ScanRequestHandler implements IMessageHandler<CPacketRequestScan, IMessage> {

    private static HashMap<UUID, Long> COOLDOWN = new HashMap<>();
    public static SPacketScanResult LAST_RESULT;

    @Override
    public IMessage onMessage(CPacketRequestScan request, MessageContext ctx) {
        final EntityPlayerMP requestee = ctx.getServerHandler().playerEntity;
        Perms.Permission requesteePerms = Perms.getPermission(requestee);

        if(requesteePerms.ordinal() < Perms.Permission.START.ordinal()){
            Main.LOGGER.info(requestee.getName() + " Tried to start the profiler, but was denied to do so!");
            return new SPacketMessage("No permission");
        }

        if(requesteePerms != Perms.Permission.FULL && request.length > ServerConfig.NON_OPS_MAX_PROFILE_TIME){
            return new SPacketMessage("Profile time is too long! You can profile up to " + ServerConfig.NON_OPS_MAX_PROFILE_TIME + " seconds.");
        }

        if(PROFILE_ENABLED.get() == true){
            return new SPacketMessage("Profiler is already running");
        }

        long secondsLeft = (COOLDOWN.getOrDefault(requestee.getGameProfile().getId(),0L) - System.currentTimeMillis())/1000;
        if(secondsLeft > 0 && requesteePerms != Perms.Permission.FULL){
            return new SPacketMessage("Please wait " + secondsLeft + " seconds.");
        }
        COOLDOWN.put(requestee.getGameProfile().getId(), System.currentTimeMillis() + (1000 * NON_OPS_PROFILE_COOL_DOWN_SECONDS));

        /* Start profiler */
        new Thread(new Runnable() {
            @Override
            public void run() {

                /* Send status to users */
                SPacketProfileStatus status = new SPacketProfileStatus(true, request.length, requestee.getName());
                for(EntityPlayerMP user : Perms.getLagGogglesUsers()) {
                    CommonProxy.sendTo(status, user);
                }

                Main.LOGGER.info(Main.MODID + " profiler started by " + requestee.getName() + " (" + request.length + " seconds)");
                LAST_RESULT = ProfileManager.runProfiler(request);
                Main.LOGGER.info(Main.MODID + " finished profiling!");

                /* Send status to users */
                SPacketProfileStatus status2 = new SPacketProfileStatus(false, request.length, requestee.getName());
                for(EntityPlayerMP user : Perms.getLagGogglesUsers()) {
                    CommonProxy.sendTo(status2, user);
                }

                if(LAST_RESULT != null) {
                    CommonProxy.sendTo(LAST_RESULT, requestee);
                    for(EntityPlayerMP user : Perms.getLagGogglesUsers()) {
                        CommonProxy.sendTo(new SPacketServerData(user), user);
                    }
                }
            }
        }).start();
        return null;
    }
}