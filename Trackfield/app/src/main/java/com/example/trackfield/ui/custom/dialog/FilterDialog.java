package com.example.trackfield.ui.custom.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.example.trackfield.R;
import com.example.trackfield.data.db.DbReader;
import com.example.trackfield.utils.LayoutUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class FilterDialog extends BaseDialog {

    // bundle keys
    private static final String BUNDLE_CHECKED_TYPES = "checkedTypes";

    // dialog tags
    private static final String TAG_DEFAULT = "filterDialog";

    private DialogListener listener;

    // arguments
    private ArrayList<String> checkedTypes;

    //

    public static FilterDialog newInstance(@StringRes int titleRes, ArrayList<String> checkedTypes,
        @StringRes int posBtnTxtRes, String tag) {

        FilterDialog instance = new FilterDialog();
        Bundle bundle = putBundleBase(titleRes, NO_RES, posBtnTxtRes, tag);

        bundle.putStringArrayList(BUNDLE_CHECKED_TYPES, checkedTypes);

        instance.setArguments(bundle);
        return instance;
    }

    // on

    @Override
    public void onAttach(@NonNull Context context) {
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
            checkedTypes = bundle.getStringArrayList(BUNDLE_CHECKED_TYPES);
        }
    }

    @Override
    protected AlertDialog buildDialog() {
        ArrayList<String> types = DbReader.get(a).getTypes();

        // set empty message if no types to filter by
        if (types.size() == 0) {
            builder.setMessage(R.string.dialgo_msg_filter_empty);
            builder.setTitle(title)
                .setNegativeButton(negBtnTxtRes, (dialog, id) -> getDialog().cancel());
            return builder.show();
        }

        View dialogView = inflater.inflate(R.layout.dialog_filter_main, null);
        final ChipGroup chipGroup = dialogView.findViewById(R.id.cg_dialog_filter_types);

        // set chips
        for (int i = 0; i < types.size(); i++) {
            Chip chip;
            String type = types.get(i);

            chip = (Chip) inflater.inflate(R.layout.component_chip, chipGroup, false);
            chipGroup.addView(chip);
            chip.setText(type);

            if (checkedTypes.contains(type)) chip.setChecked(true);
        }

        builder.setView(dialogView).setTitle(title)
            .setPositiveButton(posBtnTxtRes, (dialog, id) -> {
                try {
                    listener.onFilterDialogPositiveClick(getCheckedTypes(chipGroup), tag);
                }
                catch (NumberFormatException e) {
                    LayoutUtils.toast(R.string.toast_err_no_input, a);
                }
            })
            .setNegativeButton(negBtnTxtRes, (dialog, id) -> getDialog().cancel());

        return builder.show();
    }

    // tools

    protected void setChips(ChipGroup chipGroup) {

    }

    @NonNull
    protected ArrayList<String> getCheckedTypes(ChipGroup chipGroup) {
        ArrayList<String> checkedTypes = new ArrayList<>();

        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) checkedTypes.add(chip.getText().toString());
        }

        return checkedTypes;
    }

    // interface

    public interface DialogListener {

        void onFilterDialogPositiveClick(@NonNull ArrayList<String> checkedTypes, String tag);

    }

}
