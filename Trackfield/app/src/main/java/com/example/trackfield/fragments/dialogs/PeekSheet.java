package com.example.trackfield.fragments.dialogs;

import android.app.Activity;
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
import com.example.trackfield.toolbox.Toolbox;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PeekSheet extends BottomSheetDialogFragment {

    private Activity a;
    private View view;

    private Exercise exersice;

    // bundle
    public static final String BUNDLE_ID = "id";

    ////

    public static PeekSheet newInstance(int id, FragmentManager fm) {
        PeekSheet instance = new PeekSheet();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_ID, id);
        instance.setArguments(bundle);
        instance.show(fm, instance.getTag());
        //fm.executePendingTransactions();
        return instance;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        a = getActivity();

        // arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            int id = bundle.getInt(BUNDLE_ID, 0);
            exersice = Helper.getReader(a).getExercise(id);
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
        routeTv.setText(exersice.getRoute());
        routeVarTv.setText(exersice.getRouteVar());
        dateTv.setText(exersice.getDateTime().format(Toolbox.C.FORMATTER_CAPTION));
        distanceTv.setText(exersice.printDistance(true));
        timeTv.setText(exersice.printTime(false));
        paceTv.setText(exersice.printPace(false));

        // open full view
        view.setOnClickListener(view -> {
            ViewActivity.startActivity(a, exersice.get_id());
        });

    }

}
