package cf.terminator.laggoggles.client;

import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.client.gui.LagOverlayGui;
import cf.terminator.laggoggles.packet.SPacketScanResult;
import cf.terminator.laggoggles.util.Calculations;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;

public class ScanResultHandler implements IMessageHandler<SPacketScanResult, IMessage> {

    ArrayList<SPacketScanResult.EntityData> builder = new ArrayList<>();

    @Override
    public IMessage onMessage(SPacketScanResult message, MessageContext ctx){
        for(SPacketScanResult.EntityData entityData : message.DATA){
            if(Calculations.muPerTickCustomTotals(entityData.getValue(SPacketScanResult.EntityData.Entry.NANOS), message.TOTAL_TICKS) >= ClientConfig.MINIMUM_AMOUNT_OF_MICROSECONDS_THRESHOLD){
                builder.add(entityData);
            }
        }
        if(message.hasMore == false){
            message.DATA = new ArrayList<>(builder);
            builder = new ArrayList<>();
            ClientProxy.LAST_SCAN_RESULT = message;
            ClientProxy.lagOverlayGui.hide();
            ClientProxy.lagOverlayGui = new LagOverlayGui(message.DATA);
            ClientProxy.lagOverlayGui.show();
            GuiProfile.update();
        }
        return null;
    }

}
