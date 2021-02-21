package com.example.trackfield.dialogs.instances;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;

import com.example.trackfield.R;
import com.example.trackfield.dialogs.BaseDialog;
import com.example.trackfield.toolbox.D;

@Deprecated public class WeekChartWeeks extends BaseDialog {

    public static final String TAG = "weekChartWeeks";

    @Override public String tag() {
        return null;
    }
    @Override protected String title() {
        return "";
    }
    @Override protected String message() {
        return "";
    }
    @Override protected int positiveBtnTxtId() {
        return 0;
    }

    @Override protected AlertDialog buildDialog() {

        final View dialogView = inflater.inflate(R.layout.dialog_decimal, null);
        final EditText et = dialogView.findViewById(R.id.editText_numberDecimalField);
        //et.setHint("Weeks");
        et.setText(D.weekAmount + "");

        builder.setView(dialogView).setTitle("Week chart weeks")
                .setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final int weeks = (int) (float) Float.valueOf(et.getText().toString());
                        D.weekAmount = weeks;
                        //D.calcWeekStats();
                        //a.recreate();
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
