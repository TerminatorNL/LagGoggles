package cf.terminator.laggoggles.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.*;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Teleport {

    public static void teleportPlayer(EntityPlayerMP player, int dim, double x, double y, double z){
        new RunInServerThread(new Runnable() {
            @Override
            public void run() {
                if(player.dimension != dim) {
                    teleportPlayerToDimension(player, dim, x, y, z);
                }else{
                    player.setPositionAndUpdate(x,y,z);
                }
                player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Teleported to: " + TextFormatting.GRAY + " Dim: " + dim + TextFormatting.WHITE + ", " + (int) x + ", " +  (int) y + ", " + (int) z));
            }
        });
    }

    /* Shamelessly stolen from the SpongeCommon source, then ported it to forge. For more info:
     * https://github.com/SpongePowered/SpongeCommon/blob/292baf720df84345e02347d75085926b834abfea/src/main/java/org/spongepowered/common/entity/EntityUtil.java
     */

    private static void teleportPlayerToDimension(EntityPlayerMP playerIn, int suggestedDimensionId, double x, double y, double z) {
        WorldServer fromWorld = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(playerIn.dimension);
        WorldServer toWorld = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(suggestedDimensionId);
        playerIn.dimension = toWorld.provider.getDimension();
        ChunkPos pos = new ChunkPos(playerIn.getPosition());
        toWorld.getChunkProvider().loadChunk(pos.x, pos.z);
        final int dimensionId = playerIn.dimension;
        if (fromWorld != toWorld && fromWorld.provider.getDimensionType() == toWorld.provider.getDimensionType()) {
            playerIn.connection.sendPacket(new SPacketRespawn((dimensionId >= 0 ? -1 : 0), toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(), playerIn.interactionManager.getGameType()));
        }
        playerIn.connection.sendPacket(new SPacketRespawn(dimensionId, toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(), playerIn.interactionManager.getGameType()));
        fromWorld.removeEntityDangerously(playerIn);
        playerIn.isDead = false;
        playerIn.connection.setPlayerLocation(x, y, z, playerIn.rotationYaw, playerIn.rotationPitch);
        playerIn.world = toWorld;
        playerIn.setWorld(toWorld);
        toWorld.spawnEntity(playerIn);
        toWorld.updateEntityWithOptionalForce(playerIn, false);
        WorldServer worldserver = playerIn.getServerWorld();
        fromWorld.getPlayerChunkMap().removePlayer(playerIn);
        worldserver.getPlayerChunkMap().addPlayer(playerIn);
        worldserver.getChunkProvider().provideChunk((int)playerIn.posX >> 4, (int)playerIn.posZ >> 4);
        playerIn.connection.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch);
        playerIn.interactionManager.setWorld(toWorld);
        WorldBorder worldborder = FMLCommonHandler.instance().getMinecraftServerInstance().worlds[0].getWorldBorder();
        playerIn.connection.sendPacket(new SPacketWorldBorder(worldborder, SPacketWorldBorder.Action.INITIALIZE));
        playerIn.connection.sendPacket(new SPacketTimeUpdate(toWorld.getTotalWorldTime(), toWorld.getWorldTime(), toWorld.getGameRules().getBoolean("doDaylightCycle")));
        if (toWorld.isRaining()) {
            playerIn.connection.sendPacket(new SPacketChangeGameState(1, 0.0F));
            playerIn.connection.sendPacket(new SPacketChangeGameState(7, toWorld.getRainStrength(1.0F)));
            playerIn.connection.sendPacket(new SPacketChangeGameState(8, toWorld.getThunderStrength(1.0F)));
        }
        playerIn.sendContainerToPlayer(playerIn.inventoryContainer);
        playerIn.setPlayerHealthUpdated();
        playerIn.connection.sendPacket(new SPacketHeldItemChange(playerIn.inventory.currentItem));
    }
}
