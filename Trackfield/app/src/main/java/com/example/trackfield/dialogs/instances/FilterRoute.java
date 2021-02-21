package com.example.trackfield.dialogs.instances;

import androidx.fragment.app.FragmentManager;

import com.example.trackfield.dialogs.FilterDialog;

import java.util.ArrayList;

public class FilterRoute extends FilterDialog {

    public static FilterRoute newInstance(ArrayList<Integer> checkedTypes, FragmentManager fm) {
        FilterRoute instance = new FilterRoute();
        bundle(instance, checkedTypes);
        instance.show(fm, instance.tag());
        return instance;
    }
    @Override public String tag() {
        return "filterRouteExercises";
    }

}
