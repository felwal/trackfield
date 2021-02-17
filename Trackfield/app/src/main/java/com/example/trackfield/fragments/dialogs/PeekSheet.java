package com.example.trackfield.fragments.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;
import com.example.trackfield.activities.ViewActivity;
import com.example.trackfield.database.Helper;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.toolbox.C;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PeekSheet extends BottomSheetDialogFragment {

    private Activity a;
    private View view;
    private DismissListener listener;

    private Exercise exercise;

    // bundle
    public static final String BUNDLE_ID = "id";

    ////

    public static PeekSheet newInstance(int id, FragmentManager fm, DismissListener dismissListener) {
        PeekSheet instance = new PeekSheet();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_ID, id);
        instance.setArguments(bundle);
        instance.show(fm, instance.getTag());
        instance.listener = dismissListener;
        return instance;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        a = getActivity();

        // arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            int id = bundle.getInt(BUNDLE_ID, 0);
            exercise = Helper.getReader(a).getExercise(id);
        }

    }
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialogsheet_peek, container, false);

        setViews();

        return view;
    }

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

        // open full view
        view.setOnClickListener(view -> {
            ViewActivity.startActivity(a, exercise.get_id());
        });

    }

    @Override public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        listener.onPeekSheetDismiss(exercise.get_id());
    }

    public interface DismissListener {
        void onPeekSheetDismiss(int id);
    }

}
