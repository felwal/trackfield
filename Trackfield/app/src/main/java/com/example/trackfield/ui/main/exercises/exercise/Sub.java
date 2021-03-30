package com.example.trackfield.ui.main.exercises.exercise;

import com.example.trackfield.service.toolbox.C;
import com.example.trackfield.service.toolbox.M;

public class Sub {

    private final int _id;
    private int _superId;
    private int distance;
    private float time;

    ////

    public Sub(int _id, int _superId, int distance, float time) {
        this._id = _id;
        this._superId = _superId;
        this.distance = distance;
        this.time = time;
    }

    // set

    public void set_superId(int _superId) {
        this._superId = _superId;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setTime(float time) {
        this.time = time;
    }

    // get

    public int get_id() {
        return _id;
    }

    public int get_superId() {
        return _superId;
    }

    public int getDistance() {
        return distance;
    }

    public float getTime() {
        return time;
    }

    // get driven

    public float getPace() {
        if (distance == 0) { return 0; }
        return time / ((float) distance / 1000f);
    }

    // print

    public String printDistance() {
        if (distance == 0) { return C.NO_VALUE; }
        return M.prefix(distance, 2, "m");
    }

    public String printTime(boolean unit) {
        String timePrint = M.stringTime(time, false);
        if (!unit || timePrint.equals(C.NO_VALUE_TIME)) { return timePrint; }
        return timePrint + " s";
    }

    public String printPace(boolean unit) {
        String pacePrint = M.stringTime(getPace(), true);
        if (!unit || pacePrint.equals(C.NO_VALUE_TIME)) { return pacePrint; }
        return pacePrint + " s/km";
    }

    public String extractToFile(char div, int superId, int index) {
        return superId + "" + div + "" + index + "" + div + "" + distance + "" + div + "" + time + "";
    }

}
