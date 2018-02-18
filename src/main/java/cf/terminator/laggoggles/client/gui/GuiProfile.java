package cf.terminator.laggoggles.client.gui;

import cf.terminator.laggoggles.client.ClientProxy;
import cf.terminator.laggoggles.client.ServerDataPacketHandler;
import cf.terminator.laggoggles.packet.*;
import cf.terminator.laggoggles.util.Perms;
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
    private static final int BUTTON_SHOW_TOGGLE      = 1;
    private static final int BUTTON_ANALYZE_RESULTS  = 2;
    private static final int LABEL_ID                = 3;
    private static final int BUTTON_DONATE           = 4;
    private static final int BUTTON_OPTIONS          = 5;
    private static final int BUTTON_DOWNLOAD         = 6;

    public static SPacketProfileStatus LAST_STATUS = null;
    public static long PROFILE_END_TIME = 0L;

    public static SPacketMessage MESSAGE = null;
    public static long MESSAGE_END_TIME = 0L;


    private GuiButton startProfile;
    private DownloadButton downloadButton;
    private GuiButton optionsButton;
    private int seconds = Math.min(30, ServerDataPacketHandler.MAX_SECONDS);

    public GuiProfile(){
        super();
    }

    public static void update(){
        if(isThisGuiOpen() == false){
            return;
        }
        Minecraft.getMinecraft().displayGuiScreen(new GuiProfile());
    }

    public static void open(){
        Minecraft.getMinecraft().displayGuiScreen(new GuiProfile());
    }

    private static boolean isThisGuiOpen(){
        return Minecraft.getMinecraft().currentScreen != null && (Minecraft.getMinecraft().currentScreen instanceof GuiProfile == true);
    }

    @Override
    public void initGui(){
        super.initGui();

        buttonList = new ArrayList<>();
        labelList = new ArrayList<>();

        int centerX = width/2;
        int centerY = height/2;

        boolean profileLoaded = LAST_SCAN_RESULT != null;

        startProfile = new ProfileButton(BUTTON_START_PROFILE_ID, centerX - 100, centerY - 25, "Profile for " + seconds + " seconds");
        downloadButton = new DownloadButton(this, BUTTON_DOWNLOAD, centerX + 80, centerY - 25);
        optionsButton = new OptionsButton(BUTTON_OPTIONS, centerX - 100, centerY + 75);
        GuiButton showToggle  = new GuiButton(BUTTON_SHOW_TOGGLE, centerX - 100, centerY +  5, lagOverlayGui.isShowing.get() ? "Hide latest scan results" : "Show latest scan results");
        GuiButton analyzeResults  = new GuiButton(BUTTON_ANALYZE_RESULTS, centerX - 100, centerY +  30, "Analyze results");

        updateButton();

        showToggle.enabled = profileLoaded;
        analyzeResults.enabled = profileLoaded;

        addButton(startProfile);
        addButton(showToggle);
        addButton(analyzeResults);
        addButton(new DonateButton(BUTTON_DONATE, centerX + 10, centerY + 75));
        addButton(optionsButton);
        GuiLabel scrollHint = new GuiLabel(fontRenderer, LABEL_ID, centerX - 100, centerY - 55, 200, 20, 0xFFFFFF);
        scrollHint.addLine("Scroll while hovering over the button");
        scrollHint.addLine("to change time time!");
        labelList.add(scrollHint);
        addButton(downloadButton);
    }

    private Runnable buttonUpdateTask = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(500);
                if(isThisGuiOpen() == false){
                    return;
                }
                updateButton();
            } catch (InterruptedException ignored){}
        }
    };

    private void updateButton(){
        if(getSecondsLeftForMessage() >= 0){
            startProfile.displayString = MESSAGE.message;
            startProfile.enabled = false;
            new Thread(buttonUpdateTask).start();
        }else if(getSecondsLeftForProfiler() >= 0){
            startProfile.displayString = LAST_STATUS.issuedBy + " > " + getSecondsLeftForProfiler() + " seconds.";
            startProfile.enabled = false;
            new Thread(buttonUpdateTask).start();
        }else{
            if(ServerDataPacketHandler.PERMISSION.ordinal() >= Perms.Permission.START.ordinal()) {
                startProfile.enabled = true;
                startProfile.displayString = "Profile for " + seconds + " seconds";
            }else{
                startProfile.enabled = false;
                startProfile.displayString = "Profiling requires OP.";
            }
        }
        downloadButton.enabled = ServerDataPacketHandler.PERMISSION.ordinal() >= Perms.Permission.GET.ordinal();
    }

    private static int getSecondsLeftForProfiler(){
        if(LAST_STATUS != null) {
            return new Double(Math.ceil((PROFILE_END_TIME - System.currentTimeMillis()) / 1000)).intValue();
        }else{
            return -1;
        }
    }

    public static int getSecondsLeftForMessage(){
        return new Double(Math.ceil((MESSAGE_END_TIME - System.currentTimeMillis()) / 1000)).intValue();
    }

    @Override
    public void handleMouseInput() throws IOException{
        if(startProfile.isMouseOver() && startProfile.enabled){
            int wheel = Mouse.getDWheel();
            if(wheel != 0) {
                seconds = seconds + ((wheel / 120) * 5); /* 1 Click is 120, 1 click is 5 seconds */
                seconds = Math.max(seconds, 5);
                boolean triedMore = seconds > ServerDataPacketHandler.MAX_SECONDS;
                seconds = Math.min(seconds, ServerDataPacketHandler.MAX_SECONDS);
                if(triedMore){
                    startProfile.displayString = "Limited to " + seconds + " seconds.";
                }else {
                    startProfile.displayString = "Profile for " + seconds + " seconds";
                }
            }
        }
        super.handleMouseInput();
        Mouse.getDWheel();
    }

    public void startProfile(){
        CPacketRequestScan scan = new CPacketRequestScan();
        scan.length = seconds;
        startProfile.enabled = false;
        startProfile.displayString = "Sending command...";
        ClientProxy.NETWORK_WRAPPER.sendToServer(scan);
    }

    public void analyzeResults(){
        ArrayList<GuiScanResults.LagSource> entityList = new ArrayList<>();
        for(SPacketScanResult.EntityData entity : LAST_SCAN_RESULT.DATA){
                entityList.add(new GuiScanResults.LagSource(entity.<Long>getValue(SPacketScanResult.EntityData.Entry.NANOS), entity));
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
            case BUTTON_DONATE:
                DonateButton.donate();
                break;
            case BUTTON_OPTIONS:
                mc.displayGuiScreen(new GuiInGameConfig(this));
                break;
            case BUTTON_DOWNLOAD:
                ClientProxy.NETWORK_WRAPPER.sendToServer(new CPacketRequestResult());
                break;
        }
    }

    @Override
    public void onGuiClosed(){
        super.onGuiClosed();
        //TODO: Load the config, and install changes
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }

}
