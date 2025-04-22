package com.example.natureinsight;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class PlantInfoActivity extends AppCompatActivity {

    private static final String TAG = "PlantInfoActivity";
    private static final String SUPABASE_URL = "https://pnwcnyojlyuzvzfzcmtm.supabase.co";
    private TextView plantNameText, dateText, positionText, altitudeText, confidenceText;
    private ImageView imageView;
    private SupabaseAuth supabaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_info);

        // Initialize SupabaseAuth
        supabaseAuth = SupabaseAuth.getInstance();
        plantNameText = findViewById(R.id.plant_name);
        dateText = findViewById(R.id.observation_date);
        imageView = findViewById(R.id.plant_image);
        positionText = findViewById(R.id.position);
        altitudeText = findViewById(R.id.altitude);
        confidenceText = findViewById(R.id.confidence);


        String plantName = getIntent().getStringExtra("plant_name");
        String plantDate = getIntent().getStringExtra("plant_date");
        String plantLatitude = getIntent().getStringExtra("plant_latitude");
        String plantLongitude = getIntent().getStringExtra("plant_longitude");
        String plantConfidence = getIntent().getStringExtra("plant_confidence");

        plantNameText.setText(plantName);
        dateText.setText(getString(R.string.observation_date) + " " + plantDate);
        positionText.setText(positionText.getText()+" Lat:" + plantLatitude + ", Long:" + plantLongitude);
        altitudeText.setText(getString(R.string.altitude) + " " + getIntent().getStringExtra("plant_altitude"));
        confidenceText.setText(getString(R.string.confidence) + " " + plantConfidence);
        // Handle image loading
        Bitmap photo = getIntent().getParcelableExtra("photo_bitmap");
        if (photo != null) {
            // Direct bitmap from camera
            imageView.setImageBitmap(photo);
        } else {
            // Image from history
            String imageUrl = getIntent().getStringExtra("plant_image");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Extract the file path from the full URL
                String filePath = extractFilePath(imageUrl);
                if (filePath != null) {
                    // Get a signed URL for the image
                    supabaseAuth.getSignedImageUrl(filePath, new SupabaseAuth.FileUploadCallback() {
                        @Override
                        public void onSuccess(String signedUrl) {
                            // Load the image using the signed URL
                            runOnUiThread(() -> {
                                // Construct the full URL with the Supabase domain
                                // Check if the signedUrl already contains the domain
                                String finalUrl;
                                if (signedUrl.contains("supabase.co")) {
                                    finalUrl = signedUrl;
                                } else {
                                    // Add the domain if it's not already there
                                    finalUrl = SUPABASE_URL +"/storage/v1"+ signedUrl;
                                }
                                Log.d(TAG, "signedUrl: " + signedUrl);
                                Log.d(TAG, "Supabase: " + SUPABASE_URL);
                                Log.d(TAG, "Loading image from signed URL: " + finalUrl);
                                
                                Glide.with(PlantInfoActivity.this)
                                        .load(finalUrl)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE) // Don't cache the signed URL
                                        .into(imageView);
                            });
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Failed to get signed URL: " + error);
                            runOnUiThread(() -> {
                                Toast.makeText(PlantInfoActivity.this, 
                                    "Failed to load image", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                } else {
                    // If we can't extract the file path, try loading the URL directly
                    // This might work if the URL is already a signed URL or if the bucket is public
                    final String finalUrl = formatUrl(imageUrl);
                        
                    Glide.with(this)
                            .load(finalUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // Don't cache the signed URL
                            .into(imageView);
                }
            }
        }

        // Navigation buttons
        findViewById(R.id.nav_encyclopedia).setOnClickListener(v ->
                startActivity(new Intent(this, EncyclopediaActivity.class)));

        findViewById(R.id.nav_camera).setOnClickListener(v ->
                    startActivity(new Intent(this, PhotoActivity.class)));

        findViewById(R.id.nav_history).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        findViewById(R.id.nav_account).setOnClickListener(v ->
                startActivity(new Intent(this, AccountActivity.class)));
    }

    /**
     * Extracts the file path from a Supabase storage URL
     * Example: https://pnwcnyojlyuzvzfzcmtm.supabase.co/storage/v1/object/public/plantimages/user123/image.jpg
     * Returns: user123/image.jpg
     */
    private String formatUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        // If the URL already has a domain, return it as is
        if (url.contains("supabase.co")) {
            return url;
        }

        // Remove any leading slashes
        while (url.startsWith("/")) {
            url = url.substring(1);
        }

        // Add the Supabase domain if the URL doesn't have one
        if (!url.startsWith("http")) {
            url = SUPABASE_URL + "/storage/v1/object/public/plantimages/" + url;
        }

        return url;
    }
    private String extractFilePath(String url) {
        try {
            // Find the index of "plantimages/" in the URL
            int startIndex = url.indexOf("plantimages/");
            if (startIndex != -1) {
                // Add the length of "plantimages/" to get to the start of the file path
                startIndex += "plantimages/".length();
                return url.substring(startIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting file path from URL", e);
        }
        return null;
    }
}
