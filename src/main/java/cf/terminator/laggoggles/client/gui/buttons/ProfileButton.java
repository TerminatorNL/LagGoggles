package cf.terminator.laggoggles.client.gui.buttons;

import cf.terminator.laggoggles.Main;
import cf.terminator.laggoggles.api.Profiler;
import cf.terminator.laggoggles.client.ServerDataPacketHandler;
import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.client.gui.LagOverlayGui;
import cf.terminator.laggoggles.client.gui.QuickText;
import cf.terminator.laggoggles.profiler.ProfileResult;
import cf.terminator.laggoggles.profiler.ScanType;
import cf.terminator.laggoggles.util.Perms;
import net.minecraft.client.Minecraft;

public class ProfileButton extends SplitButton {

    public static Thread PROFILING_THREAD;
    private long frames = 0;
    public ProfileButton(int buttonId, int x, int y, String text) {
        super(buttonId, x, y, 170, 20, text);
    }

    @Override
    public void onWorldClick(GuiProfile parent) {
        parent.startProfile();
    }

    @Override
    public void updateButtons(){
        if(ServerDataPacketHandler.PERMISSION.ordinal() < Perms.Permission.START.ordinal()) {
            serverButton.enabled = false;
            serverButton.displayString = "No perms";
        }
    }

    @Override
    public void onFPSClick(GuiProfile parent) {
        final int seconds = parent.seconds;
        if(PROFILING_THREAD == null || PROFILING_THREAD.isAlive() == false){
            PROFILING_THREAD = new Thread(new Runnable() {
                @Override
                public void run() {
                    Main.LOGGER.info("Clientside profiling started. (" + seconds + " seconds)");
                    QuickText text = new QuickText("Profiling FPS... For the best results, do not look around.");
                    GuiProfile.PROFILING_PLAYER = Minecraft.getMinecraft().player.getName();
                    GuiProfile.PROFILE_END_TIME = System.currentTimeMillis() + (seconds * 1000);
                    GuiProfile.update();
                    text.show();
                    ProfileResult result = Profiler.runProfiler(seconds, ScanType.FPS, Minecraft.getMinecraft().player);
                    text.hide();
                    Main.LOGGER.info("Clientside profiling done.");
                    LagOverlayGui.create(result);
                    LagOverlayGui.show();
                    GuiProfile.update();
                }
            });
            PROFILING_THREAD.start();
        }
    }

}
