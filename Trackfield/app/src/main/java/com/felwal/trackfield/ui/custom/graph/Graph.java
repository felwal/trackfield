package com.felwal.trackfield.ui.custom.graph;

import com.felwal.trackfield.ui.common.model.RecyclerItem;
import com.felwal.trackfield.utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class Graph extends RecyclerItem {

    private final ArrayList<GraphData> data = new ArrayList<>();
    private float start, end, min, max;

    private final Borders borders;
    private final boolean[] grids = new boolean[2];

    private final boolean widthFixed;
    private final boolean yInverted;
    private final boolean zeroAsMin;

    //

    public Graph(boolean xGrid, Borders borders, boolean widthFixed, boolean yInverted, boolean zeroAsMin) {
        grids[0] = xGrid;
        this.borders = borders;
        this.widthFixed = widthFixed;
        this.yInverted = yInverted;
        this.zeroAsMin = zeroAsMin;
    }

    // set

    /**
     * Adds data sets, topmost first.
     *
     * @param data Data to add
     */
    public void addData(GraphData... data) {
        for (GraphData datum : data) {
            if (datum.isEmpty()) continue;
            updateDomainAndRange(datum);
            this.data.add(0, datum);
        }
    }

    private void updateDomainAndRange(GraphData newData) {
        if (hasData()) {
            start = Math.min(start, newData.getStart());
            end = Math.max(end, newData.getEnd());
            min = zeroAsMin ? 0 : Math.min(min, newData.getMin());
            max = Math.max(max, newData.getMax());
        }
        else {
            start = newData.getStart();
            end = newData.getEnd();
            min = zeroAsMin ? 0 : newData.getMin();
            max = newData.getMax();
        }
    }

    // get data

    public ArrayList<GraphData> getData() {
        return data;
    }

    public boolean hasData() {
        return data != null && data.size() > 0;
    }

    public boolean hasMoreThanOnePoint() {
        for (GraphData datum : data) {
            if (datum.getPointCount() > 1) return true;
        }
        return false;
    }

    public float getStart() {
        return start;
    }

    public float getEnd() {
        return end;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getDomainSize() {
        return end - start;
    }

    public float getRangeSize() {
        return max - min;
    }

    // get canvas properties

    public boolean isWidthFixed() {
        return widthFixed;
    }

    public boolean isXGridShown() {
        return grids[0];
    }

    public boolean isYGridShown() {
        return grids[1];
    }

    public Borders getBorders() {
        return borders;
    }

    // calc

    public float bias(float y) {
        return max == min ? 0 : MathUtils.heaviside(yInverted) + MathUtils.signum(!yInverted) * (y - min) / (max - min);
    }

    // compare

    private boolean sameArgsAs(Graph graph) {
        return Arrays.equals(grids, graph.grids) && borders.equals(graph.borders) && widthFixed == graph.widthFixed &&
            yInverted == graph.yInverted;
    }

    private boolean sameDataAs(Graph graph) {
        if (data.size() != graph.data.size()) return false;
        for (int i = 0; i < graph.data.size(); i++) {
            if (!data.get(i).sameDataPointsAs(graph.data.get(i))) return false;
        }
        return true;
    }

    // extends RecyclerItem

    @Override
    public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof Graph)) return false;
        return sameArgsAs((Graph) item) && item.hasTag(tag);
    }

    @Override
    public boolean sameContentAs(RecyclerItem item) {
        return sameItemAs(item) && sameDataAs((Graph) item);
    }

}
