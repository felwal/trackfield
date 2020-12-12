package com.example.trackfield.views;

import com.example.trackfield.items.headers.RecyclerItem;

import java.util.ArrayList;
import java.util.TreeMap;

public class GraphData extends RecyclerItem {

    private TreeMap<Float, Float> dataPoints = new TreeMap<>();
    private float min, max;

    private int graphType; // line, bezier, spline, bar
    private boolean showPoints, showArea;
    private boolean scrollable;

    ////

    public GraphData(TreeMap<Float, Float> dataPoints) {
        this.dataPoints = dataPoints;
        setMinAndMax();
    }
    public GraphData(ArrayList<Float> dataPoints) {
        for (int i = 0; i < dataPoints.size(); i++) {
            this.dataPoints.put((float) i, dataPoints.get(i));
        }
        setMinAndMax();
    }
    public GraphData() {
        dataPoints.put(0f,1f);
        dataPoints.put(1f,2f);
        dataPoints.put(2f,4f);
        dataPoints.put(3f,3f);
        dataPoints.put(4f,1f);
        dataPoints.put(5f,2f);
        dataPoints.put(6f,0f);
        dataPoints.put(7f,2f);
        dataPoints.put(8f,4f);
        //dataPoints.put(9f,3f);
        dataPoints.put(10f,1f);
        dataPoints.put(11f,2f);

        setMinAndMax();
    }

    // set
    private void setMinAndMax() {

        min = dataPoints.firstEntry().getValue();
        max = dataPoints.firstEntry().getValue();

        for (TreeMap.Entry<Float, Float> entry : dataPoints.entrySet()) {
            float y = entry.getValue();
            if (y < min) min = y;
            if (y > max) max = y;
        }

        /*for (int i = 0; i < dataPoints.size(); i++) {
            float y = dataPoints.get(i);
            if (y != 0 && (i == 0 || min == 0 || y < min)) min = y;
            if (y != 0 && (i == 0 || max == 0 || y > max)) max = y;
        }*/
    }

    // get
    public TreeMap<Float, Float> getDataPoints() {
        return dataPoints;
    }
    public float getMin() {
        return min;
    }
    public float getMax() {
        return max;
    }

    public float get(int index) {
        return dataPoints.get(index);
    }
    public int size() {
        return dataPoints.size();
    }

    // recycler item
    @Override public boolean sameItemAs(RecyclerItem item) {
        return false;
    }
    @Override public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof GraphData)) return false;
        return dataPoints.equals(((GraphData) item).getDataPoints());
    }

}
