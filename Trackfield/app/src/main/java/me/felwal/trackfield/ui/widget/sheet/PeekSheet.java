package me.felwal.trackfield.ui.widget.sheet;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.felwal.android.util.ResourcesKt;
import me.felwal.trackfield.R;
import me.felwal.trackfield.data.db.DbReader;
import me.felwal.trackfield.data.db.model.Exercise;
import me.felwal.trackfield.ui.exercisedetail.ExerciseDetailActivity;
import me.felwal.trackfield.utils.AppConsts;

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
        TextView routeTv = view.findViewById(R.id.tv_peeksheet_route);
        TextView routeVarTv = view.findViewById(R.id.tv_peeksheet_routevar);
        TextView dateTv = view.findViewById(R.id.tv_peeksheet_date);
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
        int textColor = ResourcesKt.getColorByAttr(a, android.R.attr.textColorPrimary);
        routeTv.setTextColor(textColor);
        routeVarTv.setTextColor(textColor);
        dateTv.setTextColor(ResourcesKt.getColorByAttr(a, android.R.attr.textColorSecondary));
        distanceTv.setTextColor(textColor);
        timeTv.setTextColor(textColor);
        paceTv.setTextColor(textColor);

        // open exercisedetail
        //ConstraintLayout headerCl = view.findViewById(R.id.cl_peeksheet_header);
        view.setOnClickListener(view -> ExerciseDetailActivity.startActivity(a, exercise.getId()));
    }

    // interface

    public interface SheetListener {

        void onPeekSheetClick(int exerciseId);

    }

}
