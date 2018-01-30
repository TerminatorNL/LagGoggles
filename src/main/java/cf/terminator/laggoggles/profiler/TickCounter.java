package cf.terminator.laggoggles.profiler;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.concurrent.atomic.AtomicLong;

public class TickCounter {

    public static AtomicLong ticks = new AtomicLong(0L);

    @SubscribeEvent
    public void addTick(TickEvent.ServerTickEvent e) {
        if(e.phase == TickEvent.Phase.START) {
            ticks.incrementAndGet();
        }
    }
}
