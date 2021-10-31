package com.felwal.trackfield.ui.widget.graph;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.felwal.trackfield.utils.LayoutUtils;
import com.felwal.trackfield.utils.ScreenUtils;
import com.felwal.trackfield.utils.TypeUtils;

import java.util.ArrayList;
import java.util.TreeMap;

public class GraphData {

    // graph types
    public static final int GRAPH_LINE = 0;
    public static final int GRAPH_BEZIER = 1;
    public static final int GRAPH_SPLINE = 2;
    public static final int GRAPH_BAR = 3;
    public static final int GRAPH_POINTS = 4;

    // value nodes
    private final TreeMap<Float, Float> nodes;
    private float min, max;

    // surface points
    private ArrayList<PointF> surPoints = new ArrayList<>();
    private final ArrayList<PointF> surFirstConPoints = new ArrayList<>();
    private final ArrayList<PointF> surSecondConPoints = new ArrayList<>();

    private final int graphType;
    private final boolean showPoints;
    private final boolean showArea;

    private final Paint paint = new Paint() {{
        setColor(Color.parseColor("#FF5BB974"));
        setAntiAlias(true);
        setStrokeWidth(ScreenUtils.px(3));
        setStyle(Style.STROKE);
    }};

    //

    public GraphData(TreeMap<Float, Float> nodes, int graphType, boolean showPoints, boolean showArea) {
        this.nodes = nodes;
        this.graphType = graphType;
        this.showPoints = showPoints;
        this.showArea = showArea;

        if (isGraphType(GRAPH_BAR) || isGraphType(GRAPH_POINTS)) {
            paint.setStyle(Paint.Style.FILL);
        }

        calcMinAndMax();
    }

    // set

    public void setSurfacePoints(ArrayList<PointF> surPoints) {
        this.surPoints = surPoints;
        calcConPoints();
    }

    public void setPaint(int colorAttrResId, Context c) {
        paint.setColor(c.getColor(LayoutUtils.getIdAttr(colorAttrResId, c)));
    }

    // calc

    private void calcMinAndMax() {
        if (getDataPointCount() == 0) return;

        min = nodes.firstEntry().getValue();
        max = nodes.firstEntry().getValue();

        for (TreeMap.Entry<Float, Float> entry : nodes.entrySet()) {
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

    /**
     * Calculates the bezier connection points
     */
    private void calcConPoints() {
        if (graphType != GRAPH_BEZIER) return;

        for (int i = 1; i < surPoints.size(); i++) {
            PointF firstPoint = new PointF((surPoints.get(i).x + surPoints.get(i - 1).x) / 2, surPoints.get(i - 1).y);
            PointF secondPoint = new PointF((surPoints.get(i).x + surPoints.get(i - 1).x) / 2, surPoints.get(i).y);

            surFirstConPoints.add(firstPoint);
            surSecondConPoints.add(secondPoint);
        }
    }

    // get points

    public TreeMap<Float, Float> getNodes() {
        return nodes;
    }

    public ArrayList<PointF> getSurPoints() {
        return surPoints;
    }

    public ArrayList<PointF> getSurFirstConPoints() {
        return surFirstConPoints;
    }

    public ArrayList<PointF> getSurSecondConPoints() {
        return surSecondConPoints;
    }

    // get paint

    public Paint getPaint() {
        return paint;
    }

    public Paint getAreaPaint() {
        Paint areaPaint = new Paint(paint);
        areaPaint.setAlpha(64);
        areaPaint.setStyle(Paint.Style.FILL);
        return areaPaint;
    }

    // get

    public boolean arePointsShown() {
        return showPoints;
    }

    public boolean isAreaShown() {
        return showArea;
    }

    public int getGraphType() {
        return graphType;
    }

    public boolean isGraphType(int graphType) {
        return this.graphType == graphType;
    }

    public boolean isEmpty() {
        return nodes == null || nodes.size() == 0;
    }

    public boolean sameDataPointsAs(GraphData data) {
        //return dataPoints.equals(data.dataPoints);
        return TypeUtils.treeMapsEquals(nodes, data.nodes);
    }

    public int getDataPointCount() {
        return nodes.size();
    }

    // get domain and range

    public float getStart() {
        return nodes.firstKey();
    }

    public float getEnd() {
        return nodes.lastKey();
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getDomainSize() {
        return nodes.lastKey() - nodes.firstKey();
    }

    public float getPointCount() {
        if (nodes == null) return 0;
        return nodes.size();
    }

}
