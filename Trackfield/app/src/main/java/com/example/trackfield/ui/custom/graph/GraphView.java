package com.example.trackfield.ui.custom.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.trackfield.R;
import com.example.trackfield.utils.LayoutUtils;
import com.example.trackfield.utils.ScreenUtils;
import com.example.trackfield.utils.annotations.Unfinished;

import java.util.ArrayList;
import java.util.TreeMap;

public class GraphView extends View implements View.OnTouchListener {

    private Graph graph;

    // states
    private boolean updateData = true;

    // dimens
    private float start, end;
    private float bottom, top;
    private float width, height;

    private float unitWidth = ScreenUtils.px(30);
    private final float barRadius = ScreenUtils.px(6);

    private final Paint borderPaint = new Paint() {{
        // colorGray5 / colorOnBackground
        setColor(getResources().getColor(LayoutUtils.getAttr(R.attr.colorOnBackground, getContext())));
        setAntiAlias(true);
        setStrokeWidth(ScreenUtils.px(1));
        setStyle(Paint.Style.STROKE);
    }};
    private final Paint gridPaint = new Paint() {{
        // colorGrey3 / strokeColor
        setColor(getResources().getColor(LayoutUtils.getAttr(R.attr.strokeColor, getContext())));
        setAntiAlias(true);
        setStrokeWidth(ScreenUtils.px(1));
        setStyle(Paint.Style.STROKE);
    }};

    //

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }

    public void setGraph(Graph graph) {
        if (this.graph == null || !sameGraphAs(graph)) {
            this.graph = graph;
            updateData = true;
        }
    }

    // extends View

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // calc
        calcDimens();
        calcPoints();

        // draw
        drawGrid(canvas);
        drawBorders(canvas);
        for (GraphData datum : graph.getData()) {
            switch (datum.getGraphType()) {
                case GraphData.GRAPH_LINE:
                    drawLineCurve(canvas, datum);
                    break;
                case GraphData.GRAPH_BEZIER:
                    drawBezierCurve(canvas, datum);
                    break;
                case GraphData.GRAPH_SPLINE:
                    drawSplineCurve(canvas, datum);
                    break;
                case GraphData.GRAPH_BAR:
                    drawBars(canvas, datum);
                    break;
                case GraphData.GRAPH_POINTS:
                    drawPoints(canvas, datum);
                    break;
            }
        }
    }

    @Unfinished
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // handle touch events
        return true;
    }

    // calc

    private void calcDimens() {
        start = 0;
        end = getWidth();
        width = end - start;
        top = getPaddingTop();
        bottom = getHeight() - getPaddingBottom();
        height = bottom - top;
    }

    private void calcPoints() {
        if (!updateData) return;

        boolean hasBars = graph.hasData() && graph.getData().get(0).isGraphType(GraphData.GRAPH_BAR);

        // dimensions
        if (graph.isWidthFixed()) {
            unitWidth = (width - (hasBars ? 2 * barRadius : 0)) / graph.getDomainSize();
        }
        else {
            float totalWidth = ScreenUtils.px(24) + graph.getDomainSize() * unitWidth;
            ViewGroup.LayoutParams params = getLayoutParams();
            params.width = (int) totalWidth;
            setLayoutParams(params);
        }

        for (GraphData datum : graph.getData()) {
            ArrayList<PointF> surPoints = new ArrayList<>();

            for (TreeMap.Entry<Float, Float> entry : datum.getNodes().entrySet()) {
                float y = height + getPaddingTop() - graph.bias(entry.getValue()) * height;
                float x = (entry.getKey() - graph.getStart()) * unitWidth + (hasBars ? barRadius : 0);
                surPoints.add(new PointF(x, y));
            }
            datum.setSurfacePoints(surPoints);
        }

        updateData = false;
    }

    // borders

    private void drawBorders(Canvas canvas) {
        Borders borders = graph.getBorders();
        Path path = new Path();
        path.reset();

        if (borders.isLeft()) {
            path.moveTo(start, bottom);
            path.lineTo(start, top);
        }
        if (borders.isRight()) {
            path.moveTo(end, bottom);
            path.lineTo(end, top);
        }
        if (borders.isTop()) {
            path.moveTo(start, top);
            path.lineTo(end, top);
        }
        if (borders.isBottom()) {
            path.moveTo(start, bottom);
            path.lineTo(end, bottom);
        }

        canvas.drawPath(path, borderPaint);
    }

    private void drawGrid(Canvas canvas) {
        Path path = new Path();
        path.reset();

        if (graph.isXGridShown()) {
            for (int xNum = 1; xNum < width / unitWidth; xNum++) {
                float x = xNum * unitWidth;
                path.moveTo(x, bottom);
                path.lineTo(x, top);
            }
        }

        canvas.drawPath(path, gridPaint);
    }

    // draw curves

    private void drawLineCurve(Canvas canvas, GraphData data) {
        ArrayList<PointF> points = data.getSurPoints();
        if (points.isEmpty()) return;

        Path path = new Path();
        path.reset();

        for (int i = 0; i < points.size(); i++) {
            PointF p = points.get(i);

            if (i == 0) path.moveTo(p.x, p.y);
            else path.lineTo(p.x, p.y);
            drawPointIfEnabled(canvas, p, data);
        }

        canvas.drawPath(path, data.getPaint());
        drawAreaIfEnabled(canvas, path, data);
    }

    private void drawBezierCurve(Canvas canvas, GraphData data) {
        ArrayList<PointF> points = data.getSurPoints();
        ArrayList<PointF> firstConPoints = data.getSurFirstConPoints();
        ArrayList<PointF> secondConPoints = data.getSurSecondConPoints();
        if (points.isEmpty() && firstConPoints.isEmpty() && secondConPoints.isEmpty()) return;

        Path path = new Path();
        path.reset();

        for (int i = 0; i < points.size(); i++) {
            PointF p = points.get(i);

            if (i == 0) path.moveTo(p.x, p.y);
            else path.cubicTo(
                firstConPoints.get(i - 1).x, firstConPoints.get(i - 1).y,
                secondConPoints.get(i - 1).x, secondConPoints.get(i - 1).y,
                p.x, p.y
            );
            drawPointIfEnabled(canvas, p, data);
        }

        canvas.drawPath(path, data.getPaint());
        drawAreaIfEnabled(canvas, path, data);
    }

    @Unfinished
    private void drawSplineCurve(Canvas canvas, GraphData data) {
        // TODO: implement drawing spline curve
        drawLineCurve(canvas, data);
    }

    private void drawBars(Canvas canvas, GraphData data) {
        float cornerRadius = ScreenUtils.px(1);
        float zeroHeight = ScreenUtils.px(1);

        ArrayList<PointF> points = data.getSurPoints();
        if (points.isEmpty()) return;

        for (PointF p : points) {
            canvas.drawRoundRect(p.x - barRadius, p.y == bottom ? bottom - zeroHeight : p.y, p.x + barRadius, bottom,
                cornerRadius, cornerRadius, data.getPaint());
            drawPointIfEnabled(canvas, p, data);
        }
    }

    private void drawPoints(Canvas canvas, GraphData data) {
        ArrayList<PointF> points = data.getSurPoints();
        if (points.isEmpty()) return;

        Path path = new Path();
        path.reset();

        for (int i = 0; i < points.size(); i++) {
            PointF p = points.get(i);
            path.moveTo(p.x, p.y);
            drawPoint(canvas, p, data.getPaint());
        }
    }

    // draw single

    private void drawPoint(Canvas canvas, PointF p, Paint paint) {
        canvas.drawCircle(p.x, p.y, ScreenUtils.px(4), paint);
    }

    private void drawPointIfEnabled(Canvas canvas, PointF p, GraphData data) {
        if (data.arePointsShown()) drawPoint(canvas, p, data.getPaint());
    }

    private void drawAreaIfEnabled(Canvas canvas, Path path, GraphData data) {
        ArrayList<PointF> points = data.getSurPoints();
        if (!data.isAreaShown()) return;

        path.lineTo(points.get(points.size() - 1).x, getHeight() - getPaddingBottom());
        path.lineTo(points.get(0).x, getHeight() - getPaddingBottom());
        path.lineTo(points.get(0).x, points.get(0).y);
        canvas.drawPath(path, data.getAreaPaint());
    }

    // compare

    public boolean sameGraphAs(Graph graph) {
        return this.graph.sameContentAs(graph);
    }

}