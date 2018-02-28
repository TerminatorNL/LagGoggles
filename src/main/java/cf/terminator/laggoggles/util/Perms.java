package cf.terminator.laggoggles.util;

import cf.terminator.laggoggles.packet.ObjectData;
import cf.terminator.laggoggles.profiler.ProfileResult;
import cf.terminator.laggoggles.server.RequestDataHandler;
import cf.terminator.laggoggles.server.ServerConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.UUID;

public class Perms {

    public static final double MAX_RANGE_FOR_PLAYERS_HORIZONTAL_SQ = ServerConfig.NON_OPS_MAX_HORIZONTAL_RANGE * ServerConfig.NON_OPS_MAX_HORIZONTAL_RANGE;
    public static final double MAX_RANGE_FOR_PLAYERS_VERTICAL_SQ = ServerConfig.NON_OPS_MAX_VERTICAL_RANGE * ServerConfig.NON_OPS_MAX_HORIZONTAL_RANGE;

    public enum Permission{
        NONE,
        GET,
        START,
        FULL
    }

    public static Permission getPermission(EntityPlayer p){
        if(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getOppedPlayers().getPermissionLevel(p.getGameProfile()) > 0 || FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer() == false) {
            return Permission.FULL;
        }else{
            return ServerConfig.NON_OP_PERMISSION_LEVEL;
        }
    }

    public static boolean hasPermission(EntityPlayer player, Permission permission){
        return getPermission(player).ordinal() >= permission.ordinal();
    }

    public static ArrayList<EntityPlayerMP> getLagGogglesUsers(){
        ArrayList<EntityPlayerMP> list = new ArrayList<>();
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if(server == null){
            return list;
        }
        for(UUID uuid : RequestDataHandler.playersWithLagGoggles){
            Entity entity = server.getEntityFromUuid(uuid);
            if(entity instanceof EntityPlayerMP){
                list.add((EntityPlayerMP) entity);
            }
        }
        return list;
    }

    public static ProfileResult getResultFor(EntityPlayerMP player, ProfileResult result){
        Permission permission = getPermission(player);
        if(permission == Permission.NONE){
            return ProfileResult.EMPTY_RESULT;
        }
        if(permission == Permission.FULL){
            return result;
        }
        ProfileResult trimmedResult = result.copyStatsOnly();
        for(ObjectData data : result.getData()){
            if(isInRange(data, player) == true){
                trimmedResult.addData(data);
            }
        }
        return trimmedResult;
    }

    public static boolean isInRange(ObjectData data, EntityPlayerMP player){
        if(data.type == ObjectData.Type.EVENT_BUS_LISTENER){
            return ServerConfig.ALLOW_NON_OPS_TO_SEE_EVENT_SUBSCRIBERS;
        }
        if(data.<Integer>getValue(ObjectData.Entry.WORLD_ID) != player.dimension){
            return false;
        }
        switch(data.type){
            case ENTITY:
                WorldServer world = DimensionManager.getWorld(data.getValue(ObjectData.Entry.WORLD_ID));
                Entity e;
                if(world != null && (e = world.getEntityFromUuid(data.getValue(ObjectData.Entry.ENTITY_UUID))) != null){
                    return checkRange(player, e.posX, e.posY, e.posZ);
                }
                return false;
            case BLOCK:
            case TILE_ENTITY:
                return checkRange(player, data.getValue(ObjectData.Entry.BLOCK_POS_X), data.getValue(ObjectData.Entry.BLOCK_POS_Y), data.getValue(ObjectData.Entry.BLOCK_POS_Z));
            default:
                return false;
        }
    }

    public static boolean checkRange(EntityPlayerMP player, Integer x, Integer y, Integer z){
        return checkRange(player, x.doubleValue(), y.doubleValue(), z.doubleValue());
    }

    public static boolean checkRange(EntityPlayerMP player, double x, double y, double z){
        double xD = x - player.posX;
        double zD = z - player.posZ;

        /* Check horizontal range */
        if(xD*xD + zD*zD > MAX_RANGE_FOR_PLAYERS_HORIZONTAL_SQ){
            return false;
        }

        /* If it's within range, we check if the Y level is whitelisted */
        if(y > ServerConfig.NON_OPS_WHITELIST_HEIGHT_ABOVE){
            return true;
        }

        /* If it's underground, we restrict the results so you can't abuse it to find spawners, chests, minecarts.. etc. */
        double yD = y - player.posY;
        if(yD*yD > MAX_RANGE_FOR_PLAYERS_VERTICAL_SQ){
            return false;
        }
        return true;
    }
}
