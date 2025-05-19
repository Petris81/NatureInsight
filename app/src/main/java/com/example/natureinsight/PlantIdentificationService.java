package com.example.natureinsight;

import android.graphics.Bitmap;
import android.util.Log;
import java.util.Locale;

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

public class PlantIdentificationService {
    private static final String TAG = "PlantIdentificationService";
    private static final String API_URL = "https://my-api.plantnet.org/v2/identify/all";
    private static final String API_KEY = "2b107fLCqkEG3shka0neYmPLe";
    private static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public interface PlantIdentificationCallback {
        void onSuccess(PlantIdentificationResult result);
        void onError(String error);
    }
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

    public void identifyPlant(Bitmap bitmap, PlantIdentificationCallback callback) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] imageBytes = stream.toByteArray();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("images", "plant.jpg",
                        RequestBody.create(MEDIA_TYPE_JPEG, imageBytes))
                .addFormDataPart("organs", "auto")
                .addFormDataPart("lang", Locale.getDefault().getLanguage())
                .build();
        Request request = new Request.Builder()
                .url(API_URL + "?include-related-images=false&no-reject=false&nb-results=10&lang=en&api-key=" + API_KEY)
                .addHeader("accept", "application/json")
                .addHeader("Content-Type", "multipart/form-data")
                .post(requestBody)
                .build();
        Log.d(TAG, "Sending request to: " + request.url());
        Log.d(TAG, "request as a string: " + request.toString());
        Log.d(TAG, "Using API key: " + API_KEY);
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
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                if (jsonResponse.has("results") && jsonResponse.get("results").getAsJsonArray().size() > 0) {
                    JsonObject bestResult = jsonResponse.get("results").getAsJsonArray().get(0).getAsJsonObject();
                    JsonObject species = bestResult.get("species").getAsJsonObject();
                    String scientificName = species.get("scientificNameWithoutAuthor").getAsString();
                    List<String> commonNames = new ArrayList<>();
                    if (species.has("commonNames") && species.get("commonNames").getAsJsonArray().size() > 0) {
                        JsonArray commonNamesArray = species.get("commonNames").getAsJsonArray();
                        for (JsonElement element : commonNamesArray) {
                            commonNames.add(element.getAsString());
                        }
                    }
                    String commonName = commonNames.isEmpty() ? scientificName : commonNames.get(0);
                    double confidence = bestResult.get("score").getAsDouble() * 100;
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