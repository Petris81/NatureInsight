package com.example.natureinsight;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class PhotoActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_CAMERA_PERMISSION = 100;
    static final int REQUEST_LOCATION_PERMISSION = 101;
    
    private SupabaseAuth supabaseAuth;
    private PlantIdentificationService plantIdentificationService;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private int altitudeOfObservation = 0;
    private int confidenceInIdentification = 0; // valeur par défault
    private String plantName; // valeur par défaut
    private String scientificName; // valeur par défaut
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supabaseAuth = SupabaseAuth.getInstance();
        plantIdentificationService = new PlantIdentificationService();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize default strings
        plantName = getString(R.string.unknown);
        scientificName = getString(R.string.unknown);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
        
        // Request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            getCurrentLocation();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }
    
    private void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = location;
                            altitudeOfObservation = (int) location.getAltitude();
                            Log.d("PhotoActivity", "Location: " + location.getLatitude() + ", " + location.getLongitude() + 
                                  ", Altitude: " + altitudeOfObservation);
                        } else {
                            Log.d("PhotoActivity", "Current location is null");
                        }
                    }
                });
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
        } else if (requestCode == REQUEST_LOCATION_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            if (requestCode == REQUEST_CAMERA_PERMISSION) {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());

            // get a uuid for each image
            String fileName = UUID.randomUUID().toString() + ".jpg";

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] imageData = stream.toByteArray();

            //identify
            identifyPlant(photo, imageData, fileName, currentDate);
        } else {
            finish();
        }
    }
    
    private void identifyPlant(Bitmap photo, byte[] imageData, String fileName, String currentDate) {
        plantIdentificationService.identifyPlant(photo, new PlantIdentificationService.PlantIdentificationCallback() {
            @Override
            public void onSuccess(PlantIdentificationService.PlantIdentificationResult result) {
                plantName = result.getCommonName();
                scientificName = result.getScientificName();
                confidenceInIdentification = (int) result.getConfidence();
                Log.d("PhotoActivity", "Plant identified: " + plantName + " with confidence: " + confidenceInIdentification + "%");
                uploadImageToSupabase(imageData, fileName, photo, currentDate);
            }

            @Override
            public void onError(String error) {
                Log.e("PhotoActivity", "Plant identification error: " + error);
                uploadImageToSupabase(imageData, fileName, photo, currentDate);
            }
        });
    }
    
    private void uploadImageToSupabase(byte[] imageData, String fileName, Bitmap photo, String currentDate) {
        supabaseAuth.uploadImage(imageData, fileName, new SupabaseAuth.FileUploadCallback() {
            @Override
            public void onSuccess(String fileUrl) {
                double latitude = currentLocation != null ? currentLocation.getLatitude() : 0.0;
                double longitude = currentLocation != null ? currentLocation.getLongitude() : 0.0;
                supabaseAuth.insertPlantObservation(
                    plantName,
                    latitude,
                    longitude,
                    confidenceInIdentification,
                    altitudeOfObservation,
                    fileUrl,
                    scientificName,
                    new SupabaseAuth.PlantObservationCallback() {
                        @Override
                        public void onSuccess(JsonObject data) {
                            runOnUiThread(() -> {
                                Toast.makeText(PhotoActivity.this, 
                                    getString(R.string.observation_saved), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PhotoActivity.this, PlantInfoActivity.class);
                                intent.putExtra("photo_bitmap", photo);
                                intent.putExtra("plant_name", plantName);
                                intent.putExtra("plant_date", currentDate);
                                intent.putExtra("plant_latitude", String.valueOf(latitude));
                                intent.putExtra("plant_longitude", String.valueOf(longitude));
                                intent.putExtra("plant_confidence", String.valueOf(confidenceInIdentification));
                                intent.putExtra("plant_altitude", String.valueOf(altitudeOfObservation));
                                intent.putExtra("scientific_name", String.valueOf(scientificName));
                                intent.putExtra("observation_datetime", data.get("observationdatetime").toString());
                                Log.d("PhotoActivity", "onSuccess: " + data.toString());
                                startActivity(intent);
                                finish();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(PhotoActivity.this, 
                                    getString(R.string.error_saving, error), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PhotoActivity.this, PlantInfoActivity.class);
                                intent.putExtra("photo_bitmap", photo);
                                intent.putExtra("plant_name", plantName);
                                intent.putExtra("plant_date", currentDate);
                                intent.putExtra("plant_latitude", String.valueOf(currentLocation != null ? currentLocation.getLatitude() : 0.0));
                                intent.putExtra("plant_longitude", String.valueOf(currentLocation != null ? currentLocation.getLongitude() : 0.0));
                                intent.putExtra("plant_confidence", String.valueOf(confidenceInIdentification));
                                intent.putExtra("plant_altitude", String.valueOf(altitudeOfObservation));
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
                        getString(R.string.error_uploading, error), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PhotoActivity.this, PlantInfoActivity.class);
                    intent.putExtra("photo_bitmap", photo);
                    intent.putExtra("plant_name", plantName);
                    intent.putExtra("plant_date", currentDate);
                    intent.putExtra("plant_latitude", String.valueOf(currentLocation != null ? currentLocation.getLatitude() : 0.0));
                    intent.putExtra("plant_longitude", String.valueOf(currentLocation != null ? currentLocation.getLongitude() : 0.0));
                    intent.putExtra("plant_confidence", String.valueOf(confidenceInIdentification));
                    intent.putExtra("plant_altitude", String.valueOf(altitudeOfObservation));
                    startActivity(intent);
                    finish();
                });
            }
        });
    }
}