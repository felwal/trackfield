package com.example.trackfield.items.distinct;

import com.example.trackfield.toolbox.Toolbox.*;

public class Goal extends RecyclerItem {

    private String values;

    //

    public Goal(float goalPace) {
        this.values = M.stringTime(goalPace, true);
    }
    public Goal(float goalPace, int distance) {
        this.values = M.stringTime(getTime(distance, goalPace), true) + C.TAB + M.stringTime(goalPace, true);
    }
    public Goal(int distance, float goalTime) {
        this.values = M.stringTime(goalTime, true) + C.TAB + M.stringTime(getPace(distance, goalTime), true);
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
        return "Goal:" + C.TAB + values;
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
