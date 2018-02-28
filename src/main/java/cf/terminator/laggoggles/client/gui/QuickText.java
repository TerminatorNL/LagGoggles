package cf.terminator.laggoggles.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class QuickText {

    private final FontRenderer renderer;
    private final String text;

    public QuickText(String text){
        this.renderer = Minecraft.getMinecraft().fontRendererObj;
        this.text = text;
    }

    @SubscribeEvent
    public void onDraw(RenderGameOverlayEvent.Post event){
        renderer.drawStringWithShadow(text, event.getResolution().getScaledWidth()/2 - renderer.getStringWidth(text) / 2, 5, 0xFFFFFF);
    }

    public void show(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void hide(){
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
