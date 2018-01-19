package cf.terminator.laggoggles.client.gui;

import cf.terminator.laggoggles.util.Calculations;
import cf.terminator.laggoggles.util.Graphical;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class GuiEntityTypes extends GuiScrollingList {

    private TreeMap<Long, String> DATA = new TreeMap<>();

    private final FontRenderer FONTRENDERER;
    private static final int slotHeight = 12;
    private int COLUMN_WIDTH_NANOS = 0;
    private int COLUMN_WIDTH_PERCENTAGES = 0;

    public GuiEntityTypes(Minecraft client, int width, int height, int top, int bottom, int left, int screenWidth, int screenHeight, ArrayList<GuiScanResults.LagSource> lagSources) {
        super(client, width, height, top, bottom, left, slotHeight, screenWidth, screenHeight);
        FONTRENDERER = client.fontRendererObj;

        HashMap<String, Long> totals = new HashMap<>();
        for(GuiScanResults.LagSource src : lagSources){
            if(totals.containsKey(src.data.className) == false){
                totals.put(src.data.className, src.nanos);
            }else{
                totals.put(src.data.className, src.nanos + totals.get(src.data.className));
            }
        }

        for(Map.Entry<String, Long> entry : totals.entrySet()){
            DATA.put(entry.getValue(), entry.getKey());
            COLUMN_WIDTH_NANOS = Math.max(COLUMN_WIDTH_NANOS, FONTRENDERER.getStringWidth(Calculations.muPerTickString(entry.getValue())));
            COLUMN_WIDTH_PERCENTAGES = Math.max(COLUMN_WIDTH_PERCENTAGES, FONTRENDERER.getStringWidth(Calculations.tickPercent(entry.getValue())));
        }
    }


    @Override
    protected int getSize() {
        return DATA.size();
    }

    @Override
    protected void elementClicked(int slot, boolean doubleClick) {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected boolean isSelected(int index) {
        return false;
    }

    @Override
    protected void drawBackground() {


    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput(left, top);
    }

    @Override
    protected void drawSlot(int slot, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        if(slot == -1){
            return;
        }
        Long nanos = DATA.descendingKeySet().toArray(new Long[0])[slot];
        String name = DATA.get(nanos);

        double heat = Calculations.heat(nanos);
        double[] RGB = Graphical.heatToColor(heat);
        int color = Graphical.RGBtoInt(RGB);
        /* microseconds */
        drawStringToLeftOf(Calculations.muPerTickString(nanos),left + COLUMN_WIDTH_NANOS + 5, slotTop, color);

        /* Percent */
        drawString(Calculations.tickPercent(nanos), left + COLUMN_WIDTH_NANOS + 10, slotTop, color);

        /* Name */
        drawString(name, left + COLUMN_WIDTH_NANOS + 10 + FONTRENDERER.getStringWidth(Calculations.tickPercent(nanos)) + 5, slotTop, 0x4C4C4C);
    }


    private void drawString(String text, int x, int y, int color) {
        FONTRENDERER.drawStringWithShadow(text, x, y, color);
    }

    private void drawStringToLeftOf(String text, int right, int y, int color) {
        FONTRENDERER.drawStringWithShadow(text, right-FONTRENDERER.getStringWidth(text), y, color);
    }

}
