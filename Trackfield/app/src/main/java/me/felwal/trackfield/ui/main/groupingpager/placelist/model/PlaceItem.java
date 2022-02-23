package me.felwal.trackfield.ui.main.groupingpager.placelist.model;

import me.felwal.trackfield.ui.common.model.RecyclerItem;

public class PlaceItem extends RecyclerItem {

    private final int id;
    private final String name;

    //

    public PlaceItem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // get

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // print

    public String printValues() {
        return "";
    }

    // extends RecyclerItem

    @Override
    public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof PlaceItem)) return false;
        PlaceItem other = (PlaceItem) item;
        return id == other.id;
    }

    @Override
    public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof PlaceItem)) return false;
        PlaceItem other = (PlaceItem) item;
        return name.equals(other.getName());
    }

}
