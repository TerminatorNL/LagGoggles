package cf.terminator.laggoggles.util;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public class ClickableLink {

    public static TextComponentString getLink(String link){
        TextComponentString text = new TextComponentString(TextFormatting.BLUE + link);
        Style style = text.getStyle();
        style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
        return text;
    }
}
