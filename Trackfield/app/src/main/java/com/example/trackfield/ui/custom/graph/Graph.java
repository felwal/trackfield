package com.example.trackfield.ui.custom.graph;

import android.graphics.Color;
import android.graphics.Paint;

import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.utils.MathUtils;
import com.example.trackfield.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class Graph extends RecyclerItem {

    private ArrayList<GraphData> data = new ArrayList<>();
    private float start, end, min, max;

    private boolean[] grids = new boolean[2];
    private Borders borders;

    private boolean widthFixed;
    private boolean yInverted;
    private boolean zeroAsMin;

    private boolean xGrid = true;
    private boolean yBorders = true;

    private Paint borderPaint = new Paint() {{
        setColor(Color.parseColor("#FF3E3F43"));
        setAntiAlias(true);
        setStrokeWidth(ScreenUtils.px(1));
        setStyle(Paint.Style.STROKE);
    }};
    private Paint gridPaint = new Paint() {{
        setColor(Color.parseColor("#FF2F3033"));
        setAntiAlias(true);
        setStrokeWidth(ScreenUtils.px(1));
        setStyle(Paint.Style.STROKE);
    }};

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

    public boolean isxGridShown() {
        return grids[0];
    }

    public boolean isyGridShown() {
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
