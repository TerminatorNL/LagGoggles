package cf.terminator.laggoggles.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class OptionsButton extends GuiButton {


    public OptionsButton(int buttonId, int x, int y) {
        super(buttonId, x, y, 90, 20, "Options");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks){
        super.drawButton(mc, mouseX, mouseY, partialTicks);
        //mc.getTextureManager().bindTexture(DONATE_TEXTURE);
        //drawModalRectWithCustomSizedTexture(x+3,y+3,0,0,14,14,14,14);
    }

}
