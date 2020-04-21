package com.github.terminatornl.laggoggles.client.gui;

import com.github.terminatornl.laggoggles.client.ClientProxy;
import com.github.terminatornl.laggoggles.client.ServerDataPacketHandler;
import com.github.terminatornl.laggoggles.client.gui.buttons.DonateButton;
import com.github.terminatornl.laggoggles.client.gui.buttons.DownloadButton;
import com.github.terminatornl.laggoggles.client.gui.buttons.OptionsButton;
import com.github.terminatornl.laggoggles.client.gui.buttons.ProfileButton;
import com.github.terminatornl.laggoggles.packet.CPacketRequestResult;
import com.github.terminatornl.laggoggles.packet.CPacketRequestScan;
import com.github.terminatornl.laggoggles.packet.SPacketMessage;
import com.github.terminatornl.laggoggles.profiler.ProfileResult;
import com.github.terminatornl.laggoggles.profiler.ScanType;
import com.github.terminatornl.laggoggles.util.Perms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;

import static com.github.terminatornl.laggoggles.profiler.ProfileManager.LAST_PROFILE_RESULT;

public class GuiProfile extends GuiScreen {

    private static final int BUTTON_START_PROFILE_ID = 0;
    private static final int BUTTON_SHOW_TOGGLE      = 1;
    private static final int BUTTON_ANALYZE_RESULTS  = 2;
    private static final int LABEL_ID                = 3;
    private static final int BUTTON_DONATE           = 4;
    private static final int BUTTON_OPTIONS          = 5;
    private static final int BUTTON_DOWNLOAD         = 6;

    public static String PROFILING_PLAYER = null;
    public static long PROFILE_END_TIME = 0L;

    public static SPacketMessage MESSAGE = null;
    public static long MESSAGE_END_TIME = 0L;


    private ProfileButton startProfile;
    private DownloadButton downloadButton;
    private GuiButton optionsButton;
    private boolean initialized = false;
    public int seconds = Math.min(30, ServerDataPacketHandler.MAX_SECONDS);

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

        boolean profileLoaded = LAST_PROFILE_RESULT.get() != null;

        startProfile = new ProfileButton(BUTTON_START_PROFILE_ID, centerX - 100, centerY - 25, "Profile for " + seconds + " seconds");
        downloadButton = new DownloadButton(this, BUTTON_DOWNLOAD, centerX + 80, centerY - 25);
        optionsButton = new OptionsButton(BUTTON_OPTIONS, centerX - 100, centerY + 75);
        GuiButton showToggle  = new GuiButton(BUTTON_SHOW_TOGGLE, centerX - 100, centerY +  5, LagOverlayGui.isShowing() ? "Hide latest scan results" : "Show latest scan results");
        GuiButton analyzeResults  = new GuiButton(BUTTON_ANALYZE_RESULTS, centerX - 100, centerY +  30, "Analyze results");


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
        initialized = true;
        updateButton();
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
        if(initialized == false){
            return;
        }
        if(getSecondsLeftForMessage() >= 0){
            startProfile.displayString = MESSAGE.message;
            startProfile.enabled = false;
            new Thread(buttonUpdateTask).start();
        }else if(getSecondsLeftForProfiler() >= 0){
            startProfile.displayString = PROFILING_PLAYER + " > " + getSecondsLeftForProfiler() + " seconds.";
            startProfile.enabled = false;
            new Thread(buttonUpdateTask).start();
        }else{
            startProfile.enabled = true;
            startProfile.displayString = "Profile for " + seconds + " seconds";
        }
        downloadButton.enabled = ServerDataPacketHandler.PERMISSION.ordinal() >= Perms.Permission.GET.ordinal();
    }

    private static int getSecondsLeftForProfiler(){
        if(PROFILING_PLAYER != null) {
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
        if(initialized == false){
            return;
        }
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

    private void analyzeResults(){
        ProfileResult result = LAST_PROFILE_RESULT.get();
        if(result != null) {
            if(result.getType() == ScanType.WORLD) {
                mc.displayGuiScreen(new GuiScanResultsWorld(result));
            }else if(result.getType() == ScanType.FPS){
                mc.displayGuiScreen(new GuiFPSResults(result));
            }
        }
    }

    @Override
    public void actionPerformed(GuiButton gui){
        switch (gui.id){
            case BUTTON_START_PROFILE_ID:
                startProfile.click(this,buttonList);
                break;
            case BUTTON_SHOW_TOGGLE:
                if(LagOverlayGui.isShowing()) {
                    LagOverlayGui.hide();
                    Minecraft.getMinecraft().displayGuiScreen(null);
                }else{
                    LagOverlayGui.show();
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
