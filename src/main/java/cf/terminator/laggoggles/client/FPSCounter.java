package cf.terminator.laggoggles.client;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FPSCounter {

    private long frames;

    @SubscribeEvent
    public void onDraw(RenderWorldLastEvent event) {
        frames++;
    }

    public void start(){
        frames = 0;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public long stop(){
        MinecraftForge.EVENT_BUS.unregister(this);
        return frames;
    }
}
