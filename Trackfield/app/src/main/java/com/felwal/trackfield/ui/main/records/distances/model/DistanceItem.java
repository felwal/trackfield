package com.felwal.trackfield.ui.main.records.distances.model;

import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.utils.AppConsts;
import com.felwal.trackfield.utils.MathUtils;

public class DistanceItem extends RecyclerItem {

    private final int distance;
    private final float bestTime;
    private final float bestPace;

    //

    public DistanceItem(int distance, float bestPace) {
        this.distance = distance;
        this.bestPace = bestPace;
        bestTime = bestPace * (float) distance / 1000;
    }

    // get

    public int getDistance() {
        return distance;
    }

    public float getBestTime() {
        return bestTime;
    }

    public float getBestPace() {
        return bestPace;
    }

    // print

    private String printBestTime() {
        return bestTime <= 0 ? AppConsts.NO_VALUE_TIME : MathUtils.stringTime(bestTime, true);
    }

    private String printBestPace() {
        return bestPace <= 0 ? AppConsts.NO_VALUE_TIME : MathUtils.stringTime(bestPace, true);
    }

    public String printValues() {
        return printBestTime() + AppConsts.TAB + printBestPace();
    }

    // extends RecyclerItem

    @Override
    public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof DistanceItem)) return false;
        DistanceItem other = (DistanceItem) item;
        return distance == other.getDistance();
    }

    @Override
    public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof DistanceItem)) return false;
        DistanceItem other = (DistanceItem) item;
        return distance == other.getDistance() && bestTime == other.getBestTime() && bestPace == other.getBestPace();
    }

}
