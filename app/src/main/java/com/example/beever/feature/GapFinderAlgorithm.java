package com.example.beever.feature;

import com.example.beever.database.EventEntry;
import com.example.beever.database.GroupEntry;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;

import static java.lang.Math.max;
import static java.lang.Math.min;

public abstract class GapFinderAlgorithm {

    private final String LOG_TAG = "GapFinderAlgorithm";
    private final int MIN_BLOCK_MINUTES = 15;
    private final int START_FORBIDDEN_TIMING_HOUR = 1;
    private final int END_FORBIDDEN_TIMING_HOUR = 8;
    private final int START_FORBIDDEN_TIMING_MIN = START_FORBIDDEN_TIMING_HOUR*60; // No meetings from 1 AM onwards
    private final int END_FORBIDDEN_TIMING_MIN = END_FORBIDDEN_TIMING_HOUR*60; // until 8 AM (rip my late-night MVMC meetings la T_T)
    private final int MIN_IN_DAY = 1440;

    private final String groupId;
    private final Integer timeout;
    private ArrayList<ArrayList<Timestamp> > result = null;

    private final int targetYear;
    private final int targetMonth;
    private final int targetDay;
    private final int targetHour;
    private final int targetMinute;
    private final int durationInMinutes;
    private Timestamp targetStartTimestamp = null;
    private Timestamp targetEndTimestamp = null;

    private boolean isAvailable = true;
    private boolean illegalTiming = false;
    private boolean errorExit = false;

    public GapFinderAlgorithm(String groupId, int timeout, int targetYear, int targetMonth, int targetDay,
                              int targetHour, int targetMinute, int durationInMinutes){
        this.groupId = groupId;
        this.timeout = timeout;
        this.targetYear = targetYear;
        this.targetMonth = targetMonth;
        this.targetDay = targetDay;
        this.targetHour = targetHour;
        this.targetMinute = targetMinute;
        this.durationInMinutes = durationInMinutes;
    }

    public void getGaps(){
        Calendar c = Calendar.getInstance();
        c.set(targetYear,targetMonth,targetDay,targetHour,targetMinute,0);
        targetStartTimestamp = new Timestamp(c.getTime());
        c.setTimeInMillis(c.getTimeInMillis()+(60*1000*durationInMinutes));
        targetEndTimestamp = new Timestamp(c.getTime());
        if (((c.get(Calendar.HOUR_OF_DAY)==START_FORBIDDEN_TIMING_HOUR)&&(c.get(Calendar.MINUTE)!=0))
            ||((c.get(Calendar.HOUR_OF_DAY)>START_FORBIDDEN_TIMING_HOUR)&&(c.get(Calendar.HOUR_OF_DAY)<END_FORBIDDEN_TIMING_HOUR))
            ||((c.get(Calendar.HOUR_OF_DAY)==END_FORBIDDEN_TIMING_HOUR)&&(c.get(Calendar.MINUTE)==0))){
            illegalTiming = true;
            isAvailable = false;
        }
        long firstTime = System.currentTimeMillis();
        GroupEntry.GetGroupEntry firstGetter = new GroupEntry.GetGroupEntry(groupId, timeout) {
            @Override
            public void onPostExecute() {
                if (!isSuccessful()){
                    //Log.d(LOG_TAG,"GroupEntry retrieval failed");
                    errorExit = true;
                    GapFinderAlgorithm.this.onPostExecute();
                    return;
                }
                GroupEntry.GetGroupRelevantEvents secondGetter = new GroupEntry.GetGroupRelevantEvents(getResult(),
                        timeout - (int)(System.currentTimeMillis() - firstTime)){
                    public void onPostExecute(){
                        if (!isSuccessful()){
                            //Log.d(LOG_TAG,"Events retrieval failed");
                            errorExit = true;
                            GapFinderAlgorithm.this.onPostExecute();
                            return;
                        }
                        runMainGapFinder(getResult());
                    }
                };
                secondGetter.start();
            }
        };
        firstGetter.start();
    }

    public void runMainGapFinder(ArrayList<EventEntry> events){

        Calendar calendar = Calendar.getInstance();
        calendar.set(targetYear,targetMonth,targetDay,0,0,0);
        if (isAvailable){
            for (EventEntry e:events){
                if ((((int)targetStartTimestamp.getSeconds()>=(int)e.getStart_time().getSeconds())
                        &&((int)targetStartTimestamp.getSeconds()<(int)e.getEnd_time().getSeconds()))
                    ||(((int)targetEndTimestamp.getSeconds()>(int)e.getStart_time().getSeconds())
                        &&((int)targetEndTimestamp.getSeconds()<=(int)e.getEnd_time().getSeconds()))
                    ||(((int)targetStartTimestamp.getSeconds()<=(int)e.getStart_time().getSeconds())
                        &&((int)targetEndTimestamp.getSeconds()>=(int)e.getEnd_time().getSeconds()))){
                    isAvailable = false;
                    break;
                }
            }
            if (isAvailable){
                onPostExecute();
                return;
            }
        }
        Timestamp targetDateBase = new Timestamp(calendar.getTime());
        int availableBlockCount = (MIN_IN_DAY + START_FORBIDDEN_TIMING_MIN) / MIN_BLOCK_MINUTES;
        int requestedBlockCount = (durationInMinutes%MIN_BLOCK_MINUTES==0?
                durationInMinutes/MIN_BLOCK_MINUTES:(durationInMinutes/MIN_BLOCK_MINUTES)+1);
        if (requestedBlockCount > availableBlockCount) {
            result = new ArrayList<>();
            onPostExecute();
            return;
        }
        ArrayList<Boolean> occupancy = new ArrayList<>();
        for (int i = 0;i<availableBlockCount;i++){
            occupancy.add(((i+1)*MIN_BLOCK_MINUTES<=START_FORBIDDEN_TIMING_MIN)
            | (i*MIN_BLOCK_MINUTES>=END_FORBIDDEN_TIMING_MIN && (i+1)*MIN_BLOCK_MINUTES<=MIN_IN_DAY+START_FORBIDDEN_TIMING_MIN));
        }
        for (EventEntry e:events){
            int startBlock = getOffsetInBlocks(targetDateBase,e.getStart_time());
            int endBlock = getOffsetInBlocks(targetDateBase,e.getEnd_time());
            //Log.d("chk",Integer.toString(startBlock));
            //Log.d("chk",Integer.toString(endBlock));
            for (int i=max(startBlock,0);i<min(availableBlockCount,endBlock);i++){
                occupancy.set(i,false);
            }
        }
        ArrayList<Integer> occupancyCumulative = new ArrayList<>(availableBlockCount);
        for (int i=0;i<availableBlockCount;i++) occupancyCumulative.add(0);
        occupancyCumulative.set(availableBlockCount-1,occupancy.get(availableBlockCount-1)? 1 : 0);
        result = new ArrayList<>();
        if (requestedBlockCount==1 && occupancy.get(availableBlockCount-1)) {
            ArrayList<Timestamp> resultAppend = new ArrayList<>();
            resultAppend.add(getOffsetTimestamp(targetDateBase, availableBlockCount-1));
            resultAppend.add(getOffsetTimestamp(targetDateBase, availableBlockCount-1 + requestedBlockCount));
            result.add(resultAppend);
        }

        for (int i = availableBlockCount-2;i>-1;i--) {
            occupancyCumulative.set(i, occupancy.get(i) ? occupancyCumulative.get(i + 1) + 1 : 0);
            if (occupancyCumulative.get(i) >= requestedBlockCount) {
                ArrayList<Timestamp> resultAppend = new ArrayList<>();
                resultAppend.add(getOffsetTimestamp(targetDateBase, i));
                resultAppend.add(getOffsetTimestamp(targetDateBase, i + requestedBlockCount));
                //Log.d("cat",resultAppend.toString());
                result.add(resultAppend);
            }
        }

        onPostExecute();
    }

    public abstract void onPostExecute();

    public int getOffsetInBlocks(Timestamp base, Timestamp target){
        //Log.d("chk2",Long.toString(target.getSeconds()-base.getSeconds()));
        return Math.round((target.getSeconds()-base.getSeconds())/(float)60/MIN_BLOCK_MINUTES);
    }

    public Timestamp getOffsetTimestamp(Timestamp base,int block){
        int targetSecond = (int)base.getSeconds();
        targetSecond += block * MIN_BLOCK_MINUTES * 60;
        return new Timestamp(targetSecond,0);
    }

    public boolean isSuccessful(){
        return !errorExit;
    }

    public boolean isIllegalTiming(){
        return illegalTiming;
    }

    public boolean isAvailable(){
        return isAvailable;
    }

    public Timestamp getTargetStartTimestamp() {
        return targetStartTimestamp;
    }

    public Timestamp getTargetEndTimestamp() {
        return targetEndTimestamp;
    }

    public ArrayList<ArrayList<Timestamp> > getResult(){
        return result;
    }
}
