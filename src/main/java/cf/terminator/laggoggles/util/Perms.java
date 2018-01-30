package cf.terminator.laggoggles.util;

import cf.terminator.laggoggles.packet.SPacketScanResult;
import cf.terminator.laggoggles.server.RequestDataHandler;
import cf.terminator.laggoggles.server.ServerConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;

public class Perms {

    private static MinecraftServer server;
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
        return RequestDataHandler.playersWithLagGoggles;
    }

    public static SPacketScanResult getResultFor(EntityPlayerMP player, SPacketScanResult result){
        server = FMLCommonHandler.instance().getMinecraftServerInstance();
        Permission permission = getPermission(player);
        if(permission == Permission.NONE){
            return null;
        }
        if(permission == Permission.FULL){
            return result;
        }
        SPacketScanResult trimmedResult = new SPacketScanResult();
        trimmedResult.endTime = result.endTime;
        trimmedResult.hasMore = result.hasMore;
        trimmedResult.TOTAL_TICKS = result.TOTAL_TICKS;
        trimmedResult.DATA = new ArrayList<>(result.DATA);
        for(SPacketScanResult.EntityData data : result.DATA){
            if(isInRange(data, player) == false){
                trimmedResult.DATA.remove(data);
            }
        }
        return trimmedResult;
    }

    public static boolean isInRange(SPacketScanResult.EntityData data, EntityPlayerMP player){
        if(data.type == SPacketScanResult.EntityData.Type.EVENT_BUS_LISTENER){
            return ServerConfig.ALLOW_NON_OPS_TO_SEE_EVENT_SUBSCRIBERS;
        }
        if(data.<Integer>getValue(SPacketScanResult.EntityData.Entry.WORLD_ID) != player.dimension){
            return false;
        }
        switch(data.type){
            case ENTITY:
                WorldServer world = server.worldServerForDimension(data.getValue(SPacketScanResult.EntityData.Entry.WORLD_ID));
                Entity e;
                if(world != null && (e = world.getEntityFromUuid(data.getValue(SPacketScanResult.EntityData.Entry.ENTITY_UUID))) != null){
                    return checkRange(player, e.posX, e.posY, e.posZ);
                }
                return false;
            case BLOCK:
            case TILE_ENTITY:
                return checkRange(player, data.getValue(SPacketScanResult.EntityData.Entry.BLOCK_POS_X), data.getValue(SPacketScanResult.EntityData.Entry.BLOCK_POS_Y), data.getValue(SPacketScanResult.EntityData.Entry.BLOCK_POS_Z));
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
