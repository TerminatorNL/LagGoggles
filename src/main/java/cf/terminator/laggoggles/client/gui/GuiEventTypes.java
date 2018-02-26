package cf.terminator.laggoggles.client.gui;

import cf.terminator.laggoggles.packet.ObjectData;
import cf.terminator.laggoggles.profiler.ProfileResult;
import cf.terminator.laggoggles.profiler.ScanType;
import cf.terminator.laggoggles.profiler.TimingManager;
import cf.terminator.laggoggles.util.Calculations;
import cf.terminator.laggoggles.util.Graphical;
import cf.terminator.laggoggles.util.Perms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.io.IOException;
import java.util.TreeMap;

import static cf.terminator.laggoggles.client.ServerDataPacketHandler.NON_OPS_CAN_SEE_EVENT_SUBSCRIBERS;
import static cf.terminator.laggoggles.client.ServerDataPacketHandler.PERMISSION;

public class GuiEventTypes extends GuiScrollingList {

    final private FontRenderer FONTRENDERER;
    private static final int slotHeight = 24;
    private int COLUMN_WIDTH_NANOS = 0;
    private TreeMap<Long, GuiScanResultsWorld.LagSource> DATA = new TreeMap<>();
    private final ProfileResult result;

    public GuiEventTypes(Minecraft client, int width, int height, int top, int bottom, int left, int screenWidth, int screenHeight, ProfileResult result) {
        super(client, width, height, top, bottom, left, slotHeight, screenWidth, screenHeight);
        FONTRENDERER = client.fontRenderer;
        this.result = result;
        for(GuiScanResultsWorld.LagSource src : result.getLagSources()){
            if(src.data.type == ObjectData.Type.EVENT_BUS_LISTENER){
                if(result.getType() == ScanType.WORLD || TimingManager.EventTimings.ThreadType.values()[src.data.<Integer>getValue(ObjectData.Entry.EVENT_BUS_THREAD_TYPE)] == TimingManager.EventTimings.ThreadType.CLIENT) {
                    DATA.put(src.data.getValue(ObjectData.Entry.NANOS), src);
                    COLUMN_WIDTH_NANOS = Math.max(COLUMN_WIDTH_NANOS, FONTRENDERER.getStringWidth(Calculations.muPerTickString(src.data.getValue(ObjectData.Entry.NANOS), result)));
                }
            }
        }
    }


    @Override
    protected int getSize() {
        if(NON_OPS_CAN_SEE_EVENT_SUBSCRIBERS == false && PERMISSION.ordinal() < Perms.Permission.FULL.ordinal()){
            return 1;
        }else {
            return DATA.size();
        }
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

    public void displayCantSeeResults(int slotTop){
        drawString("You can't see these results because the", left + 10, slotTop, 0x4C4C4C);
        drawString("server has disabled it in their config.", left + 10, slotTop + 12, 0x4C4C4C);
    }

    @Override
    protected void drawSlot(int slot, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        if(NON_OPS_CAN_SEE_EVENT_SUBSCRIBERS == false && PERMISSION.ordinal() < Perms.Permission.FULL.ordinal()){
            displayCantSeeResults(slotTop);
            return;
        }
        if(slot > DATA.size()){
            return;
        }
        long nanos = DATA.descendingKeySet().toArray(new Long[0])[slot];
        GuiScanResultsWorld.LagSource lagSource = DATA.get(nanos);
        int threadColor = 0x00FF00;
        String threadType = "(Asynchronous)";
        switch (TimingManager.EventTimings.ThreadType.values()[lagSource.data.<Integer>getValue(ObjectData.Entry.EVENT_BUS_THREAD_TYPE)]){
            case CLIENT:
                threadType = "(Gui thread)";
                threadColor = 0xFF0000;
                break;
            case SERVER:
                threadType = "(Server thread)";
                threadColor = 0xFF0000;
                break;
            case UNKNOWN:
                threadType = "(Unknown)";
                threadColor = 0xCCCCCC;
                break;
        }
        double heat = Calculations.heat(nanos, result);
        double[] RGB = Graphical.heatToColor(heat);
        int color = Graphical.RGBtoInt(RGB);
        /* microseconds */
        drawStringToLeftOf(Calculations.muPerTickString(nanos, result),left + COLUMN_WIDTH_NANOS + 5, slotTop, color);

        /* Percent */
        drawString(Calculations.tickPercent(nanos, result), left + COLUMN_WIDTH_NANOS + 10, slotTop, color);

        /* Name and blocking */
        drawString(lagSource.data.getValue(ObjectData.Entry.EVENT_BUS_LISTENER_CLASS_NAME), left + COLUMN_WIDTH_NANOS + 10 + FONTRENDERER.getStringWidth(Calculations.tickPercent(nanos, result)) + 5, slotTop     , 0x4C4C4C);
        drawString(threadType, left + COLUMN_WIDTH_NANOS + 10 + FONTRENDERER.getStringWidth(Calculations.tickPercent(nanos, result)) + 5 + FONTRENDERER.getStringWidth(lagSource.data.getValue(ObjectData.Entry.EVENT_BUS_LISTENER_CLASS_NAME)) + 5, slotTop , threadColor);

        /* Event class */
        drawString(lagSource.data.getValue(ObjectData.Entry.EVENT_BUS_EVENT_CLASS_NAME)   , left + COLUMN_WIDTH_NANOS + 10 + FONTRENDERER.getStringWidth(Calculations.tickPercent(nanos, result)) + 5, slotTop + 12, 0x4C4C4C);
    }


    private void drawString(String text, int x, int y, int color) {
        FONTRENDERER.drawStringWithShadow(text, x, y, color);
    }

    private void drawStringToLeftOf(String text, int right, int y, int color) {
        FONTRENDERER.drawStringWithShadow(text, right-FONTRENDERER.getStringWidth(text), y, color);
    }

}
