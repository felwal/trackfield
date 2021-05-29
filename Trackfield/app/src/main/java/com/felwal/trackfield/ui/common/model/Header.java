package com.felwal.trackfield.ui.common.model;

import androidx.annotation.Nullable;

import com.felwal.trackfield.utils.AppConsts;
import com.felwal.trackfield.utils.MathUtils;
import com.felwal.trackfield.utils.annotations.Unfinished;

import java.util.Arrays;

public class Header extends RecyclerItem {

    public enum Type {
        YEAR,
        MONTH,
        WEEK,
        REC
    }

    private final String title;
    private final HeaderValue[] headerValues;
    private final Type type;

    private boolean childrenExpanded = true;
    @Unfinished private boolean chartExpanded = false;
    private int firstIndex = 0;
    private int lastIndex = 0;

    //

    public Header(String title, Type type, int firstIndex, @Nullable HeaderValue... headerValues) {
        this.title = title;
        this.type = type;
        this.firstIndex = firstIndex;

        if (headerValues == null) this.headerValues = new HeaderValue[0];
        else this.headerValues = headerValues;
    }

    public Header(String title, Type type, @Nullable HeaderValue... headerValues) {
        this.title = title;
        this.type = type;

        if (headerValues == null) this.headerValues = new HeaderValue[0];
        else this.headerValues = headerValues;
    }

    // set

    public void addValues(float... values) {
        for (int i = 0; i < this.headerValues.length; i++) {
            if (i < values.length) {
                this.headerValues[i].addValue(values[i]);
            }
        }
    }

    public void setLastIndex(int lastIndex) {
        // dont override if already set. this allows setting last index of month header when new year, making sure
        // it doesnt collapse the year header that comes before the next month header -- "January 2021" header should
        // not collapse "2021" header
        if (this.lastIndex == 0) this.lastIndex = lastIndex;
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
        StringBuilder print = new StringBuilder();
        for (int i = 0; i < headerValues.length; i++) {
            if (i != 0) print.append(AppConsts.TAB);

            HeaderValue value = headerValues[i];
            print.append(MathUtils.roundToString(value.getValue(), value.getDecimals()))
                .append(" ").append(value.getUnit());
        }
        return print.toString();
    }

    // extends RecyclerItem

    @Override
    public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof Header)) return false;
        Header other = (Header) item;
        return title.equals(other.getTitle());
    }

    @Override
    public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof Header)) return false;
        Header other = (Header) item;
        return type == other.type && title.equals(other.getTitle()) && Arrays.equals(headerValues, other.headerValues)
            && childrenExpanded == other.childrenExpanded;
    }

}

