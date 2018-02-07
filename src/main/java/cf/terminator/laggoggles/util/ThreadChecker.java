package cf.terminator.laggoggles.util;

import cf.terminator.laggoggles.profiler.WorldTimingManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ThreadChecker {

    public static WorldTimingManager.EventTimings.ThreadType getThreadType(){
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if(server.isDedicatedServer() == true){
            /* Dedicated server */
            if(server.isCallingFromMinecraftThread()) {
                return WorldTimingManager.EventTimings.ThreadType.SERVER;
            }
        }else{
            /* Not a dedicated server, we have both the client and server classes. */
            if(server.isCallingFromMinecraftThread()){
                return WorldTimingManager.EventTimings.ThreadType.SERVER;
            }else if(Minecraft.getMinecraft().isCallingFromMinecraftThread()){
                return WorldTimingManager.EventTimings.ThreadType.CLIENT;
            }
        }
        return WorldTimingManager.EventTimings.ThreadType.ASYNC;
    }
}
