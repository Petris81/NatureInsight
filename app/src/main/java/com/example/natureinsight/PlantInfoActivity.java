package com.example.natureinsight;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.natureinsight.SupabaseAuth.DataCallback;
import com.example.natureinsight.SupabaseAuth.DataListCallback;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantInfoActivity extends AppCompatActivity {

    private static final String TAG = "PlantInfoActivity";
    private static final String SUPABASE_URL = "https://pnwcnyojlyuzvzfzcmtm.supabase.co";
    private TextView plantNameText, dateText, positionText, altitudeText, confidenceText, scientificNameText,
            existingComment, commentLabel;
    private ImageView imageView;
    private SupabaseAuth supabaseAuth;
    private DatabaseManager databaseManager;
    private GradientValueView niFixationValue, solStructureValue, waterRetentionValue;

    private EditText commentInput;
    private Button saveCommentButton, addCommentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_info);

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
        commentInput = findViewById(R.id.comment_input);
        saveCommentButton = findViewById(R.id.save_comment_button);
        addCommentButton = findViewById(R.id.add_comment_button);
        existingComment = findViewById(R.id.existing_comment);
        commentLabel = findViewById(R.id.comment_label);

        String plantName = getIntent().getStringExtra("plant_name");
        String plantDate = getIntent().getStringExtra("plant_date");
        String plantDateUnformated = getIntent().getStringExtra("plant_date_unformated");
        String plantLatitude = getIntent().getStringExtra("plant_latitude");
        String plantLongitude = getIntent().getStringExtra("plant_longitude");
        String plantConfidence = getIntent().getStringExtra("plant_confidence");
        String scientificName = getIntent().getStringExtra("scientific_name");
        Integer plantAltitude = getIntent().getIntExtra("plant_altitude", 0);
        String comment = getIntent().getStringExtra("noteutilisateur");

        existingComment.setText(comment);
        if (comment != null && !comment.isEmpty()) {
            existingComment.setVisibility(View.VISIBLE);
            commentLabel.setVisibility(View.VISIBLE);
        }
        plantNameText.setText(plantName);
        scientificNameText.setText(scientificName != null ? scientificName : "");
        updateEcosystemServices(scientificName);
        dateText.setText(getString(R.string.observation_date) + " " + plantDate);
        positionText.setText(getString(R.string.position) + " " + getString(R.string.latitude) + plantLatitude + ", "
                + getString(R.string.longitude) + plantLongitude);
        altitudeText.setText(getString(R.string.altitude) + " " + plantAltitude);
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
                                    finalUrl = SUPABASE_URL + "/storage/v1" + signedUrl;
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
        Map<String, Object> primaryKey = new HashMap<>();
        primaryKey.put("userid", supabaseAuth.getCurrentUserId());
        primaryKey.put("plantname", getIntent().getStringExtra("plant_name"));
        primaryKey.put("observationdatetime", getIntent().getStringExtra("observation_datetime"));
        findViewById(R.id.add_comment_button).setOnClickListener(v -> {
            commentInput.setVisibility(View.VISIBLE);
            saveCommentButton.setVisibility(View.VISIBLE);
            addCommentButton.setVisibility(View.GONE);
        });
        findViewById(R.id.save_comment_button).setOnClickListener(v -> {
            if (plantDate == null) {
                Toast.makeText(PlantInfoActivity.this, "Erreur: date d'observation manquante", Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            JsonObject updateData = new JsonObject();
            updateData.addProperty("noteutilisateur", commentInput.getText().toString());

            String timestamp = getIntent().getStringExtra("observation_datetime");
            if (timestamp != null) {
                try {
                    timestamp = timestamp.replaceAll("^\"|\"$", "");
                    java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(timestamp);
                    timestamp = dateTime
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
                } catch (Exception e) {
                    Log.e(TAG, "Error formatting timestamp: " + e.getMessage());
                }
            }
            String encodedUserId = Uri.encode(supabaseAuth.getCurrentUserId());
            String encodedPlantName = Uri.encode(plantName);
            String encodedTimestamp = timestamp.toString();// Uri.encode(timestamp);
            String selectQuery = String.format("userid=eq.%s&plantname=eq.%s&observationdatetime=eq.%s",
                    encodedUserId,
                    encodedPlantName,
                    encodedTimestamp);

            Log.d(TAG, "Select query: " + selectQuery);

            // irst we verify the record exist
            supabaseAuth.select("plant_observations", selectQuery, new DataListCallback() {
                @Override
                public void onSuccess(JsonArray data) {
                    if (data != null && data.size() > 0) {
                        String updateQuery = String.format("userid=eq.%s&plantname=eq.%s&observationdatetime=eq.%s",
                                encodedUserId,
                                encodedPlantName,
                                encodedTimestamp);

                        JsonObject updateData = new JsonObject();
                        updateData.addProperty("noteutilisateur", commentInput.getText().toString());
                        supabaseAuth.update("plant_observations", updateQuery, updateData, new DataCallback() {
                            @Override
                            public void onSuccess(JsonObject data) {
                                supabaseAuth.select("plant_observations", selectQuery, new DataListCallback() {
                                    @Override
                                    public void onSuccess(JsonArray verifyData) {
                                        runOnUiThread(() -> {
                                            if (verifyData.size() > 0) {
                                                JsonObject updatedRecord = verifyData.get(0).getAsJsonObject();
                                                String updatedNote = null;
                                                if (updatedRecord.has("noteutilisateur")
                                                        && !updatedRecord.get("noteutilisateur").isJsonNull()) {
                                                    updatedNote = updatedRecord.get("noteutilisateur").getAsString();
                                                }

                                                if (updatedNote != null
                                                        && updatedNote.equals(commentInput.getText().toString())) {
                                                    Toast.makeText(PlantInfoActivity.this,
                                                            getString(R.string.save_success), Toast.LENGTH_SHORT)
                                                            .show();
                                                    commentInput.setVisibility(View.GONE);
                                                    saveCommentButton.setVisibility(View.GONE);
                                                    addCommentButton.setVisibility(View.VISIBLE);
                                                    existingComment.setText(commentInput.getText().toString());
                                                    commentLabel.setVisibility(View.VISIBLE);
                                                } else {
                                                    Toast.makeText(PlantInfoActivity.this,
                                                            getString(R.string.update_failed), Toast.LENGTH_LONG)
                                                            .show();
                                                }
                                            } else {
                                                Toast.makeText(PlantInfoActivity.this,
                                                        getString(R.string.record_not_found_after_update),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(String error) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(PlantInfoActivity.this,
                                                    getString(R.string.verification_error, error), Toast.LENGTH_LONG)
                                                    .show();
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    Toast.makeText(PlantInfoActivity.this, getString(R.string.save_error, error),
                                            Toast.LENGTH_LONG).show();
                                });
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(PlantInfoActivity.this, getString(R.string.record_not_found),
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(PlantInfoActivity.this, getString(R.string.verification_error, error),
                                Toast.LENGTH_LONG).show();
                    });
                }
            });
            existingComment.setVisibility(View.VISIBLE);
        });
        findViewById(R.id.learn_more_button).setOnClickListener(v -> {
            String searchQuery = Uri.encode(plantName);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + searchQuery));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        findViewById(R.id.nav_encyclopedia)
                .setOnClickListener(v -> startActivity(new Intent(this, EncyclopediaActivity.class)));

        findViewById(R.id.nav_camera).setOnClickListener(v -> startActivity(new Intent(this, PhotoActivity.class)));

        findViewById(R.id.nav_history).setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));

        findViewById(R.id.nav_account).setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
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
        List<EcosystemService> services = databaseManager.queryEcosystemServices(null, scientificName);
        if (!services.isEmpty()) {
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
