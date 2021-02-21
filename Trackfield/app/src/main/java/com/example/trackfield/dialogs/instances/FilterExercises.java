package com.example.trackfield.dialogs.instances;

import androidx.fragment.app.FragmentManager;

import com.example.trackfield.dialogs.FilterDialog;

import java.util.ArrayList;

// filter
public class FilterExercises extends FilterDialog {

    public static FilterExercises newInstance(ArrayList<Integer> checkedTypes, FragmentManager fm) {
        FilterExercises instance = new FilterExercises();
        bundle(instance, checkedTypes);
        instance.show(fm, instance.tag());
        return instance;
    }
    @Override public String tag() {
        return "filterExercises";
    }

}
