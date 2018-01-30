package cf.terminator.laggoggles.mixin;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

import static cf.terminator.laggoggles.profiler.world.ProfileManager.PROFILE_ENABLED;
import static cf.terminator.laggoggles.profiler.world.ProfileManager.worldTimingManager;

@Mixin(value = World.class,
        priority = 1001)
public abstract class MixinWorld {

    Long LAGGOGGLES_START = null;

    @Inject(
            method = "updateEntities()V",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/util/ITickable.update()V",
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void beforeTick(CallbackInfo ci, Iterator iterator, TileEntity tileentity, BlockPos blockpos){
        LAGGOGGLES_START = System.nanoTime();
    }

    @Inject(
            method = "updateEntities()V",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/util/ITickable.update()V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void afterTick(CallbackInfo ci, Iterator iterator, TileEntity tileentity, BlockPos pos) {
        if (PROFILE_ENABLED.get() && LAGGOGGLES_START != null) {
            worldTimingManager.addBlockTime(tileentity.getWorld().provider.getDimension(), pos, System.nanoTime() - LAGGOGGLES_START);
        }
    }
}
