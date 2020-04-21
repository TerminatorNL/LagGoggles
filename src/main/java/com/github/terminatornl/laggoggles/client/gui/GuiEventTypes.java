package com.github.terminatornl.laggoggles.client.gui;

import com.github.terminatornl.laggoggles.Main;
import com.github.terminatornl.laggoggles.packet.ObjectData;
import com.github.terminatornl.laggoggles.profiler.ProfileResult;
import com.github.terminatornl.laggoggles.profiler.ScanType;
import com.github.terminatornl.laggoggles.profiler.TimingManager;
import com.github.terminatornl.laggoggles.util.Calculations;
import com.github.terminatornl.laggoggles.util.Graphical;
import com.github.terminatornl.laggoggles.util.Perms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.io.IOException;
import java.util.Collections;
import java.util.TreeSet;

import static com.github.terminatornl.laggoggles.client.ServerDataPacketHandler.NON_OPS_CAN_SEE_EVENT_SUBSCRIBERS;
import static com.github.terminatornl.laggoggles.client.ServerDataPacketHandler.PERMISSION;
import static com.github.terminatornl.laggoggles.util.Graphical.formatClassName;

public class GuiEventTypes extends GuiScrollingList {

    final private FontRenderer FONTRENDERER;
    private static final int slotHeight = 24;
    private int COLUMN_WIDTH_NANOS = 0;
    private TreeSet<GuiScanResultsWorld.LagSource> DATA = new TreeSet<>(Collections.reverseOrder());
    private final ProfileResult result;

    public GuiEventTypes(Minecraft client, int width, int height, int top, int bottom, int left, int screenWidth, int screenHeight, ProfileResult result) {
        super(client, width, height, top, bottom, left, slotHeight, screenWidth, screenHeight);
        FONTRENDERER = client.fontRenderer;
        this.result = result;

        for(GuiScanResultsWorld.LagSource src : result.getLagSources()){
            if(src.data.type == ObjectData.Type.EVENT_BUS_LISTENER){
                TimingManager.EventTimings.ThreadType type = TimingManager.EventTimings.ThreadType.values()[src.data.<Integer>getValue(ObjectData.Entry.EVENT_BUS_THREAD_TYPE)];
                if((result.getType() == ScanType.FPS && type == TimingManager.EventTimings.ThreadType.CLIENT) || (result.getType() == ScanType.WORLD && type != TimingManager.EventTimings.ThreadType.CLIENT)){


                    /* This removes the LagGoggles tooltip from the results, as it's only visible while profiling, it's clutter. */
                    if(src.data.<String>getValue(ObjectData.Entry.EVENT_BUS_LISTENER).contains(Main.MODID)){
                        if(src.data.<String>getValue(ObjectData.Entry.EVENT_BUS_EVENT_CLASS_NAME).equals(formatClassName(net.minecraftforge.client.event.RenderGameOverlayEvent.Post.class.toString()))) {
                            continue;
                        }
                    }

                    DATA.add(src);
                    COLUMN_WIDTH_NANOS = Math.max(COLUMN_WIDTH_NANOS, FONTRENDERER.getStringWidth(getMuStringFor(src)));
                }
            }
        }

    }


    @Override
    protected int getSize() {
        if((NON_OPS_CAN_SEE_EVENT_SUBSCRIBERS == false && result.getType() == ScanType.WORLD ) && PERMISSION.ordinal() < Perms.Permission.FULL.ordinal()){
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
        if((NON_OPS_CAN_SEE_EVENT_SUBSCRIBERS == false && result.getType() == ScanType.WORLD ) && PERMISSION.ordinal() < Perms.Permission.FULL.ordinal()){
            displayCantSeeResults(slotTop);
            return;
        }
        if(slot > DATA.size()){
            return;
        }
        GuiScanResultsWorld.LagSource lagSource = DATA.descendingSet().toArray(new GuiScanResultsWorld.LagSource[0])[slot];
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
        }
        double heat = Calculations.heatThread(lagSource, result);
        double[] RGB = Graphical.heatToColor(heat);
        int color = Graphical.RGBtoInt(RGB);

        /* times */
        drawStringToLeftOf(getMuStringFor(lagSource),left + COLUMN_WIDTH_NANOS + 5, slotTop, color);

        /* Percent */
        String percentString = getPercentStringFor(lagSource);
        drawString(percentString, left + COLUMN_WIDTH_NANOS + 10, slotTop, color);
        int percentOffSet = FONTRENDERER.getStringWidth(percentString);
        int offSet = percentOffSet;

        /* Name and blocking */
        String listener = lagSource.data.getValue(ObjectData.Entry.EVENT_BUS_LISTENER);
        drawString(listener, left + COLUMN_WIDTH_NANOS + 10 + offSet + 5, slotTop, 0x4C4C4C);

        offSet = offSet + FONTRENDERER.getStringWidth(listener);
        drawString(threadType, left + COLUMN_WIDTH_NANOS + 10 + offSet + 10, slotTop , threadColor);

        /* Event class */
        drawString(lagSource.data.getValue(ObjectData.Entry.EVENT_BUS_EVENT_CLASS_NAME)   , left + COLUMN_WIDTH_NANOS + 10 + percentOffSet + 5, slotTop + 12, 0x4C4C4C);
    }

    private String getMuStringFor(GuiScanResultsWorld.LagSource source){
        TimingManager.EventTimings.ThreadType type = TimingManager.EventTimings.ThreadType.values()[source.data.<Integer>getValue(ObjectData.Entry.EVENT_BUS_THREAD_TYPE)];
        if(type == TimingManager.EventTimings.ThreadType.CLIENT) {
            return Calculations.NFStringSimple(source.nanos, result.getTotalFrames());
        }else if (type == TimingManager.EventTimings.ThreadType.ASYNC){
            return  "No impact";
        }else if(type == TimingManager.EventTimings.ThreadType.SERVER){
            return Calculations.muPerTickString(source.nanos, result);
        }else{
            throw new IllegalStateException("Terminator_NL forgot to add code here... Please submit an issue at github!");
        }
    }

    private String getPercentStringFor(GuiScanResultsWorld.LagSource source){
        TimingManager.EventTimings.ThreadType type = TimingManager.EventTimings.ThreadType.values()[source.data.<Integer>getValue(ObjectData.Entry.EVENT_BUS_THREAD_TYPE)];
        if(type == TimingManager.EventTimings.ThreadType.CLIENT) {
            return Calculations.nfPercent(source.nanos, result);
        }else if (type == TimingManager.EventTimings.ThreadType.ASYNC){
            return  "";
        }else if(type == TimingManager.EventTimings.ThreadType.SERVER){
            return Calculations.tickPercent(source.nanos, result);
        }else{
            throw new IllegalStateException("Terminator_NL forgot to add code here... Please submit an issue at github!");
        }
    }

    private void drawString(String text, int x, int y, int color) {
        FONTRENDERER.drawStringWithShadow(text, x, y, color);
    }

    private void drawStringToLeftOf(String text, int right, int y, int color) {
        FONTRENDERER.drawStringWithShadow(text, right-FONTRENDERER.getStringWidth(text), y, color);
    }

}
