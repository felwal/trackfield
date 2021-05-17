package com.example.trackfield.utils.model;

import androidx.annotation.Nullable;

public class SortMode {

    private final String title;
    private final Mode mode;
    private final boolean ascendingByDefault;

    public enum Mode {
        DATE,
        DISTANCE,
        TIME,
        PACE,
        NAME,
        AMOUNT
    }

    //

    public SortMode(String title, Mode mode, boolean ascendingByDefault) {
        this.title = title;
        this.mode = mode;
        this.ascendingByDefault = ascendingByDefault;
    }

    // get

    public String getTitle() {
        return title;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isAscendingByDefault() {
        return ascendingByDefault;
    }

    // extends Object

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof SortMode)) return false;
        SortMode other = (SortMode) obj;
        return title.equals(other.title) && mode == other.mode && ascendingByDefault == other.ascendingByDefault;
    }

}
