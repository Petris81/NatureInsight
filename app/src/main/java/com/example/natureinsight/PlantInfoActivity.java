package com.example.natureinsight;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class PlantInfoActivity extends AppCompatActivity {

    private static final String TAG = "PlantInfoActivity";
    private static final String SUPABASE_URL = "https://pnwcnyojlyuzvzfzcmtm.supabase.co";
    private TextView plantNameText, dateText, positionText, altitudeText, confidenceText, scientificNameText;
    private ImageView imageView;
    private SupabaseAuth supabaseAuth;
    private DatabaseManager databaseManager;
    private GradientValueView niFixationValue, solStructureValue, waterRetentionValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_info);

        // Initialize SupabaseAuth and DatabaseManager
        supabaseAuth = SupabaseAuth.getInstance();
        databaseManager = DatabaseManager.getInstance();
        databaseManager.init(this);
        plantNameText = findViewById(R.id.plant_name);
        dateText = findViewById(R.id.observation_date);
        imageView = findViewById(R.id.plant_image);
        positionText = findViewById(R.id.position);
        altitudeText = findViewById(R.id.altitude);
        confidenceText = findViewById(R.id.confidence);
        scientificNameText = findViewById(R.id.plant_scientific_name);
        niFixationValue = findViewById(R.id.ni_fixation_value);
        solStructureValue = findViewById(R.id.sol_structure_value);
        waterRetentionValue = findViewById(R.id.water_retention_value);

        String plantName = getIntent().getStringExtra("plant_name");
        String plantDate = getIntent().getStringExtra("plant_date");
        String plantLatitude = getIntent().getStringExtra("plant_latitude");
        String plantLongitude = getIntent().getStringExtra("plant_longitude");
        String plantConfidence = getIntent().getStringExtra("plant_confidence");
        String scientificName = getIntent().getStringExtra("scientific_name");

        plantNameText.setText(plantName);
        scientificNameText.setText(scientificName != null ? scientificName : "");
        updateEcosystemServices(scientificName);
        dateText.setText(getString(R.string.observation_date) + " " + plantDate);
        positionText.setText(getString(R.string.position) + " " + getString(R.string.latitude) + plantLatitude + ", " + getString(R.string.longitude) + plantLongitude);
        altitudeText.setText(getString(R.string.altitude) + " " + getIntent().getStringExtra("plant_altitude"));
        confidenceText.setText(getString(R.string.confidence) + " " + plantConfidence);
        Bitmap photo = getIntent().getParcelableExtra("photo_bitmap");
        if (photo != null) {
            imageView.setImageBitmap(photo);
        } else {
            String imageUrl = getIntent().getStringExtra("plant_image");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String filePath = extractFilePath(imageUrl);
                if (filePath != null) {
                    supabaseAuth.getSignedImageUrl(filePath, new SupabaseAuth.FileUploadCallback() {
                        @Override
                        public void onSuccess(String signedUrl) {
                            runOnUiThread(() -> {
                                String finalUrl;
                                if (signedUrl.contains("supabase.co")) {
                                    finalUrl = signedUrl;
                                } else {
                                    finalUrl = SUPABASE_URL +"/storage/v1"+ signedUrl;
                                }
                                Log.d(TAG, "signedUrl: " + signedUrl);
                                Log.d(TAG, "Supabase: " + SUPABASE_URL);
                                Log.d(TAG, "Loading image from signed URL: " + finalUrl);
                                
                                Glide.with(PlantInfoActivity.this)
                                        .load(finalUrl)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .into(imageView);
                            });
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Failed to get signed URL: " + error);
                            runOnUiThread(() -> {
                                Toast.makeText(PlantInfoActivity.this, 
                                    getString(R.string.error_loading_image), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } else {
                    final String finalUrl = formatUrl(imageUrl);
                        
                    Glide.with(this)
                            .load(finalUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(imageView);
                }
            }
        }
        findViewById(R.id.learn_more_button).setOnClickListener(v -> {
            String searchQuery = Uri.encode(plantName);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + searchQuery));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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
    private String formatUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        if (url.contains("supabase.co")) {
            return url;
        }
        while (url.startsWith("/")) {
            url = url.substring(1);
        }
        if (!url.startsWith("http")) {
            url = SUPABASE_URL + "/storage/v1/object/public/plantimages/" + url;
        }

        return url;
    }
    private String extractFilePath(String url) {
        try {
            int startIndex = url.indexOf("plantimages/");
            if (startIndex != -1) {
                startIndex += "plantimages/".length();
                return url.substring(startIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting file path from URL", e);
        }
        return null;
    }
    private void updateEcosystemServices(String scientificName) {
        if (scientificName == null || scientificName.isEmpty()) {
            return;
        }

        // Query the database for ecosystem services
        List<EcosystemService> services = databaseManager.queryEcosystemServices(null, scientificName);
        if (!services.isEmpty()) {
            // Update UI with ecosystem service values
            for (EcosystemService service : services) {
                switch (service.getService()) {
                    case "soil_structuration":
                        solStructureValue.setValue(service.getValue());
                        break;
                    case "nitrogen_provision":
                        niFixationValue.setValue(service.getValue());
                        break;
                    case "storage_and_return_water":
                        waterRetentionValue.setValue(service.getValue());
                        break;
                }
            }
        }
    }
}
