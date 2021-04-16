package com.example.trackfield.view.graphs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.example.trackfield.model.recycleritems.Exerlite;
import com.example.trackfield.service.toolbox.L;
import com.example.trackfield.service.toolbox.M;

import java.util.ArrayList;
import java.util.TreeMap;

public class GraphData {

    private TreeMap<Float, Float> nodes;
    private float min, max;

    private ArrayList<PointF> surPoints = new ArrayList<>();
    private ArrayList<PointF> surFirstConPoints = new ArrayList<>();
    private ArrayList<PointF> surSecondConPoints = new ArrayList<>();

    private Paint paint = new Paint() {{
        setColor(Color.parseColor("#FF5BB974"));
        setAntiAlias(true);
        setStrokeWidth(L.px(3));
        setStyle(Style.STROKE);
    }};
    private Paint areaPaint = new Paint() {{
        setColor(Color.parseColor("#FF5BB974"));
        setAntiAlias(true);
        setStyle(Style.FILL);
    }};

    private int graphType;
    private boolean showPoints;
    private boolean showArea;

    public static final int GRAPH_LINE = 0;
    public static final int GRAPH_BEZIER = 1;
    public static final int GRAPH_SPLINE = 2;
    public static final int GRAPH_BAR = 3;
    public static final int GRAPH_POINTS = 4;

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

    public void setPaint(String colorString) {
        paint.setColor(Color.parseColor(colorString));
    }

    public void setPaint(String colorString, String areaColorString) {
        paint.setColor(Color.parseColor(colorString));
        areaPaint.setColor(Color.parseColor(areaColorString));
    }

    public void setPaint(int colorAttrResId, Context c) {
        paint.setColor(c.getColor(L.getAttr(colorAttrResId, c)));
    }

    public void setPaint(int colorAttrResId, int areaColorAttrResId, Context c) {
        paint.setColor(c.getColor(L.getAttr(colorAttrResId, c)));
        areaPaint.setColor(c.getColor(L.getAttr(areaColorAttrResId, c)));
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

    private void calcConPoints() {
        if (graphType != GRAPH_BEZIER) return;

        for (int i = 1; i < surPoints.size(); i++) {
            surFirstConPoints
                .add(new PointF((surPoints.get(i).x + surPoints.get(i - 1).x) / 2, surPoints.get(i - 1).y));
            surSecondConPoints.add(new PointF((surPoints.get(i).x + surPoints.get(i - 1).x) / 2, surPoints.get(i).y));
        }
    }

    // get

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

    //

    public Paint getPaint() {
        return paint;
    }

    public Paint getAreaPaint() {
        Paint areaPaint = new Paint(paint);
        areaPaint.setAlpha(64);
        areaPaint.setStyle(Paint.Style.FILL);
        return areaPaint;
    }

    //

    public boolean isShowPoints() {
        return showPoints;
    }

    public boolean isShowArea() {
        return showArea;
    }

    public int getGraphType() {
        return graphType;
    }

    public boolean isGraphType(int graphType) {
        return this.graphType == graphType;
    }

    //

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

    //

    public boolean isEmpty() {
        return nodes == null || nodes.size() == 0;
    }

    public boolean sameDataPointsAs(GraphData data) {
        //return dataPoints.equals(data.dataPoints);
        return M.treeMapsEquals(nodes, data.nodes);
    }

    public int getDataPointCount() {
        return nodes.size();
    }

    @Deprecated
    public static TreeMap<Float, Float> ofExerlites(ArrayList<Exerlite> exerlites) {
        TreeMap<Float, Float> map = new TreeMap<>();
        for (int i = 0; i < exerlites.size(); i++) {
            float pace = exerlites.get(i).getPace();
            float key = map.size() == 0 ? 0 : map.lastKey() + 1;
            if (pace != 0) map.put(key, pace);
        }

        return map;
    }

}
