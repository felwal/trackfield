package com.example.trackfield.ui.common.model;

import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.MathUtils;

public class Goal extends RecyclerItem {

    private final String values;

    //

    public Goal(float goalPace) {
        this.values = MathUtils.stringTime(goalPace, true);
    }

    public Goal(float goalPace, int distance) {
        this.values = MathUtils.stringTime(getTime(distance, goalPace), true)
            + AppConsts.TAB + MathUtils.stringTime(goalPace, true);
    }

    // get

    private float getTime(int distance, float pace) {
        return pace * distance / 1000f;
    }

    // print

    public String printValues() {
        return "Goal:" + AppConsts.TAB + values;
    }

    // extends RecyclerItem

    @Override
    public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof Goal)) return false;
        Goal other = (Goal) item;
        return values.equals(other.values);
    }

    @Override
    public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof Goal)) return false;
        Goal other = (Goal) item;
        return values.equals(other.values);
    }

}
