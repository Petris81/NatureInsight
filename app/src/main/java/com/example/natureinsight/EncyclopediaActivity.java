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
import java.util.List;

public class EncyclopediaActivity extends AppCompatActivity {

    RecyclerView encyclopediaList;
    EncyclopediaAdapter adapter;
    List<EncyclopediaItem> items;
    List<EncyclopediaItem> allItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encyclopedia);

        encyclopediaList = findViewById(R.id.encyclopedia_list);
        encyclopediaList.setLayoutManager(new LinearLayoutManager(this));

        // Données fictives
        allItems = new ArrayList<>();
        allItems.add(new EncyclopediaItem("Chêne pédonculé", "Arbre majestueux des forêts d'Europe."));
        allItems.add(new EncyclopediaItem("Pissenlit", "Plante vivace aux fleurs jaunes, connue pour ses graines volantes."));
        allItems.add(new EncyclopediaItem("Fougère aigle", "Grande fougère fréquente dans les sous-bois."));
        allItems.add(new EncyclopediaItem("Trèfle blanc", "Plante herbacée des prairies, souvent porte-bonheur."));

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
}
