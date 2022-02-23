package com.felwal.trackfield.ui.common;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.felwal.trackfield.R;
import com.felwal.trackfield.ui.base.BaseAdapterDelegate;
import com.felwal.trackfield.ui.base.DelegateClickListener;
import com.felwal.trackfield.ui.common.model.Header;
import com.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class HeaderSmallAdapterDelegate extends
    BaseAdapterDelegate<Header, RecyclerItem, HeaderSmallAdapterDelegate.HeaderTinyViewHolder> {

    public HeaderSmallAdapterDelegate(Activity a, DelegateClickListener listener) {
        super(a, listener);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Header && ((Header) item).isType(Header.Type.WEEK, Header.Type.GROUP);
    }

    @NonNull
    @Override
    public HeaderTinyViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new HeaderTinyViewHolder(inflater.inflate(R.layout.item_recycler_header_tiny, parent, false));
    }

    @Override
    public void onBindViewHolder(Header item, @NonNull HeaderTinyViewHolder vh, @Nullable List<Object> payloads) {
        vh.primaryTv.setText(item.getTitle());
        vh.secondaryTv.setText(item.printValues());
    }

    // vh

    class HeaderTinyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnLongClickListener {

        public final TextView primaryTv;
        public final TextView secondaryTv;

        public HeaderTinyViewHolder(View itemView) {
            super(itemView);
            primaryTv = itemView.findViewById(R.id.tv_recycler_item_header_primary);
            secondaryTv = itemView.findViewById(R.id.tv_recycler_item_header_secondary);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                listener.onDelegateClick(view, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (listener != null) {
                listener.onDelegateLongClick(view, getAdapterPosition());
            }
            return true;
        }

    }

}
