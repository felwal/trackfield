package com.example.trackfield.ui.main.recs.routes.model;

import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.MathUtils;

public class RouteItem extends RecyclerItem {

    private final int _id;
    private String name;
    private int count;
    private int avgDistance;
    private float bestPace;

    public enum SortMode {
        RECENT,
        NAME,
        AMOUNT,
        AVG_DISTANCE,
        BEST_PACE
    }

    //

    public RouteItem(int _id, String name, int count, int avgDistance, float bestPace) {
        this._id = _id;
        this.name = name;
        this.count = count;
        this.avgDistance = avgDistance;
        this.bestPace = bestPace;
    }

    // get

    public int get_id() {
        return _id;
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

    @Override public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof RouteItem)) return false;
        RouteItem r = (RouteItem) item;
        return _id == r._id;
    }

    @Override public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof RouteItem)) return false;
        RouteItem r = (RouteItem) item;
        return name.equals(r.getName()) && count == r.getCount() && avgDistance == r.getAvgDistance() && bestPace == r.getBestPace();
    }

}
