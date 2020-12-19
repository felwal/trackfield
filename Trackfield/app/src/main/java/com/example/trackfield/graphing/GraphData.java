package com.example.trackfield.graphing;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.example.trackfield.items.Exerlite;
import com.example.trackfield.toolbox.Toolbox.*;

import java.util.ArrayList;
import java.util.TreeMap;

public class GraphData {

    private TreeMap<Float, Float> dataPoints;
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

    ////

    public GraphData(TreeMap<Float, Float> dataPoints, int graphType, boolean showPoints, boolean showArea) {
        this.dataPoints = dataPoints;
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

    // calc
    private void calcMinAndMax() {
        if (getDataPointCount() == 0) return;

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
    private void calcConPoints() {
        if (graphType != GRAPH_BEZIER) return;

        for (int i = 1; i < surPoints.size(); i++) {
            surFirstConPoints.add(new PointF((surPoints.get(i).x + surPoints.get(i-1).x) / 2, surPoints.get(i-1).y));
            surSecondConPoints.add(new PointF((surPoints.get(i).x + surPoints.get(i-1).x) / 2, surPoints.get(i).y));
        }
    }

    // get
    public TreeMap<Float, Float> getDataPoints() {
        return dataPoints;
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

    public Paint getPaint() {
        return paint;
    }
    public Paint getAreaPaint() {
        Paint areaPaint = new Paint(paint);
        areaPaint.setAlpha(64);
        areaPaint.setStyle(Paint.Style.FILL);
        return areaPaint;
    }

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

    public float getStart() {
        return dataPoints.firstKey();
    }
    public float getEnd() {
        return dataPoints.lastKey();
    }
    public float getMin() {
        return min;
    }
    public float getMax() {
        return max;
    }
    public float getDomainSize() {
        return dataPoints.lastKey() - dataPoints.firstKey();
    }
    public float getPointCount() {
        if (dataPoints == null) return 0;
        return dataPoints.size();
    }

    public boolean isEmpty() {
        return dataPoints == null || dataPoints.size() == 0;
    }
    public boolean sameDataPointsAs(GraphData data) {
        //return dataPoints.equals(data.dataPoints);
        return M.treeMapsEquals(dataPoints, data.dataPoints);
    }
    public int getDataPointCount() {
        return dataPoints.size();
    }

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
