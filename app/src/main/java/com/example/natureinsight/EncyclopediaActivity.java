package com.example.natureinsight;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EncyclopediaActivity extends AppCompatActivity {

    RecyclerView encyclopediaList;
    EncyclopediaAdapter adapter;
    List<EncyclopediaItem> items;
    List<EncyclopediaItem> allItems;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encyclopedia);

        // Initialize DatabaseManager
        databaseManager = DatabaseManager.getInstance();
        databaseManager.init(this);

        encyclopediaList = findViewById(R.id.encyclopedia_list);
        encyclopediaList.setLayoutManager(new LinearLayoutManager(this));

        // Get unique species from database
        Set<String> uniqueSpecies = new HashSet<>();
        List<EcosystemService> services = databaseManager.queryEcosystemServices(null, null);
        for (EcosystemService service : services) {
            uniqueSpecies.add(service.getSpecies());
        }

        // Convert to EncyclopediaItems
        allItems = new ArrayList<>();
        for (String species : uniqueSpecies) {
            allItems.add(new EncyclopediaItem(species, ""));
        }

        items = new ArrayList<>(allItems);
        adapter = new EncyclopediaAdapter(items);
        encyclopediaList.setAdapter(adapter);

        ImageView searchIcon = findViewById(R.id.search_icon);
        LinearLayout searchContainer = findViewById(R.id.search_container);
        EditText searchInput = findViewById(R.id.search_input);
        Button searchButton = findViewById(R.id.search_button);

        searchIcon.setOnClickListener(v -> {
            if (searchContainer.getVisibility() == View.GONE) {
                searchContainer.setVisibility(View.VISIBLE);
            } else {
                searchContainer.setVisibility(View.GONE);
            }
        });

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().toLowerCase();
            List<EncyclopediaItem> filtered = new ArrayList<>();
            for (EncyclopediaItem item : allItems) {
                if (item.name.toLowerCase().contains(query) || item.description.toLowerCase().contains(query)) {
                    filtered.add(item);
                }
            }
            items = filtered;
            adapter = new EncyclopediaAdapter(items);
            encyclopediaList.setAdapter(adapter);
        });

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
    protected void onDestroy() {
        super.onDestroy();
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
}
