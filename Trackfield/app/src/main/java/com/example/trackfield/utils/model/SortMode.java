package com.example.trackfield.utils.model;

import androidx.annotation.Nullable;

public class SortMode {

    private final String title;
    private final Mode mode;
    private final boolean ascendingDefault;

    public enum Mode {
        DATE,
        DISTANCE,
        TIME,
        PACE,
        NAME,
        AMOUNT
    }

    //

    public SortMode(String title, Mode mode, boolean ascendingDefault) {
        this.title = title;
        this.mode = mode;
        this.ascendingDefault = ascendingDefault;
    }

    // get

    public String getTitle() {
        return title;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isAscendingDefault() {
        return ascendingDefault;
    }

    // extends Object

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof SortMode)) return false;
        SortMode other = (SortMode) obj;
        return title.equals(other.title) && mode == other.mode && ascendingDefault == other.ascendingDefault;
    }

}
