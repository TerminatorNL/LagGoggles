/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package cf.terminator.laggoggles.client.gui;

import cf.terminator.laggoggles.client.ClientProxy;
import cf.terminator.laggoggles.packet.CPacketRequestEntityTeleport;
import cf.terminator.laggoggles.packet.CPacketRequestTileEntityTeleport;
import cf.terminator.laggoggles.packet.ObjectData;
import cf.terminator.laggoggles.profiler.ProfileResult;
import cf.terminator.laggoggles.profiler.ScanType;
import cf.terminator.laggoggles.util.Calculations;
import cf.terminator.laggoggles.util.Graphical;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class GuiSingleEntities extends GuiScrollingList {

    private ArrayList<GuiScanResultsWorld.LagSource> LAGSOURCES = new ArrayList<>();
    private int selected = -1;
    private final FontRenderer FONTRENDERER;
    private static final int slotHeight = 12;
    private int COLUMN_WIDTH_NANOS = 0;
    private int COLUMN_WIDTH_PERCENTAGES = 0;
    private ProfileResult result;

    public GuiSingleEntities(Minecraft client, int width, int height, int top, int bottom, int left, int screenWidth, int screenHeight, ProfileResult result) {
        super(client, width, height, top, bottom, left, slotHeight, screenWidth, screenHeight);
        FONTRENDERER = client.fontRendererObj;
        this.result = result;
        ScanType type = result.getType();
        if(type == ScanType.WORLD) {
            for (GuiScanResultsWorld.LagSource src : result.getLagSources()) {
                switch (src.data.type) {
                    case BLOCK:
                    case ENTITY:
                    case TILE_ENTITY:
                        LAGSOURCES.add(src);
                }
            }
        }else if (type == ScanType.FPS){
            for (GuiScanResultsWorld.LagSource src : result.getLagSources()) {
                switch (src.data.type) {
                    case GUI_BLOCK:
                    case GUI_ENTITY:
                        LAGSOURCES.add(src);
                }
            }
        }
        Collections.sort(LAGSOURCES);

        if(type == ScanType.WORLD) {
            for (GuiScanResultsWorld.LagSource src : LAGSOURCES) {
                COLUMN_WIDTH_NANOS = Math.max(COLUMN_WIDTH_NANOS, FONTRENDERER.getStringWidth(Calculations.muPerTickString(src.nanos, result)));
                COLUMN_WIDTH_PERCENTAGES = Math.max(COLUMN_WIDTH_PERCENTAGES, FONTRENDERER.getStringWidth(Calculations.tickPercent(src.nanos, result)));
            }
        }else if (type == ScanType.FPS){
            for (GuiScanResultsWorld.LagSource src : LAGSOURCES) {
                COLUMN_WIDTH_NANOS = Math.max(COLUMN_WIDTH_NANOS, FONTRENDERER.getStringWidth(Calculations.NFStringSimple(src.nanos, result.getTotalFrames())));
                COLUMN_WIDTH_PERCENTAGES = Math.max(COLUMN_WIDTH_PERCENTAGES, FONTRENDERER.getStringWidth(Calculations.nfPercent(src.nanos, result)));
            }
        }
    }

    @Override
    protected int getSize() {
        return LAGSOURCES.size();
    }

    @Override
    protected void elementClicked(int slot, boolean doubleClick) {
        selected = slot;
        if(doubleClick){
            switch (LAGSOURCES.get(slot).data.type) {
                case TILE_ENTITY:
                case GUI_BLOCK:
                case BLOCK:
                    ClientProxy.NETWORK_WRAPPER.sendToServer(new CPacketRequestTileEntityTeleport(LAGSOURCES.get(slot).data));
                    Minecraft.getMinecraft().displayGuiScreen(null);
                    break;
                case ENTITY:
                case GUI_ENTITY:
                    ClientProxy.NETWORK_WRAPPER.sendToServer(new CPacketRequestEntityTeleport(LAGSOURCES.get(slot).data.getValue(ObjectData.Entry.ENTITY_UUID)));
                    Minecraft.getMinecraft().displayGuiScreen(null);
                    break;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected boolean isSelected(int index) {
        return selected == index;
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
        GuiScanResultsWorld.LagSource source = LAGSOURCES.get(slot);

        double heat;
        if(result.getType() == ScanType.WORLD) {
            heat = Calculations.heat(source.nanos, result);
        }else{
            heat = Calculations.heatNF(source.nanos, result);
        }
        double[] RGB = Graphical.heatToColor(heat);
        int color = Graphical.RGBtoInt(RGB);

        int offSet = 5 + COLUMN_WIDTH_NANOS;
        if(result.getType() == ScanType.WORLD) {
            /* microseconds */
            drawStringToLeftOf(Calculations.muPerTickString(source.nanos, result), offSet, slotTop, color);
        }else if(result.getType() == ScanType.FPS){
            /* nanoseconds */
            drawStringToLeftOf(Calculations.NFStringSimple(source.nanos, result.getTotalFrames()), offSet, slotTop, color);

            offSet = offSet + 5 + COLUMN_WIDTH_PERCENTAGES;
            /* percent */
            drawStringToLeftOf(Calculations.nfPercent(source.nanos, result), offSet, slotTop, color);
        }
        offSet = offSet + 5;

        String name;
        String className;
        switch (source.data.type){
            case ENTITY:
            case GUI_ENTITY:
                name = source.data.getValue(ObjectData.Entry.ENTITY_NAME);
                className = source.data.getValue(ObjectData.Entry.ENTITY_CLASS_NAME);
                break;
            case BLOCK:
            case TILE_ENTITY:
            case GUI_BLOCK:
                name = source.data.getValue(ObjectData.Entry.BLOCK_NAME);
                className = source.data.getValue(ObjectData.Entry.BLOCK_CLASS_NAME);
                break;
            default:
                name = "Error! Please submit an issue at github";
                className = source.data.type.toString();
        }

        /* Name */
        drawString(name, offSet, slotTop, color);

        offSet = offSet + FONTRENDERER.getStringWidth(name) + 5;

        /* class */
        drawString(className, offSet, slotTop, 0x4C4C4C);
    }


    private void drawString(String text, int x, int y, int color) {
        FONTRENDERER.drawStringWithShadow(text, x, y, color);
    }

    private void drawStringToLeftOf(String text, int right, int y, int color) {
        FONTRENDERER.drawStringWithShadow(text, right-FONTRENDERER.getStringWidth(text), y, color);
    }
}