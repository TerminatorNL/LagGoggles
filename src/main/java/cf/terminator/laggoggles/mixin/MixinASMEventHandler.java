package cf.terminator.laggoggles.mixin;

import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ASMEventHandler.class, priority = 1001, remap = false)
public abstract class MixinASMEventHandler implements IEventListener, cf.terminator.laggoggles.util.ASMEventHandler {

    @Shadow
    private ModContainer owner;

    @Override
    public ModContainer getOwner(){
        return owner;
    }

}
