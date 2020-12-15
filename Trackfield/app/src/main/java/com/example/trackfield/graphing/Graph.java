package com.example.trackfield.graphing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import androidx.annotation.NonNull;

import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.toolbox.Toolbox.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Graph extends RecyclerItem {

    private ArrayList<GraphData> data = new ArrayList<>();
    private float start, end, min, max;

    private boolean[] grids = new boolean[2];
    private boolean[] borders = new boolean[4];
    private boolean widthFixed = true;
    private boolean yInverted = false;

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

    ////


    public Graph(@NonNull GraphData data, boolean xGrid, boolean lBorder, boolean rBorder, boolean tBorder, boolean bBorder, boolean widthFixed, boolean yInverted) {
        this.widthFixed = widthFixed;
        this.yInverted = yInverted;

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
    public void addData(GraphData data) {
        if (data.isEmpty()) return;
        this.data.add(0, data);
        updateDomainAndRange(data);
    }
    private void setDomainAndRange(GraphData data) {
        start = data.getStart();
        end = data.getEnd();
        min = data.getMin();
        max = data.getMax();
    }
    private void updateDomainAndRange(GraphData newData) {
        start = Math.min(start, newData.getStart());
        end = Math.max(end, newData.getEnd());
        min = Math.min(min, newData.getMin());
        max = Math.max(max, newData.getMax());
    }

    // get
    public ArrayList<GraphData> getData() {
        return data;
    }
    public boolean hasData() {
        return data != null && data.size() > 0;
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

    public float bias(float y) {
        return max == min ? 0 : M.heaviside(yInverted) + M.signum(!yInverted) * (y - min) / (max - min);
    }

    // compare
    private boolean sameArgsAs(Graph graph) {
        return Arrays.equals(grids, graph.grids) && Arrays.equals(borders, graph.borders) && widthFixed == graph.widthFixed && yInverted == graph.yInverted;
    }
    private boolean sameDataAs(Graph graph) {
        if (data.size() != graph.data.size()) return false;
        for (int i = 0; i < graph.data.size(); i++) {
            if (!data.get(i).sameDataPointsAs(graph.data.get(i))) return false;
        }
        return true;
    }

    // recycler
    @Override public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof Graph)) return false;
        return sameArgsAs((Graph) item) && item.hasTag(tag);
    }
    @Override public boolean sameContentAs(RecyclerItem item) {
        return sameItemAs(item) && sameDataAs((Graph) item);
    }

}
