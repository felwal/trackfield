package com.example.trackfield.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.trackfield.R;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.toolbox.L;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public abstract class FilterDialog extends BaseDialog {

    protected DialogListener listener;
    protected ArrayList<Integer> checkedTypes;

    // bundle
    private final static String BUNDLE_CHECKED_TYPES = "checkedTypes";

    ////

    protected static void bundle(FilterDialog dialog, ArrayList<Integer> checkedTypes) {
        Bundle bundle = new Bundle();
        bundle.putIntegerArrayList(BUNDLE_CHECKED_TYPES, checkedTypes);
        dialog.setArguments(bundle);
    }

    @NonNull
    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            checkedTypes = bundle.getIntegerArrayList(BUNDLE_CHECKED_TYPES);
        }

        return super.onCreateDialog(savedInstanceState);
    }
    @Override public void onAttach(Context context) {
        super.onAttach(context);

        try { listener = (DialogListener) context; }
        catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DialogListener");
        }
    }
    @Override protected AlertDialog buildDialog() {

        View dialogView = inflater.inflate(R.layout.dialog_filter_main, null);
        final ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroup_types);

        setChips(chipGroup);

        if (!message().equals("")) builder.setMessage(message());
        builder.setView(dialogView).setTitle(title())
                .setPositiveButton(positiveBtnTxtId(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
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

    protected void setChips(ChipGroup chipGroup) {

        for (int type = 0; type < Exercise.TYPES_PLURAL.length; type++){
            Chip chip;
            if (type < chipGroup.getChildCount()) chip = (Chip) chipGroup.getChildAt(type);
            else { chip = (Chip) inflater.inflate(R.layout.layout_chip, null); chipGroup.addView(chip); }
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

    @Override protected String title() {
        return getString(R.string.dialog_title_filter);
    }
    @Override protected String message() {
        return "";
    }
    @Override protected int positiveBtnTxtId() {
        return R.string.dialog_btn_filter;
    }

    public interface DialogListener {
        void onFilterDialogPositiveClick(ArrayList<Integer> checkedTypes, String tag);
    }

}
