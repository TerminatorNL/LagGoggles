package com.github.terminatornl.laggoggles.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class QuickText {

    private final FontRenderer renderer;
    private final String text;

    public QuickText(String text){
        this.renderer = Minecraft.getMinecraft().fontRenderer;
        this.text = text;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDraw(RenderGameOverlayEvent event){
        if(event.getType() == RenderGameOverlayEvent.ElementType.SUBTITLES){
            renderer.drawStringWithShadow(text, event.getResolution().getScaledWidth()/2 - renderer.getStringWidth(text) / 2, 5, 0xFFFFFF);
        }
    }

    public void show(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void hide(){
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
