package com.example.trackfield.adapters.adapterdelegates.delegates;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.R;
import com.example.trackfield.adapters.adapterdelegates.AdapterDelegate;
import com.example.trackfield.adapters.adapterdelegates.ListDelegationAdapter;
import com.example.trackfield.adapters.recycleradapters.RecyclerAdapter;
import com.example.trackfield.items.Exerlite;
import com.example.trackfield.items.headers.RecyclerItem;

import java.util.ArrayList;

public class ExerciseAdapterDelegate implements AdapterDelegate<ArrayList<RecyclerItem>> {

    private final LayoutInflater inflater;
    private final ListDelegationAdapter adapter;

    ////

    public ExerciseAdapterDelegate(Context c, ListDelegationAdapter adapter) {
        inflater = LayoutInflater.from(c);
        this.adapter = adapter;
    }

    @Override
    public boolean isForViewType(@NonNull ArrayList<RecyclerItem> items, int position) {
        return items.get(position) instanceof Exerlite;
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new ExerciseVH(inflater.inflate(R.layout.item_exercise, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ArrayList<RecyclerItem> items, int position, @NonNull RecyclerView.ViewHolder viewHolder) {
        Exerlite exerlite = (Exerlite) items.get(position);
        ExerciseVH holder = (ExerciseVH) viewHolder;

        holder.primary.setText(exerlite.printPrimary());
        holder.secondary.setText(exerlite.printDistanceTimePace());
        holder.caption.setText(exerlite.printCaption());
    }

    public class ExerciseVH extends RecyclerAdapter.BaseVH implements View.OnClickListener {

        public View view;
        public TextView primary;
        public TextView secondary;
        public TextView caption;

        public ExerciseVH(View v) {
            super(v);
            view = v;
            primary = v.findViewById(R.id.textView_primary);
            secondary = v.findViewById(R.id.textView_secondary);
            caption = v.findViewById(R.id.textView_caption);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (adapter.listener != null) {
                adapter.listener.onItemClick(view, getAdapterPosition(), RecyclerAdapter.ITEM_ITEM);
            }
        }

    }

}
