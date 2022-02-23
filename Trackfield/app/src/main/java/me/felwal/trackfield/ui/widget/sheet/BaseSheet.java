package me.felwal.trackfield.ui.widget.sheet;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public abstract class BaseSheet extends BottomSheetDialogFragment {

    protected Activity a;
    protected View view;
    protected LayoutInflater inflater;

    // arguments
    protected String tag;

    // extends DialogFragment

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        a = getActivity();
        inflater = requireActivity().getLayoutInflater();

        unpackBundle();
    }

    // tools

    public void show(FragmentManager fm) {
        super.show(fm, tag);
    }

    // abstract

    protected abstract void unpackBundle();

}
