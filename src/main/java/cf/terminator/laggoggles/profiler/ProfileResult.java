package cf.terminator.laggoggles.profiler;

import cf.terminator.laggoggles.client.gui.GuiScanResultsWorld;
import cf.terminator.laggoggles.packet.ObjectData;
import cf.terminator.laggoggles.packet.SPacketScanResult;
import cf.terminator.laggoggles.util.Side;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ProfileResult {

    public static final ProfileResult EMPTY_RESULT = new ProfileResult(0,0,0,Side.UNKNOWN, ScanType.EMPTY);

    private final long startTime;
    private long endTime;
    private long totalTime;
    private long tickCount;
    private boolean isLocked = false;
    private Side side;
    private ScanType type;
    private long totalFrames = 0;
    private List<ObjectData> cachedList = null;

    private final ArrayList<ObjectData> OBJECT_DATA = new ArrayList<>();
    private List<GuiScanResultsWorld.LagSource> lagSources = null;

    public ProfileResult(long startTime, long endTime, long tickCount, Side side, ScanType type){
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalTime = endTime - startTime;
        this.tickCount = tickCount;
        this.side = side;
        this.type = type;
    }

    private ProfileResult(ProfileResult original, boolean copyData){
        this.startTime = original.startTime;
        this.endTime = original.endTime;
        this.totalTime = original.totalTime;
        this.tickCount = original.tickCount;
        this.totalFrames = original.totalFrames;
        this.type = original.type;
        this.side = original.side;
        if(copyData == true) {
            OBJECT_DATA.addAll(original.OBJECT_DATA);
        }
    }

    public void setFrames(long frames){
        if(isLocked) {
            throw new IllegalStateException("This data is for review only. You can't modify it. use copy() instead.");
        }
        totalFrames = frames;
    }

    public void addData(ObjectData data){
        if(isLocked) {
            throw new IllegalStateException("This data is for review only. You can't modify it. use copy() instead.");
        }
        OBJECT_DATA.add(data);
    }

    public boolean removeData(ObjectData data){
        if(isLocked) {
            throw new IllegalStateException("This data is for review only. You can't modify it. use copy() instead.");
        }
        return OBJECT_DATA.remove(data);
    }

    public boolean addAll(Collection<? extends ObjectData> data){
        if(isLocked) {
            throw new IllegalStateException("This data is for review only. You can't modify it. use copy() instead.");
        }
        return OBJECT_DATA.addAll(data);
    }

    /**
     * Clones the dataset. If you wish to modify it, use {@link #addData} and {@link #removeData}
     * @return object data
     */
    public List<ObjectData> getData() {
        if(cachedList == null){
            cachedList = Collections.unmodifiableList(OBJECT_DATA);
        }
        return cachedList;
    }

    public List<GuiScanResultsWorld.LagSource> getLagSources(){
        if(lagSources == null){
            ArrayList<GuiScanResultsWorld.LagSource> tmp = new ArrayList<>();
            for(ObjectData entity : getData()){
                tmp.add(new GuiScanResultsWorld.LagSource(entity.<Long>getValue(ObjectData.Entry.NANOS), entity));
            }
            Collections.sort(tmp);
            lagSources = Collections.unmodifiableList(tmp);
        }
        return lagSources;
    }

    public long getStartTime(){
        return startTime;
    }

    public long getEndTime(){
        return endTime;
    }

    public long getTickCount(){
        return tickCount;
    }

    public Side getSide(){
        return side;
    }

    public ScanType getType() {
        return type;
    }

    public long getTotalFrames(){
        return totalFrames;
    }

    public ProfileResult copy(){
        return new ProfileResult(this, true);
    }

    public ProfileResult copyStatsOnly(){
        return new ProfileResult(this, false);
    }

    public long getTotalTime(){
        return totalTime;
    }

    public double getTPS(){
        double seconds = totalTime / 1000000000D;
        return tickCount/seconds;
    }

    public double getFPS(){
        double seconds = totalTime / 1000000000D;
        return totalFrames/seconds;
    }

    public double getNanoPerFrame(){
        return (double)totalTime/(double)totalFrames;
    }

    public void lock(){
        isLocked = true;
    }

    private SPacketScanResult getPacket(){
        SPacketScanResult result = new SPacketScanResult();
        result.endTime = this.endTime;
        result.startTime = this.startTime;
        result.tickCount = this.tickCount;
        result.totalTime = this.totalTime;
        result.side = this.side;
        result.type = this.type;
        result.totalFrames = this.totalFrames;
        result.DATA.addAll(OBJECT_DATA);
        return result;
    }


    public List<SPacketScanResult> createPackets(EntityPlayerMP player){
        ArrayList<SPacketScanResult> list = new ArrayList<>();
        ArrayList<ObjectData> data = new ArrayList<>(OBJECT_DATA);
        player.sendMessage(new TextComponentString(TextFormatting.GRAY + "LagGoggles" + TextFormatting.WHITE + ": Generating the results for you..."));
        long time = System.currentTimeMillis();
        double dataSize = data.size();
        while(data.size() > 0) {
            SPacketScanResult packet = new SPacketScanResult();
            packet.endTime = this.endTime;
            packet.startTime = this.startTime;
            packet.tickCount = this.tickCount;
            packet.totalTime = this.totalTime;
            packet.side = this.side;
            packet.type = this.type;
            packet.totalFrames = this.totalFrames;
            packet.hasMore = true;

            ArrayList<ObjectData> sub = new ArrayList<>(data.subList(0,Math.min(50,data.size())));
            data.removeAll(sub);
            packet.DATA.addAll(sub);
            list.add(packet);
            if(time + 5000 < System.currentTimeMillis()){
                time = System.currentTimeMillis();
                player.sendMessage(new TextComponentString(TextFormatting.GRAY + "LagGoggles" + TextFormatting.WHITE + ": result is processing: " + Math.round(100 - (int) ((double) data.size()/dataSize * 100d)) + "%"));
            }
        }
        if(list.size() >= 1) {
            list.get(list.size() - 1).hasMore = false;
        }
        player.sendMessage(new TextComponentString(TextFormatting.GRAY + "LagGoggles" + TextFormatting.WHITE + ": Done!"));
        return list;
    }

}
