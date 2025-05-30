package com.example.natureinsight;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    private RecyclerView historyList;
    private HistoryAdapter adapter;
    private List<HistoryItem> items;
    private SupabaseAuth supabaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        supabaseAuth = SupabaseAuth.getInstance();

        historyList = findViewById(R.id.history_list);
        
        historyList.setLayoutManager(new LinearLayoutManager(this));

        items = new ArrayList<>();
        adapter = new HistoryAdapter(items, this);
        historyList.setAdapter(adapter);

        findViewById(R.id.nav_encyclopedia).setOnClickListener(v ->
                startActivity(new Intent(this, EncyclopediaActivity.class)));

        findViewById(R.id.nav_camera).setOnClickListener(v ->
                startActivity(new Intent(this, PhotoActivity.class)));

        findViewById(R.id.nav_history).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        findViewById(R.id.nav_account).setOnClickListener(v ->
                startActivity(new Intent(this, AccountActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryFromSupabase();
    }

    private void loadHistoryFromSupabase() {
        if (!supabaseAuth.isAuthenticated()) {
            Toast.makeText(this, getString(R.string.sign_in_history), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        supabaseAuth.getPlantObservations(new SupabaseAuth.DataListCallback() {
            @Override
            public void onSuccess(JsonArray data) {
                runOnUiThread(() -> {
                    items.clear();
                    for (int i = 0; i < data.size(); i++) {
                        JsonObject observation = data.get(i).getAsJsonObject();
                        HistoryItem item = new HistoryItem(observation);
                        items.add(item);
                    }
                    Collections.sort(items, new Comparator<HistoryItem>() {
                        @Override
                        public int compare(HistoryItem o1, HistoryItem o2) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
                                Date date1 = sdf.parse(o1.date);
                                Date date2 = sdf.parse(o2.date);
                                return date2.compareTo(date1);
                            } catch (ParseException e) {
                                Log.e(TAG, "Error parsing date", e);
                                return 0;
                            }
                        }
                    });
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading history: " + error);
                    if (error.contains("Session expired")) {
                        Toast.makeText(HistoryActivity.this, getString(R.string.session_expired), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(HistoryActivity.this, getString(R.string.error_loading_history, error), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
