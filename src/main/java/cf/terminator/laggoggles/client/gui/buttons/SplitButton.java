package cf.terminator.laggoggles.client.gui.buttons;

import cf.terminator.laggoggles.client.gui.GuiProfile;
import cf.terminator.laggoggles.client.gui.LagOverlayGui;
import net.minecraft.client.gui.GuiButton;

import java.util.List;

import static cf.terminator.laggoggles.client.gui.buttons.SplitButton.State.NORMAL;
import static cf.terminator.laggoggles.client.gui.buttons.SplitButton.State.SPLIT;

public abstract class SplitButton extends GuiButton{

    State state = NORMAL;
    long lastClick = 0;
    enum State{
        NORMAL,
        SPLIT,
    }

    protected final GuiButton clientButton;
    protected final GuiButton serverButton;

    public SplitButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        clientButton = new GuiButton(id,x + width/2 + 5,y,width/2 - 5, height, "FPS");
        serverButton = new GuiButton(id,x,y,width/2 - 5, height, "World");
    }

    public void click(GuiProfile parent, List<GuiButton> buttonList){
        if(lastClick + 50 > System.currentTimeMillis()){
            return;
        }
        lastClick = System.currentTimeMillis();
        updateButtons();
        if(state == NORMAL) {
            state = SPLIT;
            buttonList.remove(this);
            buttonList.add(clientButton);
            buttonList.add(serverButton);
        }else if(state == SPLIT){
            LagOverlayGui.hide();
            buttonList.add(this);
            buttonList.remove(clientButton);
            buttonList.remove(serverButton);
            if(serverButton.isMouseOver()){
                onWorldClick(parent);
            }else if(clientButton.isMouseOver()){
                onFPSClick(parent);
            }
            state = NORMAL;
        }
    }

    public void updateButtons(){};

    public abstract void onWorldClick(GuiProfile parent);
    public abstract void onFPSClick(GuiProfile parent);
}
