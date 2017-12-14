package cf.terminator.laggoggles.client.gui;

import cf.terminator.laggoggles.client.ClientProxy;
import cf.terminator.laggoggles.packet.ProfileStatus;
import cf.terminator.laggoggles.packet.RequestScan;
import cf.terminator.laggoggles.packet.ScanResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;

import static cf.terminator.laggoggles.client.ClientProxy.LAST_SCAN_RESULT;
import static cf.terminator.laggoggles.client.ClientProxy.lagOverlayGui;

public class GuiProfile extends GuiScreen {

    private static final int BUTTON_START_PROFILE_ID = 0;
    private static final int BUTTON_SHOW_TOGGLE = 1;
    private static final int BUTTON_ANALYZE_RESULTS = 2;
    private static final int LABEL_ID = 3;
    public static ProfileStatus LAST_STATUS = null;

    public static void update(){
        if( Minecraft.getMinecraft().currentScreen == null || (Minecraft.getMinecraft().currentScreen instanceof GuiProfile == false)) {
            return;
        }
        Minecraft.getMinecraft().displayGuiScreen(new GuiProfile());
    }

    public GuiProfile(){
        super();
    }
    private GuiButton startProfile;
    private int seconds = 30;

    @Override
    public void initGui(){
        super.initGui();
        buttonList = new ArrayList<>();
        labelList = new ArrayList<>();

        int centerX = width/2;
        int centerY = height/2;

        boolean profileLoaded = LAST_SCAN_RESULT != null;

        startProfile = new GuiButton(BUTTON_START_PROFILE_ID, centerX - 100, centerY - 25, "Profile for " + seconds + " seconds");
        GuiButton showToggle  = new GuiButton(BUTTON_SHOW_TOGGLE, centerX - 100, centerY +  5, lagOverlayGui.isShowing.get() ? "Hide latest scan results" : "Show latest scan results");
        GuiButton analyzeResults  = new GuiButton(BUTTON_ANALYZE_RESULTS, centerX - 100, centerY +  30, "Analyze results");

        if(LAST_STATUS != null && LAST_STATUS.isProfiling == true){
            startProfile.enabled = false;
            startProfile.displayString = LAST_STATUS.issuedBy + " > " + LAST_STATUS.length + " seconds.";
        }

        showToggle.enabled = profileLoaded;
        analyzeResults.enabled = profileLoaded;

        addButton(startProfile);
        addButton(showToggle);
        addButton(analyzeResults);

        GuiLabel scrollHint = new GuiLabel(fontRendererObj, LABEL_ID, centerX - 100, centerY - 55, 200, 20, 0xFFFFFF);
        scrollHint.addLine("Scroll while hovering over the button");
        scrollHint.addLine("to change time time!");
        labelList.add(scrollHint);
    }

    @Override
    public void handleMouseInput() throws IOException{
        if(startProfile.isMouseOver() && startProfile.enabled){
            int wheel = Mouse.getDWheel();
            if(wheel != 0) {
                seconds = seconds + ((wheel / 120) * 5); /* 1 Click is 120, 1 click is 5 seconds */
                seconds = Math.max(seconds, 5);
                startProfile.displayString = "Profile for " + seconds + " seconds";
            }
        }
        super.handleMouseInput();
    }

    public void startProfile(){
        RequestScan scan = new RequestScan();
        scan.length = seconds;
        ClientProxy.NETWORK_WRAPPER.sendToServer(scan);
        startProfile.enabled = false;
        startProfile.displayString = "Sending command...";
    }

    public void analyzeResults(){
        ArrayList<GuiScanResults.LagSource> entityList = new ArrayList<>();
        for(ScanResult.EntityData entity : LAST_SCAN_RESULT.DATA){
            if(entity.name.equals("Dead") == false) {
                entityList.add(new GuiScanResults.LagSource(entity.nanos, entity));
            }
        }
        mc.displayGuiScreen(new GuiScanResults(entityList));
    }

    @Override
    public void actionPerformed(GuiButton gui){
        switch (gui.id){
            case BUTTON_START_PROFILE_ID:
                startProfile();
                break;
            case BUTTON_SHOW_TOGGLE:
                if(lagOverlayGui.isShowing.get()) {
                    lagOverlayGui.hide();
                    Minecraft.getMinecraft().displayGuiScreen(null);
                }else{
                    lagOverlayGui.show();
                    Minecraft.getMinecraft().displayGuiScreen(null);
                }
                break;
            case BUTTON_ANALYZE_RESULTS:
                analyzeResults();
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }
}
