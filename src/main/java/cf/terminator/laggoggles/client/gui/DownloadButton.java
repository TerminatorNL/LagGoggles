package cf.terminator.laggoggles.client.gui;

import cf.terminator.laggoggles.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

public class DownloadButton extends GuiButton{

    private ResourceLocation DOWNLOAD_TEXTURE = new ResourceLocation(Main.MODID_LOWER, "download.png");

    public DownloadButton(int buttonId, int x, int y) {
        super(buttonId, x, y, 20, 20, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partials) {
        super.drawButton(mc, mouseX, mouseY, partials);
        mc.getTextureManager().bindTexture(DOWNLOAD_TEXTURE);
        drawModalRectWithCustomSizedTexture(x+3,y+3,0,0,14,14,14,14);
    }
}
