package cf.terminator.laggoggles.client.gui;

import cf.terminator.laggoggles.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;

public class DonateButton extends GuiButton {

    private ResourceLocation DONATE_TEXTURE = new ResourceLocation(Main.MODID_LOWER, "donate.png");
    private static final URI DONATE_URL;
    static {
        try {
            DONATE_URL = new URI("https://www.paypal.com/cgi-bin/webscr?return=https://minecraft.curseforge.com/projects/laggoggles?gameCategorySlug=mc-mods&projectID=283525&cn=Add+special+instructions+to+the+addon+author()&business=leon.philips12%40gmail.com&bn=PP-DonationsBF:btn_donateCC_LG.gif:NonHosted&cancel_return=https://minecraft.curseforge.com/projects/laggoggles?gameCategorySlug=mc-mods&projectID=283525&lc=US&item_name=LagGoggles+(from+curseforge.com)&cmd=_donations&rm=1&no_shipping=1&currency_code=USD");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public DonateButton(int buttonId, int x, int y) {
        super(buttonId, x, y, 90, 20, "Donate");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partials) {
        super.drawButton(mc, mouseX, mouseY, partials);
        mc.getTextureManager().bindTexture(DONATE_TEXTURE);
        drawModalRectWithCustomSizedTexture(x+3,y+3,0,0,14,14,14,14);
    }


    public static void donate(){
        Main.LOGGER.info("Attempting to open link in browser: " + DONATE_URL.toString());
        try {
            if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(DONATE_URL);
            }else {
                Main.LOGGER.info("Attempting xdg-open...");
                Runtime.getRuntime().exec("xdg-open " + DONATE_URL.toString());
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
}
