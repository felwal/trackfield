package me.felwal.trackfield.ui.common;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import me.felwal.trackfield.R;
import me.felwal.trackfield.ui.base.BaseAdapterDelegate;
import me.felwal.trackfield.ui.base.DelegateClickListener;
import me.felwal.trackfield.ui.common.model.Header;
import me.felwal.trackfield.ui.common.model.RecyclerItem;

import java.util.List;

public class HeaderMediumAdapterDelegate extends
    BaseAdapterDelegate<Header, RecyclerItem, HeaderMediumAdapterDelegate.HeaderSmallViewHolder> {

    public HeaderMediumAdapterDelegate(Activity a, DelegateClickListener listener) {
        super(a, listener);
    }

    // extends AbsListItemAdapterDelegate

    @Override
    public boolean isForViewType(@NonNull RecyclerItem item) {
        return item instanceof Header && ((Header) item).isType(Header.Type.MONTH);
    }

    @NonNull
    @Override
    public HeaderSmallViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new HeaderSmallViewHolder(inflater.inflate(R.layout.item_recycler_header_small, parent, false));
    }

    @Override
    public void onBindViewHolder(Header item, @NonNull HeaderSmallViewHolder vh, @Nullable List<Object> payloads) {
        vh.primaryTv.setText(item.getTitle());
        vh.secondaryTv.setText(item.printValues());

        // set height depending on if children are expanded
        int height = (int) c.getResources().getDimension(
            item.areChildrenExpanded() ? R.dimen.layout_recycler_header_month : R.dimen.layout_recycler_header_month_collapsed);
        vh.itemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
    }

    // vh

    class HeaderSmallViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnLongClickListener {

        public final TextView primaryTv;
        public final TextView secondaryTv;

        public HeaderSmallViewHolder(View itemView) {
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
