package com.felwal.trackfield.ui.main.records.routes.model;

import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.utils.AppConsts;
import com.felwal.trackfield.utils.MathUtils;

public class RouteItem extends RecyclerItem {

    private final int id;
    private final String name;
    private final int count;
    private final int avgDistance;
    private final float bestPace;

    //

    public RouteItem(int id, String name, int count, int avgDistance, float bestPace) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.avgDistance = avgDistance;
        this.bestPace = bestPace;
    }

    // get

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public int getAvgDistance() {
        return avgDistance;
    }

    public float getBestPace() {
        return bestPace;
    }

    // print

    private String printAvgDistance() {
        return avgDistance != 0 ? MathUtils.prefix(avgDistance, 1, "m") : AppConsts.NO_VALUE;
    }

    private String printBestPace() {
        return bestPace != -1 ? MathUtils.stringTime(bestPace, true) : AppConsts.NO_VALUE_TIME;
    }

    public String printValues() {
        return count + AppConsts.TAB + printAvgDistance() + AppConsts.TAB + printBestPace();
    }

    // extends RecyclerItem

    @Override
    public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof RouteItem)) return false;
        RouteItem other = (RouteItem) item;
        return id == other.id;
    }

    @Override
    public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof RouteItem)) return false;
        RouteItem r = (RouteItem) item;
        return name.equals(r.getName()) && count == r.getCount() && avgDistance == r.getAvgDistance() && bestPace == r
            .getBestPace();
    }

}
