package cf.terminator.laggoggles.profiler.world;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.packet.RequestScan;
import cf.terminator.laggoggles.packet.ScanResult;
import cf.terminator.laggoggles.profiler.TickCounter;
import cf.terminator.laggoggles.profiler.WorldTimingManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProfileManager {

    public static WorldTimingManager worldTimingManager = new WorldTimingManager();
    public static final AtomicBoolean PROFILE_ENABLED = new AtomicBoolean(false);
    public static ScanResult runProfiler(RequestScan request){
        return runProfiler(request.length);
    }

    public static ScanResult runProfiler(int seconds){
        try {
            start();
            Thread.sleep(seconds * 1000);
            HashMap<Integer, WorldTimingManager.WorldData> data = stop();
            ScanResult result = new ScanResult();
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
                    result.DATA.add(new ScanResult.EntityData(
                            worldID,
                            e.getName(),
                            e.getClass().toString().startsWith("class ") ? e.getClass().toString().substring(6) : e.getClass().toString(),
                            e.getPersistentID(),
                            entityTimes.getValue())
                    );
                }
                for(Map.Entry<BlockPos, Long> tileEntityTimes : entry.getValue().getBlockTimes().entrySet()){
                    TileEntity e = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(worldID).getTileEntity(tileEntityTimes.getKey());
                    if(e == null){
                        continue;
                    }
                    String name;
                    ITextComponent displayName = e.getDisplayName();
                    if(displayName != null){
                        name = displayName.getFormattedText();
                    }else{
                        name = e.getClass().getSimpleName();
                    }
                    result.DATA.add(new ScanResult.EntityData(
                            worldID,
                            name,
                            e.getClass().toString().startsWith("class ") ? e.getClass().toString().substring(6) : e.getClass().toString(),
                            e.getPos(),
                            tileEntityTimes.getValue())
                    );
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
