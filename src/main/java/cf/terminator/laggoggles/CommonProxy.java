package cf.terminator.laggoggles;

import cf.terminator.laggoggles.client.MessagePacketHandler;
import cf.terminator.laggoggles.client.ProfileStatusHandler;
import cf.terminator.laggoggles.client.ScanResultHandler;
import cf.terminator.laggoggles.client.ServerDataPacketHandler;
import cf.terminator.laggoggles.packet.*;
import cf.terminator.laggoggles.profiler.TickCounter;
import cf.terminator.laggoggles.server.*;
import cf.terminator.laggoggles.util.Perms;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;

public class CommonProxy {

    public static final SimpleNetworkWrapper NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(Main.MODID);

    private byte PACKET_ID = 0;

    public void preinit(FMLPreInitializationEvent e){

    }

    public void init(FMLInitializationEvent e){}

    public void postinit(FMLPostInitializationEvent e){
        NETWORK_WRAPPER.registerMessage(
                ScanResultHandler.class,
                SPacketScanResult.class, PACKET_ID++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(
                ProfileStatusHandler.class,
                SPacketProfileStatus.class, PACKET_ID++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(
                ServerDataPacketHandler.class,
                SPacketServerData.class, PACKET_ID++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(
                RequestDataHandler.class,
                CPacketRequestServerData.class, PACKET_ID++, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(
                MessagePacketHandler.class,
                SPacketMessage.class, PACKET_ID++, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(
                ScanRequestHandler.class,
                CPacketRequestScan.class, PACKET_ID++, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(
                TeleportRequestHandler.class,
                CPacketRequestEntityTeleport.class, PACKET_ID++, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(
                TeleportToTileEntityRequestHandler.class,
                CPacketRequestTileEntityTeleport.class, PACKET_ID++, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(
                RequestResultHandler.class,
                CPacketRequestResult.class, PACKET_ID++, Side.SERVER);
    }

    public static void sendTo(IMessage msg, EntityPlayerMP player){
        if(msg instanceof SPacketScanResult) {
            /* SPacketScanResult is a big packet and 1.10.2 acts funky on those, therefore we must split it
             * I chose to do 1 packet per 50 entities */
            SPacketScanResult result = Perms.getResultFor(player, (SPacketScanResult) msg);
            if (result == null) {
                return;
            }
            ArrayList<SPacketScanResult.EntityData> DATA = new ArrayList<>(result.DATA);
            long endTime = result.endTime;
            while (DATA.size() > 0) {
                ArrayList<SPacketScanResult.EntityData> SUBLIST = new ArrayList<>();
                for (int i = 0; i < 25 && DATA.size() > 0; i++) {
                    SUBLIST.add(DATA.remove(0));
                }
                SPacketScanResult SUBRESULT = new SPacketScanResult();
                SUBRESULT.endTime = endTime;
                SUBRESULT.hasMore = DATA.size() > 0;
                SUBRESULT.DATA = SUBLIST;
                SUBRESULT.TOTAL_TICKS = result.TOTAL_TICKS;
                NETWORK_WRAPPER.sendTo(SUBRESULT, player);
            }
        }else{
            NETWORK_WRAPPER.sendTo(msg, player);
        }
    }

    public void serverStartingEvent(FMLServerStartingEvent e){
        MinecraftForge.EVENT_BUS.register(new TickCounter());
        MinecraftForge.EVENT_BUS.register(new RequestDataHandler());
    }


}
