package cf.terminator.laggoggles.client;

import cf.terminator.laggoggles.CommonProxy;
import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.client.gui.KeyHandler;
import cf.terminator.laggoggles.client.gui.LagOverlayGui;
import cf.terminator.laggoggles.packet.CPacketRequestServerData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;

import static cf.terminator.laggoggles.client.ServerDataPacketHandler.RECEIVED_RESULT;
import static cf.terminator.laggoggles.profiler.ProfileManager.LAST_PROFILE_RESULT;

public class ClientProxy extends CommonProxy {

    @Override
    public void postinit(FMLPostInitializationEvent e){
        super.postinit(e);
        ClientRegistry.registerKeyBinding(new KeyHandler("Profile GUI", Keyboard.KEY_INSERT, Main.MODID, new KeyHandler.CallBack() {
            @Override
            public void onPress() {
                Minecraft.getMinecraft().displayGuiScreen(new GuiProfile());
            }
        }));

        MinecraftForge.EVENT_BUS.register(new Object(){
            @SubscribeEvent
            public void onLogin(FMLNetworkEvent.ClientConnectedToServerEvent e){
                RECEIVED_RESULT = false;
                LagOverlayGui.destroy();
                LAST_PROFILE_RESULT.set(null);
                new ClientLoginAction().activate();
            }
        });
    }

    private class ClientLoginAction {

        int count = 0;

        @SubscribeEvent
        public void onTick(TickEvent.ClientTickEvent e){
            if(RECEIVED_RESULT == true){
                MinecraftForge.EVENT_BUS.unregister(this);
                return;
            }
            if(e.phase != TickEvent.Phase.START){
                return;
            }
            if(count++ % 5 == 0){
                NETWORK_WRAPPER.sendToServer(new CPacketRequestServerData());
            }
        }

        public void activate(){
            MinecraftForge.EVENT_BUS.register(this);
        }

    }
}
