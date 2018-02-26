package cf.terminator.laggoggles.util;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public enum Side {
    DEDICATED_SERVER,
    CLIENT_WITHOUT_SERVER,
    CLIENT_WITH_SERVER,
    UNKNOWN;

    public static Side getSide(){
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if(server == null){
            return CLIENT_WITHOUT_SERVER;
        }else if(server.isDedicatedServer()){
            return DEDICATED_SERVER;
        }else if(Minecraft.getMinecraft().isSingleplayer()){
            return CLIENT_WITH_SERVER;
        }
        return UNKNOWN;
    }

    public boolean isServer(){
        return this == DEDICATED_SERVER;
    }

    public boolean isPlayingOnServer(){
        return this == CLIENT_WITHOUT_SERVER;
    }

    public boolean isClient(){
        switch (this){
            case CLIENT_WITH_SERVER:
            case CLIENT_WITHOUT_SERVER:
                return true;
            default:
                return false;
        }
    }

    public boolean isSinglePlayer(){
        switch (this){
            case DEDICATED_SERVER:
            case CLIENT_WITHOUT_SERVER:
            case UNKNOWN:
                return false;
            case CLIENT_WITH_SERVER:
                return true;
        }
        throw new RuntimeException("Someone forgot to update this piece of code!");
    }
}
