package com.example.trackfield.dialogs.instances;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.RadioButton;

import com.example.trackfield.R;
import com.example.trackfield.dialogs.BaseDialogWithListener;
import com.example.trackfield.toolbox.Prefs;

@Deprecated public class Color extends BaseDialogWithListener {

    public static final String TAG = "color";

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

        final View element = inflater.inflate(R.layout.dialog_radio_color, null);
        final RadioButton greenRadio = element.findViewById(R.id.radioButton_green);
        final RadioButton monoRadio = element.findViewById(R.id.radioButton_mono);

        if (Prefs.isColorGreen()) { greenRadio.setChecked(true); }
        else { monoRadio.setChecked(true); }

        builder.setView(element).setTitle("Color")
                .setNegativeButton(negativeBtnTxtId(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                });

        greenRadio.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                getDialog().cancel();
                if (!Prefs.isColorGreen()) {
                    Prefs.setColorGreen();
                    //a.recreate();
                    listener.doRecreate();
                }
            }
        });

        monoRadio.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                getDialog().cancel();
                if (!Prefs.isColorMono()) {
                    Prefs.setColorMono();
                    //a.recreate();
                    listener.doRecreate();
                }
            }
        });

        return builder.show();
    }

}
