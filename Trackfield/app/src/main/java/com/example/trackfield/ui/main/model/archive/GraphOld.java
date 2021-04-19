package com.example.trackfield.ui.main.model.archive;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.trackfield.R;
import com.example.trackfield.ui.main.model.Exerlite;
import com.example.trackfield.ui.main.model.RecyclerItem;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

@Deprecated public class GraphOld extends RecyclerItem {

    private TreeMap<Float, Float> coordinates = new TreeMap<>();

    private float min;
    private float max;

    public enum DataX {
        INDEX,
        _ID
        //DATE
    }
    public enum DataY {
        DISTANCE,
        TIME,
        PACE
    }

    ////

    public GraphOld(ArrayList<Exerlite> exerlites, final DataX xData, final DataY yData) {

        for (int i = 0; i < exerlites.size(); i++) {
            Exerlite e = exerlites.get(i);

            float x, y;
            switch (xData) {
                case INDEX: x = i; break;
                case _ID:   x = (e.get_id()); break;
                default:    x = 0; break;
            }
            switch (yData) {
                case DISTANCE:  y = ((float) e.getDistance()); break;
                case TIME:      y = (e.getTime()); break;
                case PACE:      y = (e.getPace()); break;
                default:        y = 0; break;
            }

            if (y != 0 && (i == 0 || min == 0 || y < min)) { min = y; }
            if (y != 0 && (i == 0 || max == 0 || y > max)) { max = y; }
            coordinates.put(x, y);
        }

    }

    // get
    public TreeMap<Float, Float> getCoordinates() {
        return coordinates;
    }

    public static ConstraintLayout inflateLayout(LayoutInflater inflater, ViewGroup parent) {
        return (ConstraintLayout) inflater.inflate(R.layout.dep_chart, parent, false);
    }
    public void inflateElements(LayoutInflater inflater, LinearLayout ll, ViewGroup parent) {

        for (Map.Entry<Float, Float> coord : coordinates.entrySet()) {

            // element
            ConstraintLayout element = (ConstraintLayout) inflater.inflate(R.layout.dep_chart_element_point, parent, false);
            ll.addView(element);

            // label
            TextView xTv = element.findViewById(R.id.textView_index);
            xTv.setText(coord.getKey() + "");

            // point
            ConstraintLayout point = element.findViewById(R.id.constraintLayout_point);
            if (coord.getValue() != 0) {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) point.getLayoutParams();
                params.verticalBias = bias(coord.getValue());
                point.setLayoutParams(params);
            }
            else { point.setVisibility(View.GONE); }
        }

    }

    // tools
    private float rel(float y) {
        return max == 0 ? 0 : y / max;
    }
    private float bias(float y) {
        return max == min ? 0.5f : (float) ((y - min) / (max - min));
    }

    public int size() {
        return coordinates.size();
    }

    @Override public boolean sameItemAs(RecyclerItem item) {
        if (!(item instanceof GraphOld)) return false;
        GraphOld g = (GraphOld) item;
        return true;
    }
    @Override public boolean sameContentAs(RecyclerItem item) {
        if (!(item instanceof GraphOld)) return false;
        GraphOld g = (GraphOld) item;
        return coordinates == g.getCoordinates();
    }

}
