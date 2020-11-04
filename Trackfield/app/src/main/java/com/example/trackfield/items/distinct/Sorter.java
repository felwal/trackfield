package com.example.trackfield.items.distinct;

import com.example.trackfield.toolbox.Toolbox.*;

public class Sorter extends RecyclerItem {

    private C.SortMode[] sortModes;
    private String[] sortModesTitle;
    private C.SortMode sortMode;
    private boolean smallestFirst;

    private static final char[] ARROWS = { '↓', '↑' };

    ////

    public Sorter(C.SortMode[] sortModes, String[] sortModesTitle, C.SortMode sortMode, boolean smallestFirst) {
        this.sortModes = sortModes;
        this.sortModesTitle = sortModesTitle;
        this.sortMode = sortMode;
        this.smallestFirst = smallestFirst;
    }

    // set
    public void setSortMode(C.SortMode sortMode) {
        this.sortMode = sortMode;
    }
    public void setSmallestFirst(boolean smallestFirst) {
        this.smallestFirst = smallestFirst;
    }

    // get
    public C.SortMode getSortMode() {
        return sortMode;
    }
    public boolean getSmallestFirst() {
        return smallestFirst;
    }
    public String getTitle() {
        return sortModesTitle[indexOfSortMode()] + " " + ARROWS[M.boolToInt(smallestFirst)];
    }
    public C.SortMode[] getSortModes() {
        return sortModes;
    }
    public String[] getSortModesTitle() {
        return sortModesTitle;
    }

    private int indexOfSortMode() {

        for (int i = 0; i < sortModes.length; i++) {
            if (sortMode == sortModes[i]) return i;
        }
        return 0;
    }

    @Override public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof Sorter)) return false;
        Sorter s = (Sorter) item;
        return sortModes == s.getSortModes() && sortModesTitle == s.getSortModesTitle();
    }
    @Override public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof Sorter)) return false;
        Sorter s = (Sorter) item;
        return sortMode == s.getSortMode() && smallestFirst == s.getSmallestFirst();
    }

}
