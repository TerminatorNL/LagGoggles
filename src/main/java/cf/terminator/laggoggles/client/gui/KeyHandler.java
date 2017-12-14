package cf.terminator.laggoggles.client.gui;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyHandler extends KeyBinding {

    private final CallBack callBack;

    public KeyHandler(String description, int keyCode, String category, CallBack callback) {
        super(description, keyCode, category);
        this.callBack = callback;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(this.isPressed()){
            callBack.onPress();
        }
    }

    public interface CallBack{
        void onPress();
    }
}
