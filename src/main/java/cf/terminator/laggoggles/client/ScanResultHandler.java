package cf.terminator.laggoggles.client;

import cf.terminator.laggoggles.api.event.LagGogglesEvent;
import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.client.gui.LagOverlayGui;
import cf.terminator.laggoggles.packet.ObjectData;
import cf.terminator.laggoggles.packet.SPacketScanResult;
import cf.terminator.laggoggles.profiler.ProfileResult;
import cf.terminator.laggoggles.util.Calculations;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;

import static cf.terminator.laggoggles.profiler.ProfileManager.LAST_PROFILE_RESULT;

public class ScanResultHandler implements IMessageHandler<SPacketScanResult, IMessage> {

    private ArrayList<ObjectData> builder = new ArrayList<>();

    @Override
    public IMessage onMessage(SPacketScanResult message, MessageContext ctx){
        final lon tickCount = message.tickCount > 0 ? message.tickCount : 1;
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
