package com.example.trackfield.ui.common.model;

import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.MathUtils;

public class Goal extends RecyclerItem {

    private String values;

    //

    public Goal(float goalPace) {
        this.values = MathUtils.stringTime(goalPace, true);
    }
    public Goal(float goalPace, int distance) {
        this.values = MathUtils.stringTime(getTime(distance, goalPace), true) + AppConsts.TAB + MathUtils.stringTime(goalPace, true);
    }
    public Goal(int distance, float goalTime) {
        this.values = MathUtils.stringTime(goalTime, true) + AppConsts.TAB + MathUtils.stringTime(getPace(distance, goalTime), true);
    }

    // get
    private float getTime(int distance, float pace) {
        return pace * distance / 1000f;
    }
    private float getPace(int distance, float time) {
        if (distance == 0) { return 0; }
        return time / ((float) distance / 1000f);
    }

    // print
    public String printValues() {
        return "Goal:" + AppConsts.TAB + values;
    }

    @Override public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof Goal)) return false;
        Goal g = (Goal) item;
        return values.equals(g.values);
    }
    @Override public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof Goal)) return false;
        Goal g = (Goal) item;
        return values.equals(g.values);
    }
}
