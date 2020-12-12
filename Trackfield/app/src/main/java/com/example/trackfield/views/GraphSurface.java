package com.example.trackfield.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.trackfield.toolbox.Toolbox.*;

import java.util.ArrayList;
import java.util.TreeMap;

public class GraphSurface extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener, Runnable {

    private Thread drawThread;
    private SurfaceHolder holder;

    // paint
    Paint curvePaint = new Paint() {{
        setColor(Color.parseColor("#FF5BB974"));
        setAntiAlias(true);
        setStrokeWidth(L.px(3));
        setStyle(Paint.Style.STROKE);
    }};
    Paint pointPaint = new Paint() {{
        setColor(Color.parseColor("#FF5BB974"));
        setAntiAlias(true);
        setStrokeWidth(L.px(3));
        setStyle(Paint.Style.FILL_AND_STROKE);
    }};
    Paint barPaint = new Paint() {{
        setColor(Color.parseColor("#FF5BB974"));
        setAntiAlias(true);
        setStyle(Paint.Style.FILL);
    }};
    Paint areaPaint = new Paint() {{
        setColor(Color.parseColor("#405BB974"));
        setAntiAlias(true);
        setStyle(Style.FILL);
    }};
    Paint borderPaint = new Paint() {{
        setColor(Color.parseColor("#FF3E3F43"));
        setAntiAlias(true);
        setStrokeWidth(L.px(1));
        setStyle(Paint.Style.STROKE);
    }};
    Paint xPaint = new Paint() {{
        setColor(Color.parseColor("#FF2F3033"));
        setAntiAlias(true);
        setStrokeWidth(L.px(1));
        setStyle(Paint.Style.STROKE);
    }};

    // states
    private boolean surfaceReady = false;
    private boolean drawingActive = false;

    // points
    private ArrayList<PointF> points = new ArrayList<>();
    private ArrayList<PointF> firstConPoints = new ArrayList<>();
    private ArrayList<PointF> secondConPoints = new ArrayList<>();

    private static final float MARGIN = 50;
    private static final int MAX_FRAME_TIME = (int) (1000.0 / 60.0); // Time per frame for 60 FPS
    private static final String TAG_LOG = "surface";

    ////

    public GraphSurface(Context context, AttributeSet attrs) {
        super(context, attrs);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        setOnTouchListener(this);
    }

    // points
    public void calcPoints(GraphData data) {

        float height = getHeight() - 2*MARGIN;//getLargeBarHeight() - CURVE_BOTTOM_MARGIN;
        float deltaX = L.px(30);//(float) getWidth() / data.size();

        for (TreeMap.Entry<Float, Float> entry : data.getDataPoints().entrySet()) {
            float y = height - (entry.getValue() / data.getMax() * height) + MARGIN;
            float x = L.px(16) + entry.getKey() * deltaX;//deltaX * i;
            points.add(new PointF(x, y));
        }

        calcConPoints();
    }
    private void calcConPoints() {

        for (int i = 1; i < points.size(); i++) {
            firstConPoints.add(new PointF((points.get(i).x + points.get(i-1).x) / 2, points.get(i-1).y));
            secondConPoints.add(new PointF((points.get(i).x + points.get(i-1).x) / 2, points.get(i).y));
        }
    }

    // draw
    private void drawCurves(Canvas canvas) {

        canvas.drawARGB(255, 32, 33, 36); // BG

        drawBorders(canvas);
        drawX(canvas);

        //drawBezierCurve(canvas);
        //drawLineCurve(canvas);
        drawBars(canvas);
    }
    private void drawLineCurve(Canvas canvas) {
        if (points.isEmpty()) return;

        Path path = new Path();
        path.reset();

        for (int i = 0; i < points.size(); i++) {
            if (i == 0) path.moveTo(points.get(0).x, points.get(0).y);
            else path.lineTo(points.get(i).x, points.get(i).y);
            canvas.drawCircle(points.get(i).x, points.get(i).y, L.px(4), pointPaint);
        }

        canvas.drawPath(path, curvePaint);

        path.lineTo(points.get(points.size()-1).x, getHeight() - MARGIN);
        path.lineTo(points.get(0).x, getHeight() - MARGIN);
        path.lineTo(points.get(0).x, points.get(0).y);
        canvas.drawPath(path, areaPaint);
    }
    private void drawBezierCurve(Canvas canvas) {
        if (points.isEmpty() && firstConPoints.isEmpty() && secondConPoints.isEmpty()) return;

        Path path = new Path();
        path.reset();

        for (int i = 0; i < points.size(); i++) {
            if (i == 0) path.moveTo(points.get(0).x, points.get(0).y);
            else path.cubicTo(
                    firstConPoints.get(i-1).x, firstConPoints.get(i-1).y,
                    secondConPoints.get(i-1).x, secondConPoints.get(i-1).y,
                    points.get(i).x, points.get(i).y
            );
            canvas.drawCircle(points.get(i).x, points.get(i).y, L.px(4), pointPaint);
        }

        canvas.drawPath(path, curvePaint);

        path.lineTo(points.get(points.size()-1).x, getHeight() - MARGIN);
        path.lineTo(points.get(0).x, getHeight() - MARGIN);
        path.lineTo(points.get(0).x, points.get(0).y);
        canvas.drawPath(path, areaPaint);
    }
    private void drawBars(Canvas canvas) {
        if (points.isEmpty()) return;

        float barRadius = L.px(6);

        for (PointF p : points) {
            canvas.drawRoundRect(p.x - barRadius, p.y, p.x + barRadius, getHeight() - MARGIN, barRadius, barRadius, barPaint);
        }

        //canvas.drawPath(path, pointPaint);
    }

    private void drawBorders(Canvas canvas) {

        Path path = new Path();
        path.reset();
        float min = getHeight() - MARGIN;
        float max = MARGIN;

        path.moveTo(points.get(0).x, min);
        path.lineTo(getWidth(), min);

        path.moveTo(points.get(0).x, max);
        path.lineTo(getWidth(), max);

        canvas.drawPath(path, borderPaint);
    }
    private void drawX(Canvas canvas) {

        Path path = new Path();
        path.reset();
        float min = getHeight() - MARGIN;
        float max = MARGIN;

        for (PointF p : points) {
            path.moveTo(p.x, min);
            path.lineTo(p.x, max);
        }

        canvas.drawPath(path, xPaint);
    }

    @Override public void run() {
        Log.d(TAG_LOG, "Draw thread started");
        long frameStartTime;
        long frameTime;

        // In order to work reliable on Nexus 7, we place ~500ms delay at the start of drawing thread // (AOSP - Issue 58385)
        if (android.os.Build.BRAND.equalsIgnoreCase("google") && android.os.Build.MANUFACTURER.equalsIgnoreCase("asus") && android.os.Build.MODEL.equalsIgnoreCase("Nexus 7")) {
            Log.w(TAG_LOG, "Sleep 500ms (Device: Asus Nexus 7)");
            try { Thread.sleep(500); }
            catch (InterruptedException ignored) { }
        }

        try {
            while (drawingActive) {
                if (holder == null) return;

                frameStartTime = System.nanoTime();
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    try { drawCurves(canvas); }
                    finally { holder.unlockCanvasAndPost(canvas); }
                }

                // calculate the time required to draw the frame in ms
                frameTime = (System.nanoTime() - frameStartTime) / 1000000;
                if (frameTime < MAX_FRAME_TIME) {
                    // faster than the max fps - limit the FPS
                    try { Thread.sleep(MAX_FRAME_TIME - frameTime); }
                    catch (InterruptedException e) {}
                }
            }
        }
        catch (Exception e) { Log.w(TAG_LOG, "Exception while locking/unlocking"); }
        Log.d(TAG_LOG, "Draw thread finished");
    }

    // thread
    public void startDrawThread() {
        if (surfaceReady && drawThread == null) {
            drawThread = new Thread(this, "Draw thread");
            drawingActive = true;
            drawThread.start();
        }
    }
    public void stopDrawThread() {
        if (drawThread == null) {
            Log.d(TAG_LOG, "DrawThread is null");
            return;
        }
        drawingActive = false;
        while (true) {
            try {
                Log.d(TAG_LOG, "Request last frame");
                drawThread.join(5000);
                break;
            }
            catch (Exception e) {
                Log.e(TAG_LOG, "Could not join with draw thread");
            }
        }
        drawThread = null;
    }

    @Override public boolean onTouch(View v, MotionEvent event) {
        // Handle touch events
        return true;
    }
    @Override public void surfaceCreated(SurfaceHolder holder) {
        this.holder = holder;

        calcPoints(new GraphData());

        if (drawThread != null) {
            Log.d(TAG_LOG, "draw thread still active..");
            drawingActive = false;
            try { drawThread.join(); }
            catch (InterruptedException e) {}
        }

        surfaceReady = true;
        startDrawThread();
        Log.d(TAG_LOG, "Created");
    }
    @Override public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface is not used anymore - stop the drawing thread
        stopDrawThread();
        // and release the surface
        holder.getSurface().release();

        this.holder = null;
        surfaceReady = false;
        Log.d(TAG_LOG, "Destroyed");
    }
    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }

        // resize your UI
    }

}