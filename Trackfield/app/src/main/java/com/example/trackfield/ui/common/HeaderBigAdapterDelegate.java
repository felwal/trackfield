package com.example.trackfield.ui.common;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.ui.base.BaseAdapterDelegate;
import com.example.trackfield.ui.base.DelegateClickListener;
import com.example.trackfield.ui.common.model.Header;
import com.example.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class HeaderBigAdapterDelegate extends
    BaseAdapterDelegate<Header, RecyclerItem, HeaderBigAdapterDelegate.HeaderBigViewHolder> {

    public HeaderBigAdapterDelegate(Activity a, DelegateClickListener listener) {
        super(a, listener);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Header && ((Header) item).isType(Header.Type.YEAR);
    }

    @NonNull
    @Override
    public HeaderBigViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new HeaderBigViewHolder(inflater.inflate(R.layout.item_header_big, parent, false));
    }

    @Override
    public void onBindViewHolder(Header item, @NonNull HeaderBigViewHolder vh, @Nullable List<Object> payloads) {
        vh.primaryTv.setText(item.getTitle());
        vh.secondaryTv.setText(item.printValues());

        // set height depending on if children are expanded
        int height = (int) c.getResources().getDimension(
            item.areChildrenExpanded() ? R.dimen.layout_header_year : R.dimen.layout_header_year_collapsed);
        vh.itemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
    }

    // vh

    class HeaderBigViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnLongClickListener {

        public TextView primaryTv;
        public TextView secondaryTv;

        public HeaderBigViewHolder(View itemView) {
            super(itemView);
            primaryTv = itemView.findViewById(R.id.textView_primary);
            secondaryTv = itemView.findViewById(R.id.textView_secondary);
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
