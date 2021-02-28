package com.example.trackfield.adapters.adapterdelegates;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackfield.items.headers.RecyclerItem;

import java.util.ArrayList;

public class ListDelegationAdapter extends RecyclerView.Adapter {

    public AdapterDelegate.ItemClickListener listener;
    protected AdapterDelegatesManager<ArrayList<RecyclerItem>> delegatesManager;
    protected ArrayList<RecyclerItem> items;

    //

    public ListDelegationAdapter() {
        this(new AdapterDelegatesManager<>());
    }

    public ListDelegationAdapter(@NonNull AdapterDelegate<ArrayList<RecyclerItem>>... delegates) {
        this(new AdapterDelegatesManager<>(delegates));
    }

    public ListDelegationAdapter(@NonNull AdapterDelegatesManager<ArrayList<RecyclerItem>> delegatesManager) {
        this.delegatesManager = delegatesManager;
    }

    //

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        delegatesManager.onBindViewHolder(items, holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void setItems(ArrayList<RecyclerItem> items) {
        this.items = items;
    }

    public ArrayList<RecyclerItem> getItems() {
        return items;
    }

}
