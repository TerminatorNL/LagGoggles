package cf.terminator.laggoggles.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.interfaces.block.IMixinBlock;

import java.util.Iterator;
import java.util.Random;

import static cf.terminator.laggoggles.profiler.ProfileManager.PROFILE_ENABLED;
import static cf.terminator.laggoggles.profiler.ProfileManager.timingManager;

@Mixin(value = WorldServer.class, priority = 1001)
public abstract class MixinWorldServerSponge extends World {

    protected MixinWorldServerSponge(ISaveHandler p_i45749_1_, WorldInfo p_i45749_2_, WorldProvider p_i45749_3_, Profiler p_i45749_4_, boolean p_i45749_5_) {
        super(p_i45749_1_, p_i45749_2_, p_i45749_3_, p_i45749_4_, p_i45749_5_);
    }

    private Long LAGGOGGLES_START_TICK = null;
    private Long LAGGOGGLES_START_RANDOM = null;

    @Inject(method = "tickUpdates(Z)Z",
            at = @At(value = "INVOKE",
                     target = "net/minecraft/block/Block.updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V",
                     shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void beforeUpdate(boolean runAllPending, CallbackInfoReturnable<Boolean> cir, int i, Iterator iterator, NextTickListEntry nextticklistentry1, int k, IBlockState iblockstate){
        LAGGOGGLES_START_TICK = System.nanoTime();
    }

    @Inject(method = "tickUpdates(Z)Z",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/block/Block.updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void afterUpdate(boolean runAllPending, CallbackInfoReturnable<Boolean> cir, int i, Iterator iterator, NextTickListEntry nextticklistentry1, int k, IBlockState iblockstate, Block var10, WorldServer var11, BlockPos var12, IBlockState var13, Random var14){
        if (PROFILE_ENABLED.get() && LAGGOGGLES_START_TICK != null) {
            timingManager.addBlockTime(provider.getDimension(), nextticklistentry1.position, System.nanoTime() - LAGGOGGLES_START_TICK);
        }
    }


    @Inject(method = "updateBlocks",
            at = @At(value = "INVOKE",
                     target = "net/minecraft/block/Block.randomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V",
                     shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void beforeUpdateBlocks(CallbackInfo ci, int i, boolean flag, boolean flag1, PhaseTracker phaseTracker, Iterator iterator, Chunk chunk, World world, int j, int k, ExtendedBlockStorage var10[], int var11, int var12, ExtendedBlockStorage extendedblockstorage, int i1, int j1, int k1, int l1, int i2, IBlockState iblockstate, Block block, BlockPos pos, IMixinBlock spongeBlock, PhaseData currentTuple, IPhaseState phaseState){
        LAGGOGGLES_START_RANDOM = System.nanoTime();
    }

    @Inject(method = "updateBlocks",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/block/Block.randomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void afterUpdateBlocks(CallbackInfo ci, int i, boolean flag, boolean flag1, PhaseTracker phaseTracker, Iterator iterator, Chunk chunk, World world, int j, int k, ExtendedBlockStorage var10[], int var11, int var12, ExtendedBlockStorage extendedblockstorage, int i1, int j1, int k1, int l1, int i2, IBlockState iblockstate, Block block, BlockPos pos, IMixinBlock spongeBlock, PhaseData currentTuple, IPhaseState phaseState){
        if (PROFILE_ENABLED.get() && LAGGOGGLES_START_RANDOM != null) {
            timingManager.addBlockTime(provider.getDimension(), new BlockPos(k1 + j, i2 + extendedblockstorage.getYLocation(), l1 + k), System.nanoTime() - LAGGOGGLES_START_RANDOM);
        }
    }

}