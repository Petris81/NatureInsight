package com.example.natureinsight;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class PhotoActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_CAMERA_PERMISSION = 100;
    private SupabaseAuth supabaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supabaseAuth = SupabaseAuth.getInstance();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());

            // Generate a unique filename for the image
            String fileName = UUID.randomUUID().toString() + ".jpg";
            
            // Convert bitmap to byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] imageData = stream.toByteArray();

            // Upload the image to Supabase storage
            supabaseAuth.uploadImage(imageData, fileName, new SupabaseAuth.FileUploadCallback() {
                @Override
                public void onSuccess(String fileUrl) {
                    // For now, we'll use placeholder values for the observation
                    // In a real app, these would come from the plant identification API
                    String plantName = "Inconnu"; // This should come from your plant identification API
                    double latitude = 0.0; // This should come from GPS
                    double longitude = 0.0; // This should come from GPS
                    int confidence = 80; // This should come from your plant identification API
                    int altitude = 0; // This should come from GPS

                    // Insert the observation into Supabase with the image URL
                    supabaseAuth.insertPlantObservation(
                        plantName,
                        latitude,
                        longitude,
                        confidence,
                        altitude,
                        fileUrl,
                        new SupabaseAuth.PlantObservationCallback() {
                            @Override
                            public void onSuccess(JsonObject data) {
                                runOnUiThread(() -> {
                                    Toast.makeText(PhotoActivity.this, 
                                        "Observation enregistrée avec succès!", Toast.LENGTH_SHORT).show();
                                    
                                    // Continue to PlantInfoActivity with the photo
                                    Intent intent = new Intent(PhotoActivity.this, PlantInfoActivity.class);
                                    intent.putExtra("photo_bitmap", photo);
                                    intent.putExtra("plant_name", plantName);
                                    intent.putExtra("observation_date", currentDate);
                                    startActivity(intent);
                                    finish();
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    Toast.makeText(PhotoActivity.this, 
                                        "Erreur lors de l'enregistrement: " + error, Toast.LENGTH_SHORT).show();
                                    
                                    // Still continue to PlantInfoActivity even if Supabase insertion failed
                                    Intent intent = new Intent(PhotoActivity.this, PlantInfoActivity.class);
                                    intent.putExtra("photo_bitmap", photo);
                                    intent.putExtra("plant_name", plantName);
                                    intent.putExtra("observation_date", currentDate);
                                    startActivity(intent);
                                    finish();
                                });
                            }
                        }
                    );
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(PhotoActivity.this, 
                            "Erreur lors du téléchargement de l'image: " + error, Toast.LENGTH_SHORT).show();
                        
                        // Continue to PlantInfoActivity even if image upload failed
                        Intent intent = new Intent(PhotoActivity.this, PlantInfoActivity.class);
                        intent.putExtra("photo_bitmap", photo);
                        intent.putExtra("plant_name", "Inconnu");
                        intent.putExtra("observation_date", currentDate);
                        startActivity(intent);
                        finish();
                    });
                }
            });
        } else {
            finish();
        }
    }
}
