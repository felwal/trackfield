package com.example.trackfield.adapters.adapterdelegates.delegates;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.adapters.adapterdelegates.AdapterDelegate;
import com.example.trackfield.adapters.adapterdelegates.ListDelegationAdapter;
import com.example.trackfield.adapters.recycleradapters.RecyclerAdapter;
import com.example.trackfield.items.headers.RecyclerItem;
import com.example.trackfield.items.headers.Sorter;

import java.util.ArrayList;

public class SorterAdapterDelegate implements AdapterDelegate<ArrayList<RecyclerItem>>  {

    private final LayoutInflater inflater;
    private final ListDelegationAdapter adapter;

    //

    public SorterAdapterDelegate(Context c, ListDelegationAdapter adapter) {
        inflater = LayoutInflater.from(c);
        this.adapter = adapter;
    }

    @Override
    public boolean isForViewType(@NonNull ArrayList<RecyclerItem> items, int position) {
        return items.get(position) instanceof Sorter;
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new SorterVH(inflater.inflate(R.layout.item_exercise, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ArrayList<RecyclerItem> items, int position, @NonNull RecyclerView.ViewHolder viewHolder) {
        Sorter sorter = (Sorter) items.get(position);
        SorterVH holder = (SorterVH) viewHolder;

        holder.title.setText(sorter.getTitle());
        //adapter.sortMode = sorter.getSortMode();
    }

    public class SorterVH extends RecyclerAdapter.BaseVH implements View.OnClickListener {

        public View view;
        public ConstraintLayout button;
        public TextView title;

        public SorterVH(View v) {
            super(v);
            view = v;
            button = v.findViewById(R.id.constraintLayout_sort);
            title = v.findViewById(R.id.textView_sort);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (adapter.listener != null) {
                adapter.listener.onItemClick(view, getAdapterPosition(), getItemViewType());
            }
        }

    }

}
