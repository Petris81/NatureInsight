package com.example.natureinsight;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Service class for handling plant identification API calls.
 * Uses the Plant.id API to identify plants from images.
 */
public class PlantIdentificationService {
    private static final String TAG = "PlantIdentificationService";
    private static final String API_URL = "https://api.plant.id/v2/identify";
    private static final String API_KEY = "2b10o3slmiJWarzGzRInISf8w";
    private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    /**
     * Interface for plant identification callbacks.
     */
    public interface PlantIdentificationCallback {
        void onSuccess(PlantIdentificationResult result);
        void onError(String error);
    }

    /**
     * Result class for plant identification.
     */
    public static class PlantIdentificationResult {
        private String scientificName;
        private String commonName;
        private double confidence;
        private String family;
        private String genus;
        private List<String> commonNames;

        public PlantIdentificationResult(String scientificName, String commonName, double confidence, 
                                        String family, String genus, List<String> commonNames) {
            this.scientificName = scientificName;
            this.commonName = commonName;
            this.confidence = confidence;
            this.family = family;
            this.genus = genus;
            this.commonNames = commonNames;
        }

        public String getScientificName() {
            return scientificName;
        }

        public String getCommonName() {
            return commonName;
        }

        public double getConfidence() {
            return confidence;
        }

        public String getFamily() {
            return family;
        }

        public String getGenus() {
            return genus;
        }

        public List<String> getCommonNames() {
            return commonNames;
        }
    }

    /**
     * Identifies a plant from a bitmap image.
     * 
     * @param bitmap The image to identify
     * @param callback The callback to handle the response
     */
    public void identifyPlant(Bitmap bitmap, PlantIdentificationCallback callback) {
        // Convert bitmap to JPEG byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] imageBytes = stream.toByteArray();

        // Create multipart request body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("images", "plant.jpg",
                        RequestBody.create(MEDIA_TYPE_JPEG, imageBytes))
                .addFormDataPart("organs", "auto")
                .build();


        // Create the request with API key in the header
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("api-key", API_KEY)
                .addHeader("Content-Type", "multipart/form-data")
                .post(requestBody)
                .build();

        // Log the request URL for debugging
        Log.d(TAG, "Sending request to: " + request.url());
        Log.d(TAG, "request as a string: " + request.toString());
        Log.d(TAG, "Using API key: " + API_KEY);
        // Execute the request
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Identification failed: " + errorBody);
                    callback.onError("Identification failed: " + response.code());
                    return;
                }

                String responseBody = response.body().string();
                Log.d(TAG, "Identification response: " + responseBody);
                
                // Parse the response
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                
                // Check if we have results
                if (jsonResponse.has("results") && jsonResponse.get("results").getAsJsonArray().size() > 0) {
                    JsonObject bestResult = jsonResponse.get("results").getAsJsonArray().get(0).getAsJsonObject();
                    
                    // Extract species information
                    JsonObject species = bestResult.get("species").getAsJsonObject();
                    String scientificName = species.get("scientificNameWithoutAuthor").getAsString();
                    
                    // Extract common names
                    List<String> commonNames = new ArrayList<>();
                    if (species.has("commonNames") && species.get("commonNames").getAsJsonArray().size() > 0) {
                        JsonArray commonNamesArray = species.get("commonNames").getAsJsonArray();
                        for (JsonElement element : commonNamesArray) {
                            commonNames.add(element.getAsString());
                        }
                    }
                    
                    // Get the first common name or use scientific name if none available
                    String commonName = commonNames.isEmpty() ? scientificName : commonNames.get(0);
                    
                    // Extract confidence score
                    double confidence = bestResult.get("score").getAsDouble() * 100; // Convert to percentage
                    
                    // Extract family and genus
                    String family = "";
                    String genus = "";
                    if (species.has("family")) {
                        family = species.get("family").getAsJsonObject()
                                .get("scientificNameWithoutAuthor").getAsString();
                    }
                    if (species.has("genus")) {
                        genus = species.get("genus").getAsJsonObject()
                                .get("scientificNameWithoutAuthor").getAsString();
                    }
                    
                    // Create and return the result
                    PlantIdentificationResult result = new PlantIdentificationResult(
                            scientificName, commonName, confidence, family, genus, commonNames);
                    callback.onSuccess(result);
                } else {
                    callback.onError("No plant identification results found");
                }
            } catch (IOException e) {
                Log.e(TAG, "Identification failed", e);
                callback.onError("Identification failed: " + e.getMessage());
            }
        }).start();
    }
} 