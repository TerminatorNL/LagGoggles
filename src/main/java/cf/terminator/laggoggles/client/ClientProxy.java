package cf.terminator.laggoggles.client;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.client.gui.KeyHandler;
import cf.terminator.laggoggles.client.gui.LagOverlayGui;
import cf.terminator.laggoggles.packet.ScanResult;
import cf.terminator.laggoggles.util.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;

import java.io.File;

public class ClientProxy extends CommonProxy {

    public static LagOverlayGui lagOverlayGui = new LagOverlayGui();
    public static ScanResult LAST_SCAN_RESULT = null;
    public static File CONFIG_FILE;

    public void preinit(FMLPreInitializationEvent e){
        CONFIG_FILE = e.getSuggestedConfigurationFile();
    }

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
                if(lagOverlayGui != null){
                    lagOverlayGui.hide();
                }
                lagOverlayGui = new LagOverlayGui();
                LAST_SCAN_RESULT = null;
            }
        });
    }

}
