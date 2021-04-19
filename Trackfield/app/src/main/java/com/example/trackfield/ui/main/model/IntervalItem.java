package com.example.trackfield.ui.main.model;

public class IntervalItem extends RecyclerItem {

    private String interval;
    private int count;

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

    @Override public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof IntervalItem)) return false;
        IntervalItem i = (IntervalItem) item;
        return interval.equals(i.getInterval());
    }
    @Override public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof IntervalItem)) return false;
        IntervalItem i = (IntervalItem) item;
        return interval.equals(i.getInterval()) && count == i.getCount();
    }

}
