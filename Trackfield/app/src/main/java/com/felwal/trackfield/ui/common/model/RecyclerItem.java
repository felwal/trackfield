package com.felwal.trackfield.ui.common.model;

public abstract class RecyclerItem {

    public final static String TAG_GRAPH_REC = "recGraph";
    public final static String TAG_GRAPH_WEEK = "weekGraph";
    public final static String TAG_GRAPH_BASE = "baseGraph";

    protected String tag;
    private int collapsedLevel = 0;

    // set

    public void changeVisibility(boolean expand) {
        if (expand && collapsedLevel > 0) collapsedLevel--;
        else collapsedLevel++;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    // get

    public boolean isVisible() {
        return collapsedLevel == 0;
    }

    public boolean hasTag(String tag) {
        return this.tag != null && this.tag.equals(tag);
    }

    // compare - used in DiffUtil in RecyclerFragment

    public abstract boolean sameItemAs(RecyclerItem item);

    public abstract boolean sameContentAs(RecyclerItem item);

}
