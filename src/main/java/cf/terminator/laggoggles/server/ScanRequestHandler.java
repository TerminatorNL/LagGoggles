package cf.terminator.laggoggles.server;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.packet.ProfileStatus;
import cf.terminator.laggoggles.packet.RequestScan;
import cf.terminator.laggoggles.packet.ScanResult;
import cf.terminator.laggoggles.profiler.world.ProfileManager;
import cf.terminator.laggoggles.util.CommonProxy;
import cf.terminator.laggoggles.util.Perms;
import cf.terminator.laggoggles.util.RunInServerThread;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ScanRequestHandler implements IMessageHandler<RequestScan, ProfileStatus> {

    @Override
    public ProfileStatus onMessage(RequestScan request, MessageContext ctx) {
        if(Perms.isOP(ctx.getServerHandler().playerEntity) == false){
            Main.LOGGER.info(ctx.getServerHandler().playerEntity.getName() + " Tried to start the profiler, but was denied to do so!");
            new RunInServerThread(new Runnable() {
                @Override
                public void run() {
                    ctx.getServerHandler().playerEntity.addChatMessage(new TextComponentString(TextFormatting.RED + " Sorry! you lack permissions to do this!"));
                }
            });
            ProfileStatus status = new ProfileStatus();
            status.length = 10;
            status.isProfiling = true;
            status.issuedBy = "No permission";
            return status;
        }

        /* Start profiler */
        new Thread(new Runnable() {
            @Override
            public void run() {

                /* Send status to admins */
                ProfileStatus status = new ProfileStatus();
                status.isProfiling = true;
                status.length = request.length;
                status.issuedBy = ctx.getServerHandler().playerEntity.getName();

                for(EntityPlayerMP admin : Perms.getAdmins()) {
                    CommonProxy.sendTo(status, admin);
                }

                Main.LOGGER.info(Main.MODID + " profiler started by " + ctx.getServerHandler().playerEntity.getName() + " (" + request.length + " seconds)");
                ScanResult result = ProfileManager.runProfiler(request);
                Main.LOGGER.info(Main.MODID + " finished profiling!");

                /* Send status to admins */
                ProfileStatus status2 = new ProfileStatus();
                status2.isProfiling = false;
                status2.length = request.length;
                status2.issuedBy = ctx.getServerHandler().playerEntity.getName();

                for(EntityPlayerMP admin : Perms.getAdmins()) {
                    CommonProxy.sendTo(status2, admin);
                }

                if(result != null) {
                    /* Send result */
                    if (request.shareResult == false) {
                        CommonProxy.sendTo(result, ctx.getServerHandler().playerEntity);
                    } else {
                        for (EntityPlayerMP admin : Perms.getAdmins()) {
                            CommonProxy.sendTo(result, admin);
                        }
                    }
                }
            }
        }).start();
        return null;
    }
}