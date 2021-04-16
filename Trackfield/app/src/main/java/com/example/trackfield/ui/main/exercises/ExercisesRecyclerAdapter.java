package com.example.trackfield.ui.main.exercises;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.model.recycleritems.RecyclerItem;
import com.example.trackfield.ui.main.RecyclerAdapter;

import java.time.LocalDate;
import java.util.ArrayList;

public class ExercisesRecyclerAdapter extends RecyclerAdapter {

    @Deprecated public static int weekAmount = 12;

    //

    public ExercisesRecyclerAdapter(ArrayList<RecyclerItem> itemList, Context c) {
        super(itemList, c);
        now = LocalDate.now();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_ITEM) {
            ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.item_exercise, parent, false);
            return new ExerciseVH(cl);
        }

        // deprecated
        else if (viewType == ITEM_CHART_OLD) {
            ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.dep_chart, parent, false);
            ConstraintLayout[] elements = new ConstraintLayout[weekAmount];
            for (int i = 0; i < weekAmount; i++) {
                elements[i] = (ConstraintLayout) inflater.inflate(R.layout.dep_chart_element_bar, parent, false);
            }
            return new ChartVH(cl, elements);
        }
        else if (viewType == ITEM_CHART_DAILY) {
            ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.dep_chart_daily, parent, false);
            return new DailyChartVH(cl);
        }
        else if (viewType == ITEM_CHART_YEAR) {
            ConstraintLayout cl = (ConstraintLayout) inflater.inflate(R.layout.dep_chart, parent, false);
            return new YearChartVH(cl, parent);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    // holders

    @Deprecated public class ChartVH extends RecyclerView.ViewHolder {

        public ConstraintLayout constraintLayout;
        public LinearLayout linearLayout;
        public TextView[] weeks = new TextView[weekAmount];
        public TextView[] distances = new TextView[weekAmount];
        public View[] bars = new View[weekAmount];

        public ChartVH(ConstraintLayout cl, ConstraintLayout[] elements) {
            super(cl);
            constraintLayout = cl;
            linearLayout = cl.findViewById(R.id.linearLayout_elementContainer);
            for (int i = 0; i < weekAmount; i++) {
                linearLayout.addView(elements[i]);
                weeks[i] = elements[i].findViewById(R.id.textView_week);
                distances[i] = elements[i].findViewById(R.id.textView_distance);
                bars[i] = elements[i].findViewById(R.id.view_bar);
            }
        }

    }

    @Deprecated public class DailyChartVH extends RecyclerView.ViewHolder {

        public ConstraintLayout constraintLayout;
        public View[] bars = new View[7];

        public DailyChartVH(ConstraintLayout cl) {
            super(cl);
            constraintLayout = cl;
            bars[0] = cl.findViewById(R.id.view_barMon);
            bars[1] = cl.findViewById(R.id.view_barTue);
            bars[2] = cl.findViewById(R.id.view_barWed);
            bars[3] = cl.findViewById(R.id.view_barThu);
            bars[4] = cl.findViewById(R.id.view_barFri);
            bars[5] = cl.findViewById(R.id.view_barSat);
            bars[6] = cl.findViewById(R.id.view_barSun);
        }

    }

    @Deprecated public class YearChartVH extends RecyclerView.ViewHolder {

        public ConstraintLayout constraintLayout;
        public LinearLayout linearLayout;
        public ViewGroup parent;
        //public View[] bars = new View[D.weekAmount];

        public YearChartVH(ConstraintLayout cl, ViewGroup parent) {
            super(cl);
            constraintLayout = cl;
            linearLayout = cl.findViewById(R.id.linearLayout_elementContainer);
            this.parent = parent;
        }

    }

}
