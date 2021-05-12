package com.example.trackfield.ui.common.model;

public abstract class RecyclerItem {

    private int hiddenLevel = 0;
    protected String tag;

    // tags
    public final static String TAG_HEADER_YEAR = "yearHeader";
    public final static String TAG_HEADER_MONTH = "monthHeader";
    public final static String TAG_HEADER_WEEK = "weekHeader";
    public final static String TAG_HEADER_REC = "recHeader";
    public final static String TAG_GRAPH_REC = "recGraph";
    public final static String TAG_GRAPH_WEEK = "weekGraph";
    public final static String TAG_GRAPH_BASE = "baseGraph";

    // set

    public void changeVisibility(boolean expand) {
        if (expand && hiddenLevel > 0) hiddenLevel--;
        else hiddenLevel++;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    // get

    public boolean isVisible() {
        return hiddenLevel == 0;
    }

    public boolean hasTag(String tag) {
        return this.tag != null && this.tag.equals(tag);
    }

    public boolean hasTag(String tag, String orTag) {
        return hasTag(tag) || hasTag(orTag);
    }

    // compare

    public abstract boolean sameItemAs(RecyclerItem item);

    public abstract boolean sameContentAs(RecyclerItem item);

}
