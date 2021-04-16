package com.example.trackfield.view.graphs;

import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.example.trackfield.model.recycleritems.RecyclerItem;
import com.example.trackfield.service.toolbox.L;
import com.example.trackfield.service.toolbox.M;

import java.util.ArrayList;
import java.util.Arrays;

public class Graph extends RecyclerItem {

    private ArrayList<GraphData> data = new ArrayList<>();
    private float start, end, min, max;

    private boolean[] grids = new boolean[2];
    private boolean[] borders = new boolean[4];
    private boolean widthFixed;
    private boolean yInverted;
    private boolean zeroAsMin;

    private boolean xGrid = true;
    private boolean yBorders = true;

    private Paint borderPaint = new Paint() {{
        setColor(Color.parseColor("#FF3E3F43"));
        setAntiAlias(true);
        setStrokeWidth(L.px(1));
        setStyle(Paint.Style.STROKE);
    }};
    private Paint gridPaint = new Paint() {{
        setColor(Color.parseColor("#FF2F3033"));
        setAntiAlias(true);
        setStrokeWidth(L.px(1));
        setStyle(Paint.Style.STROKE);
    }};

    //

    public Graph(@NonNull GraphData data, boolean xGrid, boolean lBorder, boolean rBorder, boolean tBorder,
        boolean bBorder, boolean widthFixed, boolean yInverted, boolean zeroAsMin) {
        this.widthFixed = widthFixed;
        this.yInverted = yInverted;
        this.zeroAsMin = zeroAsMin;

        grids[0] = xGrid;
        borders[0] = lBorder;
        borders[1] = rBorder;
        borders[2] = tBorder;
        borders[3] = bBorder;

        if (data.isEmpty()) return;
        this.data.add(data);
        setDomainAndRange(data);
    }

    // set

    /**
     * Adds data nodes, topmost first.
     *
     * @param data Data to add
     */
    public void addData(GraphData data) {
        if (data.isEmpty()) return;
        this.data.add(0, data);
        updateDomainAndRange(data);
    }

    private void setDomainAndRange(GraphData data) {
        start = data.getStart();
        end = data.getEnd();
        min = zeroAsMin ? 0 : data.getMin();
        max = data.getMax();
    }

    private void updateDomainAndRange(GraphData newData) {
        start = Math.min(start, newData.getStart());
        end = Math.max(end, newData.getEnd());
        min = zeroAsMin ? 0 : Math.min(min, newData.getMin());
        max = Math.max(max, newData.getMax());
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

    public boolean isxGridShown() {
        return grids[0];
    }

    public boolean isyGridShown() {
        return grids[1];
    }

    public boolean isBorderShown(int border) {
        if (border < 0 || border > borders.length) return false;
        return borders[border];
    }

    public boolean[] getBorders() {
        return borders;
    }

    // calc

    public float bias(float y) {
        return max == min ? 0 : M.heaviside(yInverted) + M.signum(!yInverted) * (y - min) / (max - min);
    }

    // compare

    private boolean sameArgsAs(Graph graph) {
        return Arrays.equals(grids, graph.grids) && Arrays.equals(borders, graph.borders) &&
            widthFixed == graph.widthFixed && yInverted == graph.yInverted;
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
