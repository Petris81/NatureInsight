package com.example.natureinsight;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EncyclopediaAdapter extends RecyclerView.Adapter<EncyclopediaAdapter.EncyclopediaViewHolder> {

    private List<EncyclopediaItem> items;

    public EncyclopediaAdapter(List<EncyclopediaItem> items) {
        this.items = items;
    }

    @Override
    public EncyclopediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_encyclopedia, parent, false);
        return new EncyclopediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EncyclopediaViewHolder holder, int position) {
        EncyclopediaItem item = items.get(position);
        holder.nameTextView.setText(item.name);
        holder.descriptionTextView.setText(item.description);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class EncyclopediaViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;

        public EncyclopediaViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.encyclopedia_item_name);
            descriptionTextView = itemView.findViewById(R.id.encyclopedia_item_description);
        }
    }
}
