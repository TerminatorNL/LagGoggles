package cf.terminator.laggoggles.client.gui;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.packet.ScanResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

public class GuiScanResults extends GuiScreen {

    private final FontRenderer FONTRENDERER;
    public final TreeMap<Integer, LagSource> DATA_ID_TO_SOURCE = new TreeMap<>();
    public final TreeMap<LagSource, Integer> DATA_SOURCE_TO_ID = new TreeMap<>();

    private GuiSingleEntities guiSingleEntities;
    private GuiEntityTypes guiEntityTypes;

    private ArrayList<LagSource> data;

    public GuiScanResults(ArrayList<LagSource> data){
        super();
        FONTRENDERER = Minecraft.getMinecraft().fontRendererObj;
        this.data = data;
        int i = 0;
        for(LagSource source : data){
            DATA_ID_TO_SOURCE.put(i, source);
            DATA_SOURCE_TO_ID.put(source, i);
            i++;
        }

    }

    @Override
    public void initGui() {
        super.initGui();

        /*                                            width  , height     , top, bottom, left      , screenWidth, screenHeight, LAGSOURCES*/
        guiSingleEntities = new GuiSingleEntities(mc, width/2, height - 25, 45 , height,  0        , width      , height      , data);
        guiEntityTypes    = new GuiEntityTypes(   mc, width/2, height - 25, 45 , height,  width/2  , width      , height      , data);

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawBackground(0);
        super.drawScreen(mouseX, mouseY, partialTicks);
        guiSingleEntities.drawScreen(mouseX, mouseY, partialTicks);
        guiEntityTypes.drawScreen(mouseX, mouseY, partialTicks);
        drawString(Main.MODID + ": profile data", 5, 5, 0xFFFFFF);
        drawString("Times are presented in microseconds", 5, 15, 0xCCCCCC);
        drawString("Single entities", 5, 35, 0xFFFFFF);
        drawString("Entities by type", width/2, 35, 0xFFFFFF);
    }


    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }


    @Override
    public void handleMouseInput() throws IOException{
        super.handleMouseInput();
        guiSingleEntities.handleMouseInput();
        guiEntityTypes.handleMouseInput();
    }

    private void drawString(String text, int x, int y, int color) {
        FONTRENDERER.drawStringWithShadow(text, (float) x, (float)y, color);
    }


    /* LAGSOURCE */
    public static class LagSource implements Comparable<LagSource>{

        final long nanos;
        final ScanResult.EntityData data;

        public LagSource(long nanos, ScanResult.EntityData e){
            this.nanos = nanos;
            data = e;
        }

        @Override
        public int compareTo(LagSource other) {
            boolean thisIsBigger = this.nanos > other.nanos;
            if(thisIsBigger) {
                return -1;
            }
            boolean thisIsSmaller= this.nanos < other.nanos;
            if(thisIsSmaller){
                return 1;
            }else{
                return 0;
            }
        }
    }
}
