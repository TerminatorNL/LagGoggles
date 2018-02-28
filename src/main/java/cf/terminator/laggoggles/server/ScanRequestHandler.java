package cf.terminator.laggoggles.server;

import cf.terminator.laggoggles.CommonProxy;
import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.api.Profiler;
import cf.terminator.laggoggles.packet.CPacketRequestScan;
import cf.terminator.laggoggles.packet.SPacketMessage;
import cf.terminator.laggoggles.packet.SPacketProfileStatus;
import cf.terminator.laggoggles.packet.SPacketServerData;
import cf.terminator.laggoggles.profiler.ScanType;
import cf.terminator.laggoggles.util.Perms;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.UUID;

import static cf.terminator.laggoggles.profiler.ProfileManager.LAST_PROFILE_RESULT;
import static cf.terminator.laggoggles.profiler.ProfileManager.PROFILE_ENABLED;

public class ScanRequestHandler implements IMessageHandler<CPacketRequestScan, IMessage> {

    private static HashMap<UUID, Long> COOLDOWN = new HashMap<>();

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

        /*
        long secondsLeft = (COOLDOWN.getOrDefault(requestee.getGameProfile().getId(),0L) - System.currentTimeMillis())/1000;
        if(secondsLeft > 0 && requesteePerms != Perms.Permission.FULL){
            return new SPacketMessage("Please wait " + secondsLeft + " seconds.");
        }
        COOLDOWN.put(requestee.getGameProfile().getId(), System.currentTimeMillis() + (1000 * NON_OPS_PROFILE_COOL_DOWN_SECONDS));
*/

        long secondsLeft = secondsLeft(requestee.getGameProfile().getId());
        if(secondsLeft > 0 && requesteePerms != Perms.Permission.FULL){
            return new SPacketMessage("Please wait " + secondsLeft + " seconds.");
        }

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
                Profiler.runProfiler(request.length, ScanType.WORLD);
                Main.LOGGER.info(Main.MODID + " finished profiling!");

                /* Send status to users */
                SPacketProfileStatus status2 = new SPacketProfileStatus(false, request.length, requestee.getName());
                for(EntityPlayerMP user : Perms.getLagGogglesUsers()) {
                    CommonProxy.sendTo(status2, user);
                }

                CommonProxy.sendTo(LAST_PROFILE_RESULT.get(), requestee);
                for(EntityPlayerMP user : Perms.getLagGogglesUsers()) {
                    CommonProxy.sendTo(new SPacketServerData(user), user);
                }
            }
        }).start();
        return null;
    }


    public static long secondsLeft(UUID uuid){
        long lastRequest = COOLDOWN.getOrDefault(uuid, 0L);
        long secondsLeft = ServerConfig.NON_OPS_PROFILE_COOL_DOWN_SECONDS - ((System.currentTimeMillis() - lastRequest)/1000);
        if(secondsLeft <= 0){
            COOLDOWN.put(uuid, System.currentTimeMillis());
        }
        return secondsLeft;
    }
}