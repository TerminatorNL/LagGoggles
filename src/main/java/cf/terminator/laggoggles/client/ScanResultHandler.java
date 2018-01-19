package cf.terminator.laggoggles.client;

import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.client.gui.LagOverlayGui;
import cf.terminator.laggoggles.packet.ScanResult;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;

public class ScanResultHandler implements IMessageHandler<ScanResult, IMessage> {

    ArrayList<ScanResult.EntityData> builder = new ArrayList<>();

    @Override
    public IMessage onMessage(ScanResult message, MessageContext ctx){
        builder.addAll(message.DATA);
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
