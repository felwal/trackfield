package com.example.trackfield.ui.base;

import android.view.View;

public interface DelegateClickListener {

    void onDelegateClick(View view, int position);

    default void onDelegateLongClick(View view, int position) {
    };

}
