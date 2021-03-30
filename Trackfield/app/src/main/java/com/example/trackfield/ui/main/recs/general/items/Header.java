package com.example.trackfield.ui.main.recs.general.items;

import com.example.trackfield.service.toolbox.C;
import com.example.trackfield.service.toolbox.M;

public class Header extends RecyclerItem {

    private String title;
    private int value = 0;
    private int count = 0;
    private Type type;

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

    public Header(String title, Type type, int itemListSize) {
        this.title = title;
        this.type = type;
        this.firstIndex = itemListSize + 1;
    }

    public Header(String title, Type type) {
        this.title = title;
        this.type = type;
    }

    // set

    public void setTitle(String title) {
        this.title = title;
    }

    public void addValue(int value) {
        this.value += value;
        count++;
    }

    public void setFirstIndex(int index) {
        firstIndex = index;
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

    public int getValue() {
        return value;
    }

    public int getCount() {
        return count;
    }

    public Type getType() {
        return type;
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

    public int getChildItemCount() {
        return lastIndex - firstIndex - 1;
    }

    // get driven

    public boolean isType(Type type) {
        return this.type == type;
    }

    public boolean isType(Type type, Type orType) {
        return this.type == type || this.type == orType;
    }

    public String printValues() {
        switch (type) {
            case WEEK:
                return M.hours(value);
            case REC:
                return "";
            default:
                return M.kiloPrefix(value, 0, "m") + C.TAB + count + " activities";
        }
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
        return type == h.getType() && title.equals(h.getTitle()) && value == h.getValue() &&
            count == h.getCount() && childrenExpanded == h.childrenExpanded;
    }

}

