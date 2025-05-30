package com.example.natureinsight;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        String formattedDate = formatDate(item.date);
        holder.dateText.setText(formattedDate);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlantInfoActivity.class);
            intent.putExtra("observation_datetime", item.date);
            intent.putExtra("plant_name", item.title);
            intent.putExtra("plant_date", formattedDate);
            intent.putExtra("plant_image", item.pictureUrl);
            intent.putExtra("plant_latitude", String.valueOf(item.latitude));
            intent.putExtra("plant_longitude", String.valueOf(item.longitude));
            intent.putExtra("plant_confidence", String.valueOf(item.confidenceInIdentification));
            intent.putExtra("plant_altitude", item.altitudeOfObservation);
            intent.putExtra("plant_id", item.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    private String formatDate(String isoDate) {
        try {
            String cleanDate = isoDate.replace("T", " ");
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
            Date date = inputFormat.parse(cleanDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            return isoDate;
        }
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
