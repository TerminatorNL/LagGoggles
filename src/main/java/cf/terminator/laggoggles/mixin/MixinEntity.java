package cf.terminator.laggoggles.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static cf.terminator.laggoggles.profiler.world.ProfileManager.PROFILE_ENABLED;
import static cf.terminator.laggoggles.profiler.world.ProfileManager.worldTimingManager;

@Mixin(value = Entity.class)
public abstract class MixinEntity {

    @Shadow
    protected UUID entityUniqueID;

    @Shadow
    public int dimension;

    private Long LAGGOGGLES_START = null;

    @Inject(method = "onUpdate", at = @At("HEAD"))
    public void onEntityUpdateHEAD(CallbackInfo info){
        LAGGOGGLES_START = System.nanoTime();
    }

    @Inject(method = "onUpdate", at = @At("RETURN"))
    public void onEntityUpdateRETURN(CallbackInfo info){
        if(PROFILE_ENABLED.get() && LAGGOGGLES_START != null){
            worldTimingManager.addEntityTime(dimension, entityUniqueID, System.nanoTime() - LAGGOGGLES_START);
        }
    }


}
