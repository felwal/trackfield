package com.example.trackfield.ui.main.model;

import androidx.annotation.Nullable;

import com.example.trackfield.utils.Constants;
import com.example.trackfield.utils.MathUtils;

import java.util.Arrays;

public class Header extends RecyclerItem {

    private final String title;
    private final HeaderValue[] values;
    private final Type type;

    // as recycler item
    private boolean childrenExpanded = true;
    private boolean chartExpanded = false;
    private int firstIndex = 0;
    private int lastIndex = 0;

    public enum Type {
        YEAR,
        MONTH,
        WEEK,
        REC
    }

    //

    public Header(String title, Type type, int itemListSize, @Nullable HeaderValue... values) {
        this.title = title;
        this.type = type;
        this.firstIndex = itemListSize + 1;

        if (values == null) this.values = new HeaderValue[0];
        else this.values = values;
    }

    public Header(String title, Type type, @Nullable HeaderValue... values) {
        this.title = title;
        this.type = type;

        if (values == null) this.values = new HeaderValue[0];
        else this.values = values;
    }

    // set

    public void addValues(float... values) {
        for (int i = 0; i < this.values.length; i++) {
            if (i < values.length) {
                this.values[i].addValue(values[i]);
            }
        }
    }

    public void setLastIndex(int itemListSize) {
        if (lastIndex == 0) lastIndex = itemListSize - 1;
    }

    public void invertExpanded() {
        childrenExpanded = !childrenExpanded;
    }

    // get

    public String getTitle() {
        return title;
    }

    public boolean areChildrenExpanded() {
        return childrenExpanded;
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public int getLastIndex() {
        return lastIndex;
    }

    // get driven

    public boolean isType(Type... types) {
        for (Type type : types) {
            if (this.type == type) return true;
        }
        return false;
    }

    public String printValues() {
        String print = "";
        for (int i = 0; i < values.length; i++) {
            if (i != 0) print += Constants.TAB;
            HeaderValue value = values[i];
            print += MathUtils.roundToString(value.getValue(), value.getDecimals()) + " " + value.getUnit();
        }
        return print;
    }

    // extends RecyclerItem

    @Override
    public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof Header)) return false;
        Header h = (Header) item;
        return title.equals(h.getTitle());
    }

    @Override
    public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof Header)) return false;
        Header h = (Header) item;
        return type == h.type && title.equals(h.getTitle()) && Arrays.equals(values, h.values)
                && childrenExpanded == h.childrenExpanded;
    }

}

