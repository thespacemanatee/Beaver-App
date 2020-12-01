package com.example.beever.feature;

import android.util.Log;

import com.example.beever.database.EventEntry;
import com.example.beever.database.GroupEntry;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Math.max;
import static java.lang.Math.min;

public abstract class GapFinderAlgorithm {

    private final String LOG_TAG = "GapFinderAlgorithm";
    private final int HOUR_SEGMENTS = 4;
    private final int MIN_BLOCK_MINUTES = 60 / HOUR_SEGMENTS;
    private final int START_FORBIDDEN_TIMING_MIN = 60; // No meetings from 1 AM onwards
    private final int END_FORBIDDEN_TIMING_MIN = 480; // until 8 AM (rip my late-night MVMC meetings la T_T)
    private final int MIN_IN_DAY = 1440;

    private String groupId = null;
    private Integer timeout = null;
    private ArrayList<ArrayList<Timestamp> > result = null;
    private Calendar calendar = Calendar.getInstance();

    private int targetYear = 0;
    private int targetMonth = 0;
    private int targetDay = 0;
    private int durationInMinutes = 0;

    public GapFinderAlgorithm(String groupId, int timeout, int targetYear, int targetMonth, int targetDay, int durationInMinutes){
        this.groupId = groupId;
        this.timeout = timeout;
        this.targetYear = targetYear;
        this.targetMonth = targetMonth;
        this.targetDay = targetDay;
        this.durationInMinutes = durationInMinutes;
    }

    public void getGaps(){
        long firstTime = System.currentTimeMillis();
        GroupEntry.GetGroupEntry firstGetter = new GroupEntry.GetGroupEntry(groupId, timeout) {
            @Override
            public void onPostExecute() {
                if (!isSuccessful()){
                    Log.d(LOG_TAG,"GroupEntry retrieval failed");
                    onPostExecute();
                    return;
                }
                GroupEntry.GetGroupRelevantEvents secondGetter = new GroupEntry.GetGroupRelevantEvents(getResult(),
                        timeout - (int)(System.currentTimeMillis() - firstTime)){
                    public void onPostExecute(){
                        if (!isSuccessful()){
                            Log.d(LOG_TAG,"Events retrieval failed");
                            onPostExecute();
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
        calendar.set(targetYear,targetMonth,targetDay);
        Timestamp targetDateBase = new Timestamp(calendar.getTime());
        Log.d("ALGORITHM", "runMainGapFinder: " + targetDateBase);
        int availableBlockCount = (MIN_IN_DAY + START_FORBIDDEN_TIMING_MIN) / MIN_BLOCK_MINUTES;
        int requestedBlockCount = (durationInMinutes%MIN_BLOCK_MINUTES==0?
                durationInMinutes/MIN_BLOCK_MINUTES:(durationInMinutes/MIN_BLOCK_MINUTES)+1);
        if (requestedBlockCount > availableBlockCount) {
            result = new ArrayList<ArrayList<Timestamp> >();
            onPostExecute();
            return;
        }
        ArrayList<Boolean> occupancy = new ArrayList<Boolean>();
        for (int i = 0;i<availableBlockCount;i++){
            occupancy.add(((i+1)*MIN_BLOCK_MINUTES<=START_FORBIDDEN_TIMING_MIN)
            | (i*MIN_BLOCK_MINUTES>=END_FORBIDDEN_TIMING_MIN && (i+1)*MIN_BLOCK_MINUTES<=MIN_IN_DAY+START_FORBIDDEN_TIMING_MIN));
        }
        for (EventEntry e:events){
            int startBlock = getOffsetInMinutes(targetDateBase,e.getStart_time())/MIN_BLOCK_MINUTES;
            int endBlock = getOffsetInMinutes(targetDateBase,e.getEnd_time())/MIN_BLOCK_MINUTES;
            for (int i=max(startBlock,0);i<min(availableBlockCount,endBlock);i++){
                occupancy.set(i,false);
            }
        }
        ArrayList<Integer> occupancyCumulative = new ArrayList<Integer>(availableBlockCount);
        for (int i=0;i<availableBlockCount;i++) occupancyCumulative.add(0);
        occupancyCumulative.set(availableBlockCount-1,occupancy.get(availableBlockCount-1)? 1 : 0);
        result = new ArrayList<ArrayList<Timestamp> >();
        for (int i = availableBlockCount-2;i>-1;i--) {
            occupancyCumulative.set(i, occupancy.get(i) ? occupancyCumulative.get(i + 1) + 1 : 0);
            if (occupancyCumulative.get(i) >= requestedBlockCount) {
                ArrayList<Timestamp> resultAppend = new ArrayList<Timestamp>();
                resultAppend.add(getOffsetTimestamp(targetDateBase, i));
                resultAppend.add(getOffsetTimestamp(targetDateBase, i + requestedBlockCount));
                result.add(resultAppend);
            }
        }
        onPostExecute();
        return;
    }

    public abstract void onPostExecute();

    public int getOffsetInMinutes(Timestamp base, Timestamp target){
        return (int)(target.getSeconds()-base.getSeconds())/60;
    }

    public Timestamp getOffsetTimestamp(Timestamp base,int block){
        int targetSecond = (int)base.getSeconds();
        targetSecond += block * MIN_BLOCK_MINUTES * 60;
        return new Timestamp(targetSecond,0);
    }

    public boolean isSuccessful(){
        return result!=null;
    }

    public ArrayList<ArrayList<Timestamp> > getResult(){
        return result;
    }
}
