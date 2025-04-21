package com.example.natureinsight;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PlantInfoActivity extends AppCompatActivity {

    TextView plantNameText, dateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_info);

        plantNameText = findViewById(R.id.plant_name);
        dateText = findViewById(R.id.observation_date);

        String plantName = getIntent().getStringExtra("plant_name");
        String plantDate = getIntent().getStringExtra("plant_date");

        plantNameText.setText(plantName);
        dateText.setText(getString(R.string.observation_date) + " " + plantDate);

        ImageView imageView = findViewById(R.id.plant_image);
        Bitmap photo = getIntent().getParcelableExtra("photo_bitmap");
        if (photo != null) {
            imageView.setImageBitmap(photo);
        }
        TextView plantNameText = findViewById(R.id.plant_name);
        String name = getIntent().getStringExtra("plant_name");
        if (name != null) {
            plantNameText.setText(name);
        }
        TextView observationDateText = findViewById(R.id.observation_date);
        String date = getIntent().getStringExtra("observation_date");
        if (date != null) {
            observationDateText.setText(getString(R.string.observation_date) + " " + date);
        }
        if (name != null && date != null) {
            HistoryData.historyList.add(new HistoryItem(name, date));
        }


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
