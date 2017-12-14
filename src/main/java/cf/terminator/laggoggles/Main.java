package cf.terminator.laggoggles;

import cf.terminator.laggoggles.client.ClientProxy;
import cf.terminator.laggoggles.util.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.Logger;

@Mod(modid = Main.MODID_LOWER, name = Main.MODID, version = Main.VERSION, acceptableRemoteVersions = "*")
@IFMLLoadingPlugin.SortingIndex(1001)
public class Main {
    public static final String MODID = "LagGoggles";
    public static final String MODID_LOWER = "laggoggles";
    public static final String VERSION = "1.0";
    public static Logger LOGGER;

    @SidedProxy(
            modId = Main.MODID_LOWER,
            serverSide = "cf.terminator.laggoggles.util.CommonProxy",
            clientSide = "cf.terminator.laggoggles.client.ClientProxy"
    )
    public static CommonProxy proxy;

    @EventHandler
    public void preinit(FMLPreInitializationEvent e){
        LOGGER = e.getModLog();
        proxy.preinit(e);
        Main.LOGGER.info("Registered sided proxy for: " + (proxy instanceof ClientProxy ? "Client" : "Dedicated server"));
    }

    @EventHandler
    public void postinit(FMLPostInitializationEvent e) {
        proxy.postinit(e);
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent e){
        proxy.serverStartingEvent(e);
    }


}
