package cf.terminator.laggoggles.profiler;

import cf.terminator.laggoggles.client.gui.GuiScanResultsWorld;
import cf.terminator.laggoggles.packet.ObjectData;
import cf.terminator.laggoggles.packet.SPacketScanResult;
import cf.terminator.laggoggles.util.Side;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cf.terminator.laggoggles.profiler.ProfileManager.LAST_PROFILE_RESULT;

public class ProfileResult {

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

    public ProfileResult(ProfileResult original){
        this.startTime = original.startTime;
        this.endTime = original.endTime;
        this.totalTime = original.totalTime;
        this.tickCount = original.tickCount;
        this.totalFrames = original.totalFrames;
        this.type = original.type;
        OBJECT_DATA.addAll(original.OBJECT_DATA);
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
            for(ObjectData entity : LAST_PROFILE_RESULT.get().getData()){
                tmp.add(new GuiScanResultsWorld.LagSource(entity.<Long>getValue(ObjectData.Entry.NANOS), entity));
            }
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
        return new ProfileResult(this);
    }

    public long getTotalTime(){
        return totalTime;
    }

    public double getTPS(){
        double seconds = totalTime / 1000000000D;
        return seconds/tickCount;
    }

    public void lock(){
        isLocked = true;
    }

    public SPacketScanResult getPacket(){
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

}
