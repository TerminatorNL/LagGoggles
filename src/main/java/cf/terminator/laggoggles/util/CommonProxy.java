package cf.terminator.laggoggles.util;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.client.ProfileStatusHandler;
import cf.terminator.laggoggles.client.ScanResultHandler;
import cf.terminator.laggoggles.packet.*;
import cf.terminator.laggoggles.profiler.TickCounter;
import cf.terminator.laggoggles.server.ScanRequestHandler;
import cf.terminator.laggoggles.server.TeleportRequestHandler;
import cf.terminator.laggoggles.server.TeleportToTileEntityRequestHandler;
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

    /* CLIENT CHANNELS */
    protected byte PROFILE_STATUS_HANDLER_ID = 0x0;
    protected byte PROFILE_RESULT_HANDLER_ID = 0x1;

    /* SERVER CHANNELS */
    private byte SCAN_REQUEST_HANDLER_ID = 0x2;
    private byte TELEPORT_REQUEST_HANDLER_ID = 0x3;
    private byte TELEPORT_REQUEST_TO_TILE_HANDLER_ID = 0x4;

    public void preinit(FMLPreInitializationEvent e){}

    public void init(FMLInitializationEvent e){}

    public void postinit(FMLPostInitializationEvent e){

        NETWORK_WRAPPER.registerMessage(
                ScanResultHandler.class,
                ScanResult.class, PROFILE_STATUS_HANDLER_ID, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(
                ProfileStatusHandler.class,
                ProfileStatus.class, PROFILE_RESULT_HANDLER_ID, Side.CLIENT);
        NETWORK_WRAPPER.registerMessage(
                ScanRequestHandler.class,
                RequestScan.class, SCAN_REQUEST_HANDLER_ID, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(
                TeleportRequestHandler.class,
                TeleportRequest.class, TELEPORT_REQUEST_HANDLER_ID, Side.SERVER);
        NETWORK_WRAPPER.registerMessage(
                TeleportToTileEntityRequestHandler.class,
                TeleportToTileEntityRequest.class, TELEPORT_REQUEST_TO_TILE_HANDLER_ID, Side.SERVER);
    }

    public static void sendTo(IMessage msg, EntityPlayerMP player){
        if(msg instanceof ScanResult) {
            /* ScanResult is a big packet and 1.10.2 acts funky on those, therefore we must split it
             * I chose to do 1 packet per 50 entities */
            ArrayList<ScanResult.EntityData> DATA = ((ScanResult) msg).DATA;
            while (DATA.size() > 0) {
                ArrayList<ScanResult.EntityData> SUBLIST = new ArrayList<>();
                for (int i = 0; i < 50 && DATA.size() > 0; i++) {
                    SUBLIST.add(DATA.remove(0));
                }
                ScanResult SUBRESULT = new ScanResult();
                SUBRESULT.hasMore = DATA.size() > 0;
                SUBRESULT.DATA = SUBLIST;
                SUBRESULT.TOTAL_TICKS = ((ScanResult) msg).TOTAL_TICKS;
                NETWORK_WRAPPER.sendTo(SUBRESULT, player);
            }
        }else{
            NETWORK_WRAPPER.sendTo(msg, player);
        }
    }

    public void serverStartingEvent(FMLServerStartingEvent e){
        MinecraftForge.EVENT_BUS.register(new TickCounter());
    }
}
