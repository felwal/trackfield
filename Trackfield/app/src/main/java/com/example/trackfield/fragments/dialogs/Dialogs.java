package com.example.trackfield.fragments.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.trackfield.R;
import com.example.trackfield.objects.Exercise;
import com.example.trackfield.toolbox.Toolbox;
import com.example.trackfield.toolbox.Toolbox.C;
import com.example.trackfield.toolbox.Toolbox.D;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class Dialogs {

    // base

    public static abstract class Base extends DialogFragment {

        protected Activity a;
        protected AlertDialog.Builder builder;
        protected LayoutInflater inflater;

        public final static int NO_TEXT = -1;

        ////

        @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

            a = getActivity();
            builder = new AlertDialog.Builder(a);
            inflater = requireActivity().getLayoutInflater();

            return createDialog(buildDialog());
        }

        protected abstract String tag();
        protected abstract String title();
        protected abstract String message();
        protected abstract int positiveBtnTxtId();
        protected int negativeBtnTxtId() {
            return R.string.dialog_btn_cancel;
        }

        protected abstract AlertDialog buildDialog();
        protected AlertDialog createDialog(AlertDialog dialog) {

            // title
            int titleId = getResources().getIdentifier( "alertTitle", "id", "android" );
            if (titleId > 0) {
                TextView titleTv = dialog.findViewById(titleId);
                titleTv.setTextAppearance(R.style.DialogTitle);
            }

            // message
            TextView messageTv = dialog.findViewById(android.R.id.message);
            messageTv.setTextAppearance(title().equals("") ? R.style.DialogMessageLone : R.style.DialogMessage);

            // bg
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_dialog_bg);
            //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            //dialog.getWindow().getDecorView().getBackground().set(R.style.DialogCustom);

            return dialog;
        }

    }
    public static abstract class BaseWithListener extends Base {

        protected DialogListener listener;

        ////

        @Override public void onAttach(Context context) {
            super.onAttach(context);

            try { listener = (DialogListener) context; }
            catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement DialogListener");
            }
        }

        public interface DialogListener {
            void doRecreate();
        }

    }

    public static abstract class BinaryDialog extends Base {

        protected DialogListener listener;
        //private String title, message;
        //private int positiveButtonTextId;
        //private String tag;

        // extras
        //private final static String BUNDLE_TITLE = "title";
        //private final static String BUNDLE_MESSAGE = "message";
        //private final static String BUNDLE_POSITIVE_BUTTON = "positiveButton";
        private final static String BUNDLE_TAG = "tag";

        ////

        protected static void bundle(BinaryDialog dialog, FragmentManager fm) {
            //BinaryDialog instance = new BinaryDialog();
            Bundle bundle = new Bundle();
            //bundle.putString(BUNDLE_TITLE, title);
            //bundle.putString(BUNDLE_MESSAGE, message);
            //bundle.putInt(BUNDLE_POSITIVE_BUTTON, positiveButtonTextId);
            bundle.putString(BUNDLE_TAG, dialog.tag());
            dialog.setArguments(bundle);
            dialog.show(fm, dialog.tag());
        }

        @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

            Bundle bundle = getArguments();
            if (bundle != null) {
                //title = bundle.getString(BUNDLE_TITLE, "");
                //message = bundle.getString(BUNDLE_MESSAGE, "");
                //positiveButtonTextId = bundle.getInt(BUNDLE_POSITIVE_BUTTON);
                //tag = bundle.getString(BUNDLE_TAG);
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

            if (!message().equals("")) builder.setMessage(message());
            builder.setTitle(title())
                    .setPositiveButton(positiveBtnTxtId(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            listener.onBinaryDialogPositiveClick(tag());
                        }
                    })
                    .setNegativeButton(negativeBtnTxtId(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getDialog().cancel();
                        }
                    });

            return builder.show();
        }

        public interface DialogListener {
            void onBinaryDialogPositiveClick(String tag);
        }

    }
    public static abstract class DecimalDialog extends Base {

        protected DialogListener listener;
        private String title, message, hint;
        private float text;
        private int positiveButtonTextId;
        private String tag;

        // extras
        private final static String BUNDLE_TITLE = "title";
        private final static String BUNDLE_MESSAGE = "message";
        private final static String BUNDLE_HINT = "hint";
        private final static String BUNDLE_TEXT = "text";
        private final static String BUNDLE_POSITIVE_BUTTON = "positiveButton";
        private final static String BUNDLE_TAG = "tag";

        ////

        protected static void bundle(DecimalDialog dialog, float text) {
            //DecimalDialog instance = new DecimalDialog();
            Bundle bundle = new Bundle();
            //bundle.putString(BUNDLE_TITLE, title); new Bundle();
            //bundle.putString(BUNDLE_MESSAGE, message);
            //bundle.putString(BUNDLE_HINT, hint);
            bundle.putFloat(BUNDLE_TEXT, text);
            //bundle.putInt(BUNDLE_POSITIVE_BUTTON, positiveButtonTextId);
            //bundle.putString(BUNDLE_TAG, tag);
            dialog.setArguments(bundle);
            //dialog.show(fm, tag);
            //return instance;
        }

        protected abstract String hint();

        @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

            Bundle bundle = getArguments();
            if (bundle != null) {
                //title = bundle.getString(BUNDLE_TITLE, "");
                //message = bundle.getString(BUNDLE_MESSAGE, "");
                //hint = bundle.getString(BUNDLE_HINT, "");
                text = bundle.getFloat(BUNDLE_TEXT, 0);
                //positiveButtonTextId = bundle.getInt(BUNDLE_POSITIVE_BUTTON);
                //tag = bundle.getString(BUNDLE_TAG);
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

            final View dialogView = inflater.inflate(R.layout.dialog_decimal, null);
            final EditText et = dialogView.findViewById(R.id.editText_numberDecimalField);
            et.setHint(hint());
            if (text != NO_TEXT) et.setText(Float.toString(text));

            if (!message().equals("")) builder.setMessage(message());
            builder.setView(dialogView).setTitle(title())
                    .setPositiveButton(positiveBtnTxtId(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                final float input = Float.parseFloat(et.getText().toString());
                                listener.onDecimalDialogPositiveClick(input, tag());
                            }
                            catch (NumberFormatException e) {
                                Toolbox.L.toast("No input", a);
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

        public interface DialogListener {
            void onDecimalDialogPositiveClick(float input, String tag);
        }

    }
    public static abstract class TextDialog extends Base {

        protected DialogListener listener;
        private String text;

        // bundle
        private final static String BUNDLE_TEXT = "text";

        ////

        protected static void bundle(TextDialog dialog, String text) {
            //TextDialog instance = new TextDialog();
            Bundle bundle = new Bundle();
            //bundle.putString(BUNDLE_TITLE, title);
            //bundle.putString(BUNDLE_MESSAGE, message);
            //bundle.putString(BUNDLE_HINT, hint);
            bundle.putString(BUNDLE_TEXT, text);
            //bundle.putInt(BUNDLE_POSITIVE_BUTTON, positiveButtonTextId);
            //bundle.putString(BUNDLE_TAG, tag);
            dialog.setArguments(bundle);
            //instance.show(fm, tag);
            //return instance;
        }

        protected abstract String hint();

        @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

            Bundle bundle = getArguments();
            if (bundle != null) {
                //title = bundle.getString(BUNDLE_TITLE, "");
                //message = bundle.getString(BUNDLE_MESSAGE, "");
                //hint = bundle.getString(BUNDLE_HINT, "");
                text = bundle.getString(BUNDLE_TEXT, "");
                //positiveButtonTextId = bundle.getInt(BUNDLE_POSITIVE_BUTTON);
                //tag = bundle.getString(BUNDLE_TAG);
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

            final View dialogView = inflater.inflate(R.layout.dialog_text, null);
            final EditText et = dialogView.findViewById(R.id.editText_textField);
            et.setText(text);
            et.setHint(hint());

            if (!message().equals("")) builder.setMessage(message());
            builder.setView(dialogView).setTitle(title())
                    .setPositiveButton(positiveBtnTxtId(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final String input = et.getText().toString();
                            listener.onTextDialogPositiveClick(input, tag());
                        }
                    })
                    .setNegativeButton(negativeBtnTxtId(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getDialog().cancel();
                        }
                    });

            return builder.show();
        }

        public interface DialogListener {
            void onTextDialogPositiveClick(String input, String tag);
        }

    }
    public static abstract class TimeDialog extends Base {

        protected DialogListener listener;
        private int text1, text2;

        // extras
        private final static String BUNDLE_TEXT1 = "text1";
        private final static String BUNDLE_TEXT2 = "text2";

        ////

        protected static void bundle(TimeDialog dialog, int text1, int text2) {
            Bundle bundle = new Bundle();
            bundle.putInt(BUNDLE_TEXT1, text1);
            bundle.putInt(BUNDLE_TEXT2, text2);
            dialog.setArguments(bundle);
        }

        protected abstract String hint1();
        protected abstract String hint2();
        protected abstract int neutralBtnTxtId();

        @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

            Bundle bundle = getArguments();
            if (bundle != null) {
                //title = bundle.getString(BUNDLE_TITLE, "");
                //message = bundle.getString(BUNDLE_MESSAGE, "");
                //hint = bundle.getString(BUNDLE_HINT, "");
                text1 = bundle.getInt(BUNDLE_TEXT1, NO_TEXT);
                text2 = bundle.getInt(BUNDLE_TEXT2, NO_TEXT);
                //positiveButtonTextId = bundle.getInt(BUNDLE_POSITIVE_BUTTON);
                //tag = bundle.getString(BUNDLE_TAG);
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

            final View dialogView = inflater.inflate(R.layout.dialog_time, null);
            final EditText et1 = dialogView.findViewById(R.id.editText_numberField1);
            final EditText et2 = dialogView.findViewById(R.id.editText_numberField2);
            final ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroup_goal);
            et1.setHint(hint1());
            et2.setHint(hint2());
            if (text1 != NO_TEXT) et1.setText(Integer.toString(text1));
            if (text2 != NO_TEXT) et2.setText(Integer.toString(text2));

            // require selection
            setChipGroup(chipGroup);
            chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
                @Override public void onCheckedChanged(ChipGroup group, int checkedId) {
                    setChipGroup(group);
                }
            });

            if (!message().equals("")) builder.setMessage(message());
            builder.setView(dialogView).setTitle(title())
                    .setPositiveButton(positiveBtnTxtId(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                final int input1 = Integer.parseInt(et1.getText().toString());
                                final int input2 = Integer.parseInt(et2.getText().toString());
                                listener.onTimeDialogPositiveClick(input1, input2, tag());
                            }
                            catch (NumberFormatException e) {
                                Toolbox.L.toast("No input", a);
                            }
                        }
                    })
                    .setNegativeButton(negativeBtnTxtId(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getDialog().cancel();
                        }
                    })
                    .setNeutralButton(neutralBtnTxtId(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            listener.onTimeDialogNegativeClick(tag());
                        }
                    });

            AlertDialog dialog = builder.show();
            return dialog;
        }

        private void setChipGroup(ChipGroup chipGroup) {
            Chip checkedChip = (Chip) chipGroup.getChildAt(chipGroup.getCheckedChipId());
            if (checkedChip != null) {
                for (int i = 0; i < chipGroup.getChildCount(); ++i) {
                    chipGroup.getChildAt(i).setClickable(true);
                }
                checkedChip.setClickable(false);
            }
        }

        public interface DialogListener {
            void onTimeDialogPositiveClick(int input1, int input2, String tag);
            void onTimeDialogNegativeClick(String tag);
            //void onTimeDialogNeutralClick(String tag);
        }

    }
    public static abstract class FilterDialog extends Base {

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

        @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

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

            View dialogView = inflater.inflate(R.layout.dialog_filter, null);
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
                                Toolbox.L.toast("No input", a);
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

    // instances

    // binary
    public static class DeleteExercise extends BinaryDialog {

        public static DeleteExercise newInstance(FragmentManager fm) {
            DeleteExercise instance = new DeleteExercise();
            //bundle(instance, fm);
            instance.show(fm, instance.tag());
            return instance;
        }

        @Override protected String tag() {
            return "deleteExercise";
        }
        @Override protected String title() {
            return "";
        }
        @Override protected String message() {
            return getString(R.string.dialog_title_delete_exercise);
        }
        @Override protected int positiveBtnTxtId() {
            return R.string.dialog_btn_delete;
        }

    }
    public static class DeleteDistance extends BinaryDialog {

        public static DeleteDistance newInstance(FragmentManager fm) {
            DeleteDistance instance = new DeleteDistance();
            //bundle(instance, fm);
            instance.show(fm, instance.tag());
            return instance;
        }

        @Override protected String tag() {
            return "deleteDistance";
        }
        @Override protected String title() {
            return getString(R.string.dialog_title_delete_distance);
        }
        @Override protected String message() {
            return getString(R.string.dialog_message_delete_distance);
        }
        @Override protected int positiveBtnTxtId() {
            return R.string.dialog_btn_delete;
        }

    }
    public static class FinishTracking extends BinaryDialog {

        public static FinishTracking newInstance(FragmentManager fm) {
            FinishTracking instance = new FinishTracking();
            //bundle(instance, fm);
            instance.show(fm, instance.tag());
            return instance;
        }

        @Override protected String tag() {
            return "finishTracking";
        }
        @Override protected String title() {
            return getString(R.string.dialog_title_finish_recording);
        }
        @Override protected String message() {
            return "";
        }
        @Override protected int positiveBtnTxtId() {
            return R.string.dialog_btn_finish;
        }

    }

    // decimal
    public static class AddDistance extends DecimalDialog {

        public static AddDistance newInstance(float text, FragmentManager fm) {
            AddDistance instance = new AddDistance();
            bundle(instance, text);
            instance.show(fm, instance.tag());
            return instance;
        }

        @Override protected String tag() {
            return "addExercise";
        }
        @Override protected String title() {
            return getString(R.string.dialog_title_add_distance);
        }
        @Override protected String message() {
            return "";
        }
        @Override protected int positiveBtnTxtId() {
            return R.string.dialog_btn_add;
        }
        @Override protected String hint() {
            return "";
        }

    }
    public static class EditMass extends DecimalDialog {

        public static EditMass newInstance(float text, FragmentManager fm) {
            EditMass instance = new EditMass();
            bundle(instance, text);
            instance.show(fm, instance.tag());
            return instance;
        }

        @Override protected String tag() {
            return "editMass";
        }
        @Override protected String title() {
            return getString(R.string.dialog_title_mass);
        }
        @Override protected String message() {
            return "";
        }
        @Override protected int positiveBtnTxtId() {
            return R.string.dialog_btn_ok;
        }
        @Override protected String hint() {
            return "";
        }

    }

    // text
    public static class RenameRoute extends TextDialog {

        public static RenameRoute newInstance(String text, FragmentManager fm) {
            RenameRoute instance = new RenameRoute();
            bundle(instance, text);
            instance.show(fm, instance.tag());
            return instance;
        }

        @Override protected String tag() {
            return "renameRoute";
        }
        @Override protected String title() {
            return getString(R.string.dialog_title_rename_route);
        }
        @Override protected String message() {
            return "";
        }
        @Override protected String hint() {
            return "";
        }
        @Override protected int positiveBtnTxtId() {
            return R.string.dialog_btn_rename;
        }

    }
    public static class RenameInterval extends TextDialog {

        public static RenameInterval newInstance(String text, FragmentManager fm) {
            RenameInterval instance = new RenameInterval();
            bundle(instance, text);
            instance.show(fm, instance.tag());
            return instance;
        }

        @Override protected String tag() {
            return "renameInterval";
        }
        @Override protected String title() {
            return getString(R.string.dialog_title_rename_interval);
        }
        @Override protected String message() {
            return "";
        }
        @Override protected String hint() {
            return "";
        }
        @Override protected int positiveBtnTxtId() {
            return R.string.dialog_btn_rename;
        }

    }

    // time
    public static class GoalDistance extends TimeDialog {

        public static GoalDistance newInstance(int text1, int text2, FragmentManager fm) {
            GoalDistance instance = new GoalDistance();
            bundle(instance, text1, text2);
            instance.show(fm, instance.tag());
            return instance;
        }

        @Override protected String tag() {
            return "setGoalDistance";
        }
        @Override protected String title() {
            return getString(R.string.dialog_title_set_goal);
        }
        @Override protected String message() {
            return "";
        }
        @Override protected String hint1() {
            return "";
        }
        @Override protected String hint2() {
            return "";
        }
        @Override protected int positiveBtnTxtId() {
            return R.string.dialog_btn_set;
        }
        @Override protected int neutralBtnTxtId() {
            return R.string.dialog_btn_delete;
        }

    }
    public static class GoalRoute extends TimeDialog {

        public static GoalRoute newInstance(int text1, int text2, FragmentManager fm) {
            GoalRoute instance = new GoalRoute();
            bundle(instance, text1, text2);
            instance.show(fm, instance.tag());
            return instance;
        }

        @Override protected String tag() {
            return "setGoalRoute";
        }
        @Override protected String title() {
            return getString(R.string.dialog_title_set_goal);
        }
        @Override protected String message() {
            return "";
        }
        @Override protected String hint1() {
            return "";
        }
        @Override protected String hint2() {
            return "";
        }
        @Override protected int positiveBtnTxtId() {
            return R.string.dialog_btn_set;
        }
        @Override protected int neutralBtnTxtId() {
            return R.string.dialog_btn_delete;
        }

    }

    // filter
    public static class FilterExercises extends FilterDialog {

        public static FilterExercises newInstance(ArrayList<Integer> checkedTypes, FragmentManager fm) {
            FilterExercises instance = new FilterExercises();
            bundle(instance, checkedTypes);
            instance.show(fm, instance.tag());
            return instance;
        }
        @Override protected String tag() {
            return "filterExercises";
        }

    }
    public static class FilterRoute extends FilterDialog {

        public static FilterRoute newInstance(ArrayList<Integer> checkedTypes, FragmentManager fm) {
            FilterRoute instance = new FilterRoute();
            bundle(instance, checkedTypes);
            instance.show(fm, instance.tag());
            return instance;
        }
        @Override protected String tag() {
            return "filterRouteExercises";
        }

    }
    public static class FilterDistance extends FilterDialog {

        public static FilterDistance newInstance(ArrayList<Integer> checkedTypes, FragmentManager fm) {
            FilterDistance instance = new FilterDistance();
            bundle(instance, checkedTypes);
            instance.show(fm, instance.tag());
            return instance;
        }
        @Override protected String tag() {
            return "filterDistanceExercises";
        }

        @Override protected AlertDialog buildDialog() {

            View dialogView = inflater.inflate(R.layout.dialog_filter_distance, null);
            final Switch longerSw = dialogView.findViewById(R.id.switch_includeLonger);
            final ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroup_types);

            longerSw.setChecked(D.includeLonger);
            setChips(chipGroup);

            if (!message().equals("")) builder.setMessage(message());
            builder.setView(dialogView).setTitle(title())
                    .setPositiveButton(positiveBtnTxtId(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                D.includeLonger = longerSw.isChecked();
                                listener.onFilterDialogPositiveClick(getCheckedTypes(chipGroup), tag());
                            }
                            catch (NumberFormatException e) {
                                Toolbox.L.toast("No input", a);
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

    ////

    // settings
    public static class WeekChartWeeks extends Base {

        public static final String TAG = "weekChartWeeks";

        @Override protected String tag() {
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
                            D.calcWeekStats();
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
    public static class Theme extends BaseWithListener {

        public static final String TAG = "theme";

        @Override protected String tag() {
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

            if (D.theme) { lightRadio.setChecked(true); }
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
                    if (D.theme) {
                        D.theme = false;
                        //a.recreate();
                        listener.doRecreate();
                    }
                }
            });

            lightRadio.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    getDialog().cancel();
                    if (!D.theme) {
                        D.theme = true;
                        //a.recreate();
                        listener.doRecreate();
                    }
                }
            });

            return builder.show();
        }

    }
    public static class Color extends BaseWithListener {

        public static final String TAG = "color";

        @Override protected String tag() {
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

            if (D.color == C.COLOR_GREEN) { greenRadio.setChecked(true); }
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
                    if (D.color != C.COLOR_GREEN) {
                        D.color = C.COLOR_GREEN;
                        //a.recreate();
                        listener.doRecreate();
                    }
                }
            });

            monoRadio.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    getDialog().cancel();
                    if (D.color != C.COLOR_MONO) {
                        D.color = C.COLOR_MONO;
                        //a.recreate();
                        listener.doRecreate();
                    }
                }
            });

            return builder.show();
        }

    }

}
