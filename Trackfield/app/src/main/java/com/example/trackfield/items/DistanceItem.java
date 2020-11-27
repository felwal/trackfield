package com.example.trackfield.items;

import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.toolbox.Toolbox.*;

public class DistanceItem extends RecyclerItem {

    private int distance;
    private float bestTime;
    private float bestPace;

    ////

    public DistanceItem(int distance, float bestTime, float bestPace) {
        this.distance = distance;
        this.bestTime = bestTime;
        this.bestPace = bestPace;
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
        return bestTime <= 0 ? C.NO_VALUE_TIME : M.stringTime(bestTime, true);
    }
    private String printBestPace() {
        return bestPace <= 0 ? C.NO_VALUE_TIME : M.stringTime(bestPace, true);
    }
    public String printValues() {
        return printBestTime() + C.TAB + printBestPace();
    }

    // recycler
    @Override public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof DistanceItem)) return false;
        DistanceItem d = (DistanceItem) item;
        return distance == d.getDistance();
    }
    @Override public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof DistanceItem)) return false;
        DistanceItem d = (DistanceItem) item;
        return distance == d.getDistance() && bestTime == d.getBestTime() && bestPace == d.getBestPace();
    }

}
