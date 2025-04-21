package com.example.natureinsight;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    RecyclerView historyList;
    HistoryAdapter adapter;
    List<HistoryItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyList = findViewById(R.id.history_list);
        historyList.setLayoutManager(new LinearLayoutManager(this));


        items = HistoryData.historyList;
        adapter = new HistoryAdapter(items,this);
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
}
