package com.example.trackfield.dialogs.sheets;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.trackfield.R;
import com.example.trackfield.activities.ViewActivity;
import com.example.trackfield.database.Reader;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.toolbox.C;
import com.example.trackfield.toolbox.L;

public class PeekSheet extends BaseSheet {

    private DismissListener listener;

    // arguments
    private Exercise exercise;

    private static final String TAG = "peekSheet";

    // bundle
    public static final String BUNDLE_ID = "id";

    ////

    public static PeekSheet newInstance(int id) {

        PeekSheet instance = new PeekSheet();
        Bundle bundle = new Bundle();

        bundle.putInt(BUNDLE_ID, id);

        instance.setArguments(bundle);

        return instance;
    }

    // extends DialogFragment

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialogsheet_peek, container, false);

        setViews();

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (DismissListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DialogListener");
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        listener.onPeekSheetDismiss(exercise.get_id());
    }

    // extends BaseSheet

    @Override
    protected void unpackBundle() {

        Bundle bundle = getArguments();

        if (bundle != null) {
            int id = bundle.getInt(BUNDLE_ID, 0);
            exercise = Reader.get(a).getExercise(id);
        }

        tag = TAG;

    }

    // set

    private void setViews() {

        // find
        TextView routeTv = view.findViewById(R.id.textView_route);
        TextView routeVarTv = view.findViewById(R.id.textView_routeVar);
        TextView dateTv = view.findViewById(R.id.textView_date);
        TextView distanceTv = view.findViewById(R.id.textView_distance);
        TextView timeTv = view.findViewById(R.id.textView_time);
        TextView paceTv = view.findViewById(R.id.textView_velocity);

        // set
        routeTv.setText(exercise.getRoute());
        routeVarTv.setText(exercise.getRouteVar());
        dateTv.setText(exercise.getDateTime().format(C.FORMATTER_CAPTION));
        distanceTv.setText(exercise.printDistance(true));
        timeTv.setText(exercise.printTime(false));
        paceTv.setText(exercise.printPace(false));

        // set text color - TODO: xml attribute does not work - why?
        int textColor = L.getColorInt(android.R.attr.textColorPrimary, a);
        routeTv.setTextColor(textColor);
        routeVarTv.setTextColor(textColor);
        dateTv.setTextColor(textColor);
        distanceTv.setTextColor(textColor);
        timeTv.setTextColor(textColor);
        paceTv.setTextColor(textColor);

        // open full view
        view.setOnClickListener(view -> ViewActivity.startActivity(a, exercise.get_id()));
    }

    // interface

    public interface DismissListener {
        void onPeekSheetDismiss(int id);
    }

}
