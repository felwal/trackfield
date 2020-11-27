package com.example.trackfield.activities.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.trackfield.R;

public class BoardingFragment extends Fragment {

    private PageViewModel pageViewModel;
    private static final String ARG_SECTION_NUMBER = "section_number";

    ////

    public static BoardingFragment newInstance(int index) {
        BoardingFragment fragment = new BoardingFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_boarding, container, false);

        final TextView textView = view.findViewById(R.id.textView_boardTitle);
        pageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return view;
    }

}