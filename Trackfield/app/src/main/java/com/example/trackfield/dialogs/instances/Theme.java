package com.example.trackfield.dialogs.instances;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.RadioButton;

import com.example.trackfield.R;
import com.example.trackfield.dialogs.BaseDialogWithListener;
import com.example.trackfield.toolbox.Prefs;

@Deprecated public class Theme extends BaseDialogWithListener {

    public static final String TAG = "theme";

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

        final View element = inflater.inflate(R.layout.dialog_radio_theme, null);
        final RadioButton darkRadio = element.findViewById(R.id.radioButton_dark);
        final RadioButton lightRadio = element.findViewById(R.id.radioButton_light);
        final RadioButton batterySaverRadio = element.findViewById(R.id.radioButton_batterySaver);

        if (Prefs.isThemeLight()) { lightRadio.setChecked(true); }
        else { darkRadio.setChecked(true); }

        builder.setView(element).setTitle("Theme")
                .setNegativeButton(negativeBtnTxtId(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                });

        darkRadio.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                getDialog().cancel();
                if (Prefs.isThemeLight()) {
                    Prefs.setTheme(false);
                    //a.recreate();
                    listener.doRecreate();
                }
            }
        });

        lightRadio.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                getDialog().cancel();
                if (!Prefs.isThemeLight()) {
                    Prefs.setTheme(true);
                    //a.recreate();
                    listener.doRecreate();
                }
            }
        });

        return builder.show();
    }

}
