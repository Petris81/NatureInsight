package com.example.natureinsight;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItem> items;
    private Context context;

    public HistoryAdapter(List<HistoryItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = items.get(position);
        holder.titleText.setText(item.title);
        holder.dateText.setText(item.date);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlantInfoActivity.class);
            intent.putExtra("plant_name", item.title);
            intent.putExtra("plant_date", item.date);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, dateText;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.item_title);
            dateText = itemView.findViewById(R.id.item_date);
        }
    }
}
