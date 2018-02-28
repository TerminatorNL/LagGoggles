package cf.terminator.laggoggles.profiler;

import cf.terminator.laggoggles.Main;

public enum ScanType {
    WORLD(Main.MODID + ": World scan results"),
    FPS(Main.MODID + ": FPS scan results"),
    EMPTY("Empty profile results.");

    private final String text;

    ScanType(String text){
        this.text = text;
    }

    public String getText(ProfileResult result){
        return text;
    }
}
