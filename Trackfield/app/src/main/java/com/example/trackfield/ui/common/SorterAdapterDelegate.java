package com.example.trackfield.ui.common;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.ui.base.BaseAdapter;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.base.BaseAdapterDelegate;
import com.example.trackfield.ui.common.model.RecyclerItem;
import com.example.trackfield.ui.common.model.Sorter;

import java.util.List;

public class SorterAdapterDelegate extends
    BaseAdapterDelegate<Sorter, RecyclerItem, SorterAdapterDelegate.SorterViewHolder> {

    private BaseAdapter adapter;

    //

    public SorterAdapterDelegate(Activity activity, DelegateClickListener listener, BaseAdapter adapter) {
        super(activity, listener);
        this.adapter = adapter;
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Sorter;
    }

    @NonNull
    @Override
    public SorterViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new SorterViewHolder(inflater.inflate(R.layout.item_sorter, parent, false));
    }

    @Override
    public void onBindViewHolder(Sorter item, SorterViewHolder vh, @Nullable List<Object> payloads) {
        vh.title.setText(item.getTitle());
        adapter.setSortMode(item.getSortMode());
    }

    // vh

    public class SorterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ConstraintLayout button;
        public TextView title;

        public SorterViewHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.constraintLayout_sort);
            title = itemView.findViewById(R.id.textView_sort);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                listener.onDelegateClick(view, getAdapterPosition());
            }
        }

    }

}
