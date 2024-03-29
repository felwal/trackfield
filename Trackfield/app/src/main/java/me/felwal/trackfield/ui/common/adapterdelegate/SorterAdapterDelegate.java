package me.felwal.trackfield.ui.common.adapterdelegate;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import me.felwal.trackfield.R;
import me.felwal.trackfield.ui.base.BaseAdapterDelegate;
import me.felwal.trackfield.ui.base.BaseListAdapter;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.model.RecyclerItem;
import me.felwal.trackfield.ui.common.model.SorterItem;

public class SorterAdapterDelegate extends
    BaseAdapterDelegate<SorterItem, RecyclerItem, SorterAdapterDelegate.SorterViewHolder> {

    private final BaseListAdapter adapter;

    //

    public SorterAdapterDelegate(Activity a, DelegateClickListener listener, BaseListAdapter adapter) {
        super(a, listener);
        this.adapter = adapter;
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof SorterItem;
    }

    @NonNull
    @Override
    public SorterViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new SorterViewHolder(inflater.inflate(R.layout.fw_item_sorter, parent, false));
    }

    @Override
    public void onBindViewHolder(SorterItem item, SorterViewHolder vh, @Nullable List<Object> payloads) {
        vh.titleTv.setText(item.getTitle());
        adapter.setSortMode(item.getMode());
    }

    // vh

    public class SorterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView titleTv;

        public SorterViewHolder(View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.fw_tv);
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
