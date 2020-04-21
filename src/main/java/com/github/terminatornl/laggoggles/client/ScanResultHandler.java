package com.github.terminatornl.laggoggles.client;

import com.github.terminatornl.laggoggles.api.event.LagGogglesEvent;
import com.github.terminatornl.laggoggles.client.gui.GuiProfile;
import com.github.terminatornl.laggoggles.client.gui.LagOverlayGui;
import com.github.terminatornl.laggoggles.packet.ObjectData;
import com.github.terminatornl.laggoggles.packet.SPacketScanResult;
import com.github.terminatornl.laggoggles.profiler.ProfileResult;
import com.github.terminatornl.laggoggles.util.Calculations;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;

import static com.github.terminatornl.laggoggles.profiler.ProfileManager.LAST_PROFILE_RESULT;

public class ScanResultHandler implements IMessageHandler<SPacketScanResult, IMessage> {

    private ArrayList<ObjectData> builder = new ArrayList<>();

    @Override
    public IMessage onMessage(SPacketScanResult message, MessageContext ctx){
        final long tickCount = message.tickCount > 0 ? message.tickCount : 1;
        for(ObjectData objectData : message.DATA){
            if(Calculations.muPerTickCustomTotals(objectData.getValue(ObjectData.Entry.NANOS), tickCount) >= ClientConfig.MINIMUM_AMOUNT_OF_MICROSECONDS_THRESHOLD){
                builder.add(objectData);
            }
        }
        if(message.hasMore == false){
            ProfileResult result = new ProfileResult(message.startTime, message.endTime, tickCount, message.side, message.type);
            result.addAll(builder);
            result.lock();
            builder = new ArrayList<>();
            LAST_PROFILE_RESULT.set(result);
            LagOverlayGui.create(result);
            LagOverlayGui.show();
            GuiProfile.update();
            MinecraftForge.EVENT_BUS.post(new LagGogglesEvent.ReceivedFromServer(result));
        }
        return null;
    }

}
