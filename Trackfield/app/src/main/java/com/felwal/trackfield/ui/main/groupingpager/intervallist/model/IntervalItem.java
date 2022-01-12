package com.felwal.trackfield.ui.main.groupingpager.intervallist.model;

import com.felwal.trackfield.ui.common.model.RecyclerItem;

public class IntervalItem extends RecyclerItem {

    private final String interval;
    private final int count;

    //

    public IntervalItem(String interval, int count) {
        this.interval = interval;
        this.count = count;
    }

    // get

    public String getInterval() {
        return interval;
    }

    public int getCount() {
        return count;
    }

    // print

    public String printValues() {
        return count + "";
    }

    // extends RecyclerItem

    @Override
    public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof IntervalItem)) return false;
        IntervalItem other = (IntervalItem) item;
        return interval.equals(other.getInterval());
    }

    @Override
    public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof IntervalItem)) return false;
        IntervalItem i = (IntervalItem) item;
        return interval.equals(i.getInterval()) && count == i.getCount();
    }

}
