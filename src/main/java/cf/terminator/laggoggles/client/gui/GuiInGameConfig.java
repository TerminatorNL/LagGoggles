package cf.terminator.laggoggles.client.gui;


import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.client.ConfigData;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

public class GuiInGameConfig extends GuiConfig {

    public GuiInGameConfig(GuiScreen parent) {
        super(parent, Main.MODID_LOWER, false, false, Main.MODID + " configuration", ConfigData.class);
        titleLine2 = "Hover with the mouse over a variable to see a description";
    }
}

