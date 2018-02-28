package cf.terminator.laggoggles.mixin;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cf.terminator.laggoggles.profiler.ProfileManager.PROFILE_ENABLED;
import static cf.terminator.laggoggles.profiler.ProfileManager.timingManager;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher {

    private Long LAGGOGGLES_START = null;

    @Inject(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntity;DDDFI)V", at = @At("HEAD"))
    public void beforeRender(TileEntity tileEntityIn, double p_renderTileEntityAt_2_, double p_renderTileEntityAt_4_, double p_renderTileEntityAt_6_, float p_renderTileEntityAt_8_, int p_renderTileEntityAt_9_, CallbackInfo info){
        LAGGOGGLES_START = System.nanoTime();
    }

    @Inject(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntity;DDDFI)V", at = @At("HEAD"))
    public void afterRender(TileEntity tileEntityIn, double p_renderTileEntityAt_2_, double p_renderTileEntityAt_4_, double p_renderTileEntityAt_6_, float p_renderTileEntityAt_8_, int p_renderTileEntityAt_9_, CallbackInfo info){
        if(PROFILE_ENABLED.get()){
            timingManager.addGuiBlockTime(tileEntityIn.getPos(), System.nanoTime() - LAGGOGGLES_START);
        }
    }
}
