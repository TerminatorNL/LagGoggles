package com.github.terminatornl.laggoggles;

import com.github.terminatornl.laggoggles.client.ClientProxy;
import com.github.terminatornl.laggoggles.profiler.ProfileManager;
import com.github.terminatornl.tickcentral.TickCentral;
import com.github.terminatornl.tickcentral.api.TickHub;
import com.github.terminatornl.tickcentral.api.TickInterceptor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

import static com.github.terminatornl.laggoggles.profiler.ProfileManager.timingManager;

@Mod(modid = Main.MODID_LOWER, name = Main.MODID, version = Main.VERSION, acceptableRemoteVersions = "*", guiFactory = "com.github.terminatornl.laggoggles.client.gui.GuiInGameConfigFactory", dependencies = "required:tickcentral@[2.2,);")
@IFMLLoadingPlugin.SortingIndex(1001)
public class Main implements TickInterceptor {
    public static final String MODID = "LagGoggles";
    public static final String MODID_LOWER = "laggoggles";
    public static final String VERSION = "${version}";
    public static Logger LOGGER = LogManager.getLogger(MODID);

    @SidedProxy(
            modId = Main.MODID_LOWER,
            serverSide = "com.github.terminatornl.laggoggles.CommonProxy",
            clientSide = "com.github.terminatornl.laggoggles.client.ClientProxy"
    )
    public static CommonProxy proxy;

    @EventHandler
    public void preinit(FMLPreInitializationEvent e){
        proxy.preinit(e);
        Main.LOGGER.info("Registered sided proxy for: " + (proxy instanceof ClientProxy ? "Client" : "Dedicated server"));
    }

    @EventHandler
    public void postinit(FMLPostInitializationEvent e) {
        proxy.postinit(e);
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent e){
        proxy.serverStartingEvent(e);
        TickHub.INTERCEPTOR = this;
    }

    @Override
    public void redirectUpdateTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random) {
        if (ProfileManager.PROFILER_ENABLED_UPDATE_SAFE) {
            long LAGGOGGLES_START = System.nanoTime();
            TickHub.trueUpdateTick(block, worldIn, pos, state, random);
            timingManager.addBlockTime(worldIn.provider.getDimension(), pos, System.nanoTime() - LAGGOGGLES_START);
        }else{
            TickHub.trueUpdateTick(block, worldIn, pos, state, random);
        }
    }

    @Override
    public void redirectRandomTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random) {
        if (ProfileManager.PROFILER_ENABLED_UPDATE_SAFE) {
            long LAGGOGGLES_START = System.nanoTime();
            TickHub.trueRandomTick(block, worldIn, pos, state, random);
            timingManager.addBlockTime(worldIn.provider.getDimension(), pos, System.nanoTime() - LAGGOGGLES_START);
        }else{
            TickHub.trueRandomTick(block, worldIn, pos, state, random);
        }
    }

    @Override
    public void redirectUpdate(ITickable tickable) {
        if (ProfileManager.PROFILER_ENABLED_UPDATE_SAFE && tickable instanceof TileEntity) {
            long LAGGOGGLES_START = System.nanoTime();
            TickHub.trueUpdate(tickable);
            long time = System.nanoTime() - LAGGOGGLES_START;
            TileEntity tile = (TileEntity) tickable;
            timingManager.addBlockTime(tile.getWorld().provider.getDimension(), tile.getPos(), time);
        }else{
            TickHub.trueUpdate(tickable);
        }
    }

    @Override
    public void redirectOnUpdate(Entity entity) {
        if(ProfileManager.PROFILER_ENABLED_UPDATE_SAFE){
            long LAGGOGGLES_START = System.nanoTime();
            TickHub.trueOnUpdate(entity);
            timingManager.addEntityTime(entity.dimension, entity.getPersistentID(), System.nanoTime() - LAGGOGGLES_START);
        }else{
            TickHub.trueOnUpdate(entity);
        }
    }
}
