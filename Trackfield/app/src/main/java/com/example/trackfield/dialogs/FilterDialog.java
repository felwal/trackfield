package com.example.trackfield.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.toolbox.L;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class FilterDialog extends BaseDialog {

    protected DialogListener listener;

    protected ArrayList<Integer> checkedTypes;

    // bundle
    private final static String BUNDLE_CHECKED_TYPES = "checkedTypes";

    private final static String TAG_DEFAULT = "filterDialog";

    ////

    private FilterDialog() {}

    public static FilterDialog newInstance(String title, ArrayList<Integer> checkedTypes, @StringRes int posBtnTxtId, String tag) {

        FilterDialog instance = new FilterDialog();
        Bundle bundle = putBundleBase(title, "", posBtnTxtId, tag);

        bundle.putIntegerArrayList(BUNDLE_CHECKED_TYPES, checkedTypes);

        instance.setArguments(bundle);

        return instance;
    }

    // on

    @Override public void onAttach(@NonNull Context context) {

        super.onAttach(context);

        try {
            listener = (DialogListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DialogListener");
        }

    }

    // extends

    @Override
    protected void unpackBundle() {

        Bundle bundle = unpackBundleBase(TAG_DEFAULT);

        if (bundle != null) {
            checkedTypes = bundle.getIntegerArrayList(BUNDLE_CHECKED_TYPES);
        }

    }

    @Override protected AlertDialog buildDialog() {

        View dialogView = inflater.inflate(R.layout.dialog_filter_main, null);
        final ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroup_types);

        if (!message.equals("")) builder.setMessage(message);
        setChips(chipGroup);

        builder.setView(dialogView).setTitle(title)
                .setPositiveButton(posBtnTxtId, (dialog, id) -> {
                    try {
                        listener.onFilterDialogPositiveClick(getCheckedTypes(chipGroup), tag);
                    }
                    catch (NumberFormatException e) {
                        L.toast("No input", a);
                    }
                })
                .setNegativeButton(negBtnTxtId, (dialog, id) -> getDialog().cancel());

        return builder.show();
    }

    // tools

    protected void setChips(ChipGroup chipGroup) {

        for (int type = 0; type < Exercise.TYPES_PLURAL.length; type++){
            Chip chip;
            if (type < chipGroup.getChildCount()) {
                chip = (Chip) chipGroup.getChildAt(type);
            }
            else {
                chip = (Chip) inflater.inflate(R.layout.layout_chip, null);
                chipGroup.addView(chip);
            }
            chip.setText(Exercise.TYPES_PLURAL[type]);
            if (checkedTypes.contains(type)) chip.setChecked(true);
        }

    }

    protected ArrayList<Integer> getCheckedTypes(ChipGroup chipGroup) {

        ArrayList<Integer> checkedTypes = new ArrayList<>();
        for (int type = 0; type < chipGroup.getChildCount(); type++){
            Chip chip = (Chip) chipGroup.getChildAt(type);
            if (chip.isChecked()) checkedTypes.add(type);
        }

        return checkedTypes;
    }

    // interface

    public interface DialogListener {
        void onFilterDialogPositiveClick(ArrayList<Integer> checkedTypes, String tag);
    }

}
