package com.example.trackfield.items.headers;

public abstract class RecyclerItem {

    private int hiddenLevel = 0;

    ////

    // set
    public void changeVisibility(boolean expand) {
        if (expand && hiddenLevel > 0) hiddenLevel--;
        else hiddenLevel++;
    }

    // get
    public boolean isVisible() {
        return hiddenLevel == 0;
    }

    // compare
    public abstract boolean sameItemAs(RecyclerItem item);
    public abstract boolean sameContentAs(RecyclerItem item);

}
