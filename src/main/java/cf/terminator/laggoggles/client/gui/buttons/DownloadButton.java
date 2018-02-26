package cf.terminator.laggoggles.client.gui.buttons;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.util.Perms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.ArrayList;

import static cf.terminator.laggoggles.client.ServerDataPacketHandler.PERMISSION;
import static cf.terminator.laggoggles.client.gui.GuiProfile.getSecondsLeftForMessage;

public class DownloadButton extends GuiButton{

    private ResourceLocation DOWNLOAD_TEXTURE = new ResourceLocation(Main.MODID_LOWER, "download.png");
    private final GuiScreen parent;

    public DownloadButton(GuiScreen parent, int buttonId, int x, int y) {
        super(buttonId, x, y, 20, 20, "");
        this.parent = parent;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partials) {
        super.drawButton(mc, mouseX, mouseY, partials);
        mc.getTextureManager().bindTexture(DOWNLOAD_TEXTURE);
        drawModalRectWithCustomSizedTexture(x+3,y+3,0,0,14,14,14,14);
        if(hovered){
            ArrayList<String> hover = new ArrayList<>();
            hover.add("Download the latest available");
            hover.add("world result from the server.");
            if(PERMISSION != Perms.Permission.FULL) {
                hover.add("");
                hover.add("Because you're not opped, the results");
                hover.add("will be trimmed to your surroundings");

                if(getSecondsLeftForMessage() >= 0){
                    hover.add("");
                    hover.add(TextFormatting.GRAY + "Remember: There's a cooldown on this, you");
                    hover.add(TextFormatting.GRAY + "may have to wait before you can use it again.");
                }
            }

            GuiUtils.drawHoveringText(hover, mouseX, mouseY, parent.width, parent.height, -1, mc.fontRenderer);

        }
    }
}
