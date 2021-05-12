package com.example.trackfield.ui.common;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.base.BaseAdapterDelegate;
import com.example.trackfield.ui.common.model.Header;
import com.example.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class HeaderSmallAdapterDelegate extends
    BaseAdapterDelegate<Header, RecyclerItem, HeaderSmallAdapterDelegate.HeaderSmallViewHolder> {

    public HeaderSmallAdapterDelegate(Activity activity, DelegateClickListener listener) {
        super(activity, listener);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Header && ((Header) item).isType(Header.Type.WEEK, Header.Type.REC);
    }

    @NonNull
    @Override
    public HeaderSmallViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new HeaderSmallViewHolder(inflater.inflate(R.layout.item_header_small, parent, false));
    }

    @Override
    public void onBindViewHolder(Header item, @NonNull HeaderSmallViewHolder vh, @Nullable List<Object> payloads) {
        vh.primary.setText(item.getTitle());
        vh.secondary.setText(item.printValues());
    }

    // vh

    class HeaderSmallViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnLongClickListener {

        public TextView primary;
        public TextView secondary;

        public HeaderSmallViewHolder(View itemView) {
            super(itemView);
            primary = itemView.findViewById(R.id.textView_primary);
            secondary = itemView.findViewById(R.id.textView_secondary);
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
