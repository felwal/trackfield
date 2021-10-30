package com.felwal.trackfield.ui.custom.sheet;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.felwal.trackfield.R;
import com.felwal.trackfield.data.db.DbReader;
import com.felwal.trackfield.data.db.model.Exercise;
import com.felwal.trackfield.ui.exercisedetail.ExerciseDetailActivity;
import com.felwal.trackfield.utils.AppConsts;
import com.felwal.trackfield.utils.LayoutUtils;

public class PeekSheet extends BaseSheet {

    // bundle keys
    private static final String BUNDLE_ID = "id";

    private static final String TAG = "peekSheet";

    private SheetListener listener;

    // arguments
    private Exercise exercise;

    //

    public static PeekSheet newInstance(int exerciseId) {
        PeekSheet instance = new PeekSheet();
        Bundle bundle = new Bundle();

        bundle.putInt(BUNDLE_ID, exerciseId);

        instance.setArguments(bundle);
        return instance;
    }

    // extends DialogFragment

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.sheet_peek, container, false);
        buildSheet();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context c) {
        super.onAttach(c);

        try {
            listener = (SheetListener) c;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DialogListener");
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        listener.onPeekSheetClick(exercise.getId());
    }

    // extends BaseSheet

    @Override
    protected void unpackBundle() {
        Bundle bundle = getArguments();

        if (bundle != null) {
            int id = bundle.getInt(BUNDLE_ID, 0);
            exercise = DbReader.get(a).getExercise(id);
        }

        tag = TAG;
    }

    // set

    private void buildSheet() {
        // find
        TextView routeTv = view.findViewById(R.id.tv_sheet_peek_route);
        TextView routeVarTv = view.findViewById(R.id.tv_sheet_peek_routevar);
        TextView dateTv = view.findViewById(R.id.tv_sheet_peek_date);
        TextView distanceTv = view.findViewById(R.id.tv_peeksheet_distance);
        TextView timeTv = view.findViewById(R.id.tv_peeksheet_time);
        TextView paceTv = view.findViewById(R.id.tv_peeksheet_velocity);

        // set
        routeTv.setText(exercise.getRoute());
        routeVarTv.setText(exercise.getRouteVar());
        dateTv.setText(exercise.getDateTime().format(AppConsts.FORMATTER_CAPTION));
        distanceTv.setText(exercise.printDistance(true, a));
        timeTv.setText(exercise.printTime(false));
        paceTv.setText(exercise.printPace(false, a));

        // set text color
        // TODO: xml attribute does not work - why?
        int textColor = LayoutUtils.getColorInt(android.R.attr.textColorPrimary, a);
        routeTv.setTextColor(textColor);
        routeVarTv.setTextColor(textColor);
        dateTv.setTextColor(LayoutUtils.getColorInt(android.R.attr.textColorSecondary, a));
        distanceTv.setTextColor(textColor);
        timeTv.setTextColor(textColor);
        paceTv.setTextColor(textColor);

        // open full view
        view.setOnClickListener(view -> ExerciseDetailActivity.startActivity(a, exercise.getId()));
    }

    // interface

    public interface SheetListener {

        void onPeekSheetClick(int exerciseId);

    }

}
