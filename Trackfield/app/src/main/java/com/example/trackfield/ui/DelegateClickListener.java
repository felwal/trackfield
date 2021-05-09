package com.example.trackfield.ui;

import android.view.View;

public interface DelegateClickListener {

    void onDelegateClick(View view, int position);

    default void onDelegateLongClick(View view, int position) {
    };

}
