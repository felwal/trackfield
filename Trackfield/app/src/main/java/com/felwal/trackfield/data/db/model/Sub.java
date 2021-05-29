package com.felwal.trackfield.data.db.model;

import com.felwal.trackfield.utils.AppConsts;
import com.felwal.trackfield.utils.MathUtils;
import com.felwal.trackfield.utils.annotations.Unimplemented;

@Unimplemented
public class Sub {

    private final int id;
    private int superId;
    private int distance;
    private float time;

    //

    public Sub(int id, int superId, int distance, float time) {
        this.id = id;
        this.superId = superId;
        this.distance = distance;
        this.time = time;
    }

    // set

    public void setSuperId(int superId) {
        this.superId = superId;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setTime(float time) {
        this.time = time;
    }

    // get

    public int getId() {
        return id;
    }

    public int getSuperId() {
        return superId;
    }

    public int getDistance() {
        return distance;
    }

    public float getTime() {
        return time;
    }

    // get driven

    public float getPace() {
        if (distance == 0) return 0;
        return time / ((float) distance / 1000f);
    }

    // print

    public String printDistance() {
        if (distance == 0) return AppConsts.NO_VALUE;
        return MathUtils.prefix(distance, 2, "m");
    }

    public String printTime(boolean showUnit) {
        String timePrint = MathUtils.stringTime(time, false);

        if (!showUnit || timePrint.equals(AppConsts.NO_VALUE_TIME)) return timePrint;
        return timePrint + " s";
    }

    public String printPace(boolean showUnit) {
        String pacePrint = MathUtils.stringTime(getPace(), true);

        if (!showUnit || pacePrint.equals(AppConsts.NO_VALUE_TIME)) { return pacePrint; }
        return pacePrint + " s/km";
    }

}
