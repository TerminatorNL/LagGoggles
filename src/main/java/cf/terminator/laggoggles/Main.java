package cf.terminator.laggoggles;

import cf.terminator.laggoggles.client.ClientProxy;
import cf.terminator.laggoggles.mixinhelper.MixinValidator;
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
import org.apache.logging.log4j.Logger;

import java.util.Random;

import static cf.terminator.laggoggles.profiler.ProfileManager.PROFILE_ENABLED;
import static cf.terminator.laggoggles.profiler.ProfileManager.timingManager;

@Mod(modid = Main.MODID_LOWER, name = Main.MODID, version = Main.VERSION, acceptableRemoteVersions = "*", guiFactory = "cf.terminator.laggoggles.client.gui.GuiInGameConfigFactory")
@IFMLLoadingPlugin.SortingIndex(1001)
public class Main implements TickInterceptor {
    public static final String MODID = "LagGoggles";
    public static final String MODID_LOWER = "laggoggles";
    public static final String VERSION = "${version}";
    public static Logger LOGGER;
    private TickInterceptor TickInterceptor;

    @SidedProxy(
            modId = Main.MODID_LOWER,
            serverSide = "cf.terminator.laggoggles.CommonProxy",
            clientSide = "cf.terminator.laggoggles.client.ClientProxy"
    )
    public static CommonProxy proxy;

    @EventHandler
    public void preinit(FMLPreInitializationEvent e){
        LOGGER = e.getModLog();
        proxy.preinit(e);
        MixinValidator.validate();
        Main.LOGGER.info("Registered sided proxy for: " + (proxy instanceof ClientProxy ? "Client" : "Dedicated server"));
    }

    @EventHandler
    public void postinit(FMLPostInitializationEvent e) {
        proxy.postinit(e);
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent e){
        proxy.serverStartingEvent(e);
        TickInterceptor = TickHub.INTERCEPTOR;
        TickHub.INTERCEPTOR = this;
    }

    @Override
    public void redirectUpdateTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random) {
        long LAGGOGGLES_START = System.nanoTime();
        TickInterceptor.redirectUpdateTick(block, worldIn, pos, state, random);
        if (PROFILE_ENABLED.get()) {
            timingManager.addBlockTime(worldIn.provider.getDimension(), pos, System.nanoTime() - LAGGOGGLES_START);
        }
    }

    @Override
    public void redirectRandomTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random) {
        long LAGGOGGLES_START = System.nanoTime();
        TickInterceptor.redirectRandomTick(block, worldIn, pos, state, random);
        if (PROFILE_ENABLED.get()) {
            timingManager.addBlockTime(worldIn.provider.getDimension(), pos, System.nanoTime() - LAGGOGGLES_START);
        }
    }

    @Override
    public void redirectUpdate(ITickable tickable) {
        long LAGGOGGLES_START = System.nanoTime();
        TickInterceptor.redirectUpdate(tickable);
        if (PROFILE_ENABLED.get()) {
            if(tickable instanceof TileEntity){
                TileEntity tile = (TileEntity) tickable;
                timingManager.addBlockTime(tile.getWorld().provider.getDimension(), tile.getPos(), System.nanoTime() - LAGGOGGLES_START);
            }
        }
    }

    @Override
    public void redirectOnUpdate(Entity entity) {
        long LAGGOGGLES_START = System.nanoTime();
        TickInterceptor.redirectOnUpdate(entity);
        if(PROFILE_ENABLED.get()){
            timingManager.addEntityTime(entity.dimension, entity.getPersistentID(), System.nanoTime() - LAGGOGGLES_START);
        }
    }
}
