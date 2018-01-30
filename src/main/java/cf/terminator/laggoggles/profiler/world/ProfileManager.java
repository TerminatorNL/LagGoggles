package cf.terminator.laggoggles.profiler.world;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.packet.CPacketRequestScan;
import cf.terminator.laggoggles.packet.SPacketScanResult;
import cf.terminator.laggoggles.profiler.TickCounter;
import cf.terminator.laggoggles.profiler.WorldTimingManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static cf.terminator.laggoggles.util.Graphical.formatClassName;

public class ProfileManager {

    public static WorldTimingManager worldTimingManager = new WorldTimingManager();
    public static final AtomicBoolean PROFILE_ENABLED = new AtomicBoolean(false);
    public static SPacketScanResult runProfiler(CPacketRequestScan request){
        return runProfiler(request.length);
    }

    public static SPacketScanResult runProfiler(int seconds){
        try {
            start();
            Thread.sleep(seconds * 1000);
            HashMap<Integer, WorldTimingManager.WorldData> data = stop();
            SPacketScanResult result = new SPacketScanResult();
            result.endTime = System.currentTimeMillis();
            result.TOTAL_TICKS = TickCounter.ticks.get();
            MinecraftServer minecraftServer = FMLCommonHandler.instance().getMinecraftServerInstance();
            if(minecraftServer == null){
                /* Someone shut down the server while we were profiling! */
                return null;
            }
            for(Map.Entry<Integer, WorldTimingManager.WorldData> entry : data.entrySet()){
                int worldID = entry.getKey();
                for(Map.Entry<UUID, Long> entityTimes : entry.getValue().getEntityTimes().entrySet()){
                    Entity e = minecraftServer.worldServerForDimension(worldID).getEntityFromUuid(entityTimes.getKey());
                    if(e == null){
                        continue;
                    }
                    result.DATA.add(new SPacketScanResult.EntityData(
                            worldID,
                            e.getName(),
                            formatClassName(e.getClass().toString()),
                            e.getPersistentID(),
                            entityTimes.getValue())
                    );
                }
                for(Map.Entry<BlockPos, Long> tileEntityTimes : entry.getValue().getBlockTimes().entrySet()){
                    WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(worldID);
                    TileEntity e = world.getTileEntity(tileEntityTimes.getKey());
                    if(e != null) {
                        String name;
                        ITextComponent displayName = e.getDisplayName();
                        if (displayName != null) {
                            name = displayName.getFormattedText();
                        } else {
                            name = e.getClass().getSimpleName();
                        }
                        result.DATA.add(new SPacketScanResult.EntityData(
                                worldID,
                                name,
                                formatClassName(e.getClass().toString()),
                                e.getPos(),
                                tileEntityTimes.getValue())
                        );
                    }else{
                        /* The block is not a tile entity, get the actual block. */
                        IBlockState state = world.getBlockState(tileEntityTimes.getKey());
                        String name = state.getBlock().getLocalizedName();
                        result.DATA.add(new SPacketScanResult.EntityData(
                                worldID,
                                name,
                                formatClassName(state.getBlock().getClass().toString()),
                                tileEntityTimes.getKey(),
                                tileEntityTimes.getValue()));
                    }
                }

                for(Map.Entry<String, WorldTimingManager.EventTimings> eventTimes : entry.getValue().getEventTimes().entrySet()){
                    result.DATA.add(new SPacketScanResult.EntityData(eventTimes.getValue()));
                }

            }
            return result;
        } catch (Throwable e) {
            Main.LOGGER.error("Woa! Something went wrong while processing results! Please contact Terminator_NL and submit the following error in an issue at github!");
            e.printStackTrace();
            return null;
        }
    }

    public static void start(){
        TickCounter.ticks.set(0L);
        worldTimingManager = new WorldTimingManager();
        PROFILE_ENABLED.set(true);
    }

    public static HashMap<Integer, WorldTimingManager.WorldData> stop(){
        PROFILE_ENABLED.set(false);
        return worldTimingManager.getTimings();
    }
}
