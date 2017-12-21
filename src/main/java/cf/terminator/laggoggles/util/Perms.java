package cf.terminator.laggoggles.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;

public class Perms {

    public static boolean isOP(EntityPlayer p){
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getOppedPlayers().getPermissionLevel(p.getGameProfile()) >= 2 || FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer() == false;
    }


    public static ArrayList<EntityPlayerMP> getAdmins(){
        ArrayList<EntityPlayerMP> admins = new ArrayList<>();
        for(EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            if(isOP(player)){
                admins.add(player);
            }
        }
        return admins;
    }
}
