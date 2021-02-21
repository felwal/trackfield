package com.example.trackfield.dialogs.instances;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Switch;

import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;
import com.example.trackfield.dialogs.FilterDialog;
import com.example.trackfield.toolbox.L;
import com.example.trackfield.toolbox.Prefs;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class FilterDistance extends FilterDialog {

    public static FilterDistance newInstance(ArrayList<Integer> checkedTypes, FragmentManager fm) {
        FilterDistance instance = new FilterDistance();
        bundle(instance, checkedTypes);
        instance.show(fm, instance.tag());
        return instance;
    }
    @Override public String tag() {
        return "filterDistanceExercises";
    }

    @Override protected AlertDialog buildDialog() {

        View dialogView = inflater.inflate(R.layout.dialog_filter_distance, null);
        final Switch longerSw = dialogView.findViewById(R.id.switch_includeLonger);
        final ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroup_types);

        longerSw.setChecked(Prefs.includeLonger());
        setChips(chipGroup);

        if (!message().equals("")) builder.setMessage(message());
        builder.setView(dialogView).setTitle(title())
                .setPositiveButton(positiveBtnTxtId(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            Prefs.includeLonger(longerSw.isChecked());
                            listener.onFilterDialogPositiveClick(getCheckedTypes(chipGroup), tag());
                        }
                        catch (NumberFormatException e) {
                            L.toast("No input", a);
                        }
                    }
                })
                .setNegativeButton(negativeBtnTxtId(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                });

        return builder.show();
    }

}
