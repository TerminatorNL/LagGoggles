package com.github.terminatornl.laggoggles.util;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class RunInServerThread {

    private final Runnable runnable;

    public RunInServerThread(Runnable runnable){
        this.runnable = runnable;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e){
        MinecraftForge.EVENT_BUS.unregister(this);
        runnable.run();
    }
}
