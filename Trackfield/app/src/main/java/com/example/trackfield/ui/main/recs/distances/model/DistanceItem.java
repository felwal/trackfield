package com.example.trackfield.ui.main.recs.distances.model;

import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.utils.AppConsts;
import com.example.trackfield.utils.MathUtils;

public class DistanceItem extends RecyclerItem {

    private int distance;
    private float bestTime;
    private float bestPace;

    //

    public DistanceItem(int distance, float bestTime, float bestPace) {
        this.distance = distance;
        this.bestTime = bestTime;
        this.bestPace = bestPace;
    }

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
        DistanceItem d = (DistanceItem) item;
        return distance == d.getDistance();
    }

    @Override
    public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof DistanceItem)) return false;
        DistanceItem d = (DistanceItem) item;
        return distance == d.getDistance() && bestTime == d.getBestTime() && bestPace == d.getBestPace();
    }

}