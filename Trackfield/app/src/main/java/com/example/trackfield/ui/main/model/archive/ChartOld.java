package com.example.trackfield.ui.main.model.archive;

import com.example.trackfield.ui.main.model.RecyclerItem;
import com.example.trackfield.data.db.model.Exercise;
import com.example.trackfield.utils.Constants;

import java.util.ArrayList;

@Deprecated public class ChartOld extends RecyclerItem {

    private float[] y;
    private String[] xLabel;
    private int[] xId;
    private int type = TYPE_WEEKS;

    private float[] yRel;
    private float[] yBias;

    public static int TYPE_WEEKS = 0;
    public static int TYPE_DAILY = 1;
    public static int TYPE_YEAR = 2;

    public enum Data {
        DISTANCE,
        TIME,
        PACE,
        ENERGY,
        POWER
    }
    public enum Label {
        INDEX,
        _ID,
    }

    ////

    public ChartOld(float[] y, String[] xLabel) {
        this.xLabel = xLabel;
        this.y = y;
        setyRel();
        setyBias();
    }
    public ChartOld(float[] y) {
        this.y = y;
        setyRel();
        setyBias();
    }
    public ChartOld(ArrayList<Exercise> exercises, Data y, Label xLabel) {

        this.y = new float[exercises.size()];
        this.xId = new int[exercises.size()];
        this.xLabel = new String[exercises.size()];

        for (int i = 0; i < exercises.size(); i++) {
            Exercise e = exercises.get(i);
            xId[i] = e.get_id();

            switch (y) {
                case DISTANCE:  this.y[i] = ((float) e.getEffectiveDistance()); break;
                case TIME:      this.y[i] = (e.getTime()); break;
                case PACE:      this.y[i] = (e.getPace()); break;
                case ENERGY:    this.y[i] = ((float) e.getEnergy(Constants.UnitEnergy.JOULES)); break;
                case POWER:     this.y[i] = ((float) e.getPower()); break;
            }
            switch (xLabel) {
                case INDEX: this.xLabel[i] = Integer.toString(i + 1); break;
                case _ID: this.xLabel[i] = Integer.toString(e.get_id()); break;
            }
        }

        setyRel();
        setyBias();
    }

    // set
    public void setType(int type) {
        this.type = type;
    }

    // set driven
    private void setyRel() {

        float biggest = y[0];
        for (int i = 1; i < y.length; i++) {
            if (y[i] > biggest) { biggest = y[i]; }
        }

        yRel = new float[y.length];
        for (int i = 0; i < y.length; i++) {
            if (biggest == 0) { yRel[i] = 0; }
            yRel[i] = (float) y[i] / biggest;
        }

    }
    private void setyBias() {

        float biggest = y[0];
        float smallest = y[0];
        for (int i = 1; i < y.length; i++) {
            if (y[i] > biggest) { biggest = y[i]; }
            if (y[i] < smallest) { smallest = y[i]; };
        }

        // biggest = 1, smallest = 0
        yBias = new float[y.length];
        for (int i = 0; i < y.length; i++) {
            yBias[i] = (float) (y[i] - smallest) / (biggest - smallest);
        }

    }

    // get
    public float[] getY() {
        return y;
    }
    public String[] getxLabel() {
        return xLabel;
    }
    public int[] getxId() {
        return xId;
    }
    public int getType() {
        return type;
    }
    public boolean isType(int type) {
        return this.type == type;
    }

    // get driven
    public float[] getyRel() {
        return yRel;
    }
    public float[] getyBias() {
        return yBias;
    }
    public float[] getyBiasCalc(int[] hideIndices, boolean includeZeros) {

        float biggest = y[0];
        float smallest = y[0];
        for (int i = 1; i < y.length; i++) {
            if (y[i] > biggest) { biggest = y[i]; }
            if (y[i] < smallest && y[i] != 0) { smallest = y[i]; };
        }

        // biggest = 1, smallest = 0
        float[] yBiasCalc = new float[y.length];
        for (int i = 0; i < y.length; i++) {
            if (y[i] != 0) {
                yBiasCalc[i] = (y[i] - smallest) / (biggest - smallest);
            }
            else { yBiasCalc[i] = 0.5f; }
        }

        return yBiasCalc;
    }

    public int length() {
        return y.length;
    }

    @Override public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof ChartOld)) return false;
        ChartOld c = (ChartOld) item;
        return true;
    }
    @Override public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof ChartOld)) return false;
        ChartOld c = (ChartOld) item;
        return y == c.getY();
    }

}
