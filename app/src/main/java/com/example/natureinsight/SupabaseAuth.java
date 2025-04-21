package com.example.natureinsight;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MultipartBody;

public class SupabaseAuth {
    private static final String TAG = "SupabaseAuth";
    private static final String SUPABASE_URL = "https://pnwcnyojlyuzvzfzcmtm.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBud2NueW9qbHl1enZ6ZnpjbXRtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ4OTI3NTUsImV4cCI6MjA2MDQ2ODc1NX0.hcy_e5S6ckaLcc06T_h4MqIusxzQnEBVOsMaI9axRIo";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    private static SupabaseAuth instance;
    private String currentUserToken;
    private String currentUserId;
    private String currentUserEmail;

    private SupabaseAuth() {}

    public static synchronized SupabaseAuth getInstance() {
        if (instance == null) {
            instance = new SupabaseAuth();
        }
        return instance;
    }
    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
    }

    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public interface AuthCallback {
        void onSuccess(String token);
        void onError(String error);
    }

    public interface DataCallback {
        void onSuccess(JsonObject data);
        void onError(String error);
    }

    public interface DataListCallback {
        void onSuccess(JsonArray data);
        void onError(String error);
    }

    public interface PlantObservationCallback {
        void onSuccess(JsonObject data);
        void onError(String error);
    }

    public interface FileUploadCallback {
        void onSuccess(String fileUrl);
        void onError(String error);
    }

    // Auth Methods
    public void signUp(String email, String password, AuthCallback callback) {
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("password", password);

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/signup")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        executeAuthRequest(request, callback);
    }

    public void signIn(String email, String password, AuthCallback callback) {
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("password", password);

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        executeAuthRequest(request, callback);
    }

    public void signOut() {
        currentUserToken = null;
        currentUserId = null;
    }

    public boolean isAuthenticated() {
        return currentUserToken != null;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    // Data Methods
    public void insert(String table, JsonObject data, DataCallback callback) {
        if (!isAuthenticated()) {
            callback.onError("User not authenticated");
            return;
        }

        RequestBody body = RequestBody.create(data.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + table)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + currentUserToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build();

        executeDataRequest(request, callback);
    }

    public void select(String table, String query, DataListCallback callback) {
        if (!isAuthenticated()) {
            callback.onError("User not authenticated");
            return;
        }

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + table + "?" + query)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + currentUserToken)
                .get()
                .build();

        executeDataListRequest(request, callback);
    }

    public void update(String table, String id, JsonObject data, DataCallback callback) {
        if (!isAuthenticated()) {
            callback.onError("User not authenticated");
            return;
        }

        RequestBody body = RequestBody.create(data.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + table + "?id=eq." + id)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + currentUserToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .patch(body)
                .build();

        executeDataRequest(request, callback);
    }

    public void delete(String table, String id, DataCallback callback) {
        if (!isAuthenticated()) {
            callback.onError("User not authenticated");
            return;
        }

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/" + table + "?id=eq." + id)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + currentUserToken)
                .delete()
                .build();

        executeDataRequest(request, callback);
    }

    public void insertPlantObservation(
            String plantName,
            double latitude,
            double longitude,
            int confidenceInIdentification,
            int altitudeOfObservation,
            String pictureOfObservation,
            PlantObservationCallback callback) {
        
        if (!isAuthenticated()) {
            callback.onError("User not authenticated");
            return;
        }

        JsonObject observation = new JsonObject();
        observation.addProperty("userid", currentUserId);
        observation.addProperty("plantname", plantName);
        observation.addProperty("observationdatetime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        observation.addProperty("latitude", String.valueOf(latitude));
        observation.addProperty("longitude", String.valueOf(longitude));
        observation.addProperty("confidenceinidentification", confidenceInIdentification);
        observation.addProperty("altitudeofobservation", altitudeOfObservation);
        observation.addProperty("pictureofobservation", pictureOfObservation);

        // Log the JSON for debugging
        Log.d(TAG, "Inserting observation: " + observation.toString());

        RequestBody body = RequestBody.create(observation.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/plant_observations")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + currentUserToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build();

        // Create a DataCallback that delegates to the PlantObservationCallback
        DataCallback dataCallback = new DataCallback() {
            @Override
            public void onSuccess(JsonObject data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        };

        executeDataRequest(request, dataCallback);
    }

    public void getPlantObservations(DataListCallback callback) {
        if (!isAuthenticated()) {
            callback.onError("User not authenticated");
            return;
        }

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/plant_observations?userID=eq." + currentUserId)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + currentUserToken)
                .get()
                .build();

        executeDataListRequest(request, callback);
    }

    // File Upload Methods
    public void uploadImage(byte[] imageData, String fileName, FileUploadCallback callback) {
        if (!isAuthenticated()) {
            callback.onError("User not authenticated");
            return;
        }

        // Create a unique file path using the user's ID and a UUID
        String filePath = currentUserId + "/" + fileName;

        // Create multipart request body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,
                        RequestBody.create(MediaType.parse("image/jpeg"), imageData))
                .build();

        // Create the upload request
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/storage/v1/object/plantimages/" + filePath)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + currentUserToken)
                .post(requestBody)
                .build();

        // Execute the request
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Upload failed: " + errorBody);
                    callback.onError(errorBody);
                    return;
                }

                // Construct the public URL for the uploaded file
                String fileUrl = SUPABASE_URL + "/storage/v1/object/public/plantimages/" + filePath;
                callback.onSuccess(fileUrl);
            } catch (IOException e) {
                Log.e(TAG, "Upload failed", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    private void executeAuthRequest(Request request, AuthCallback callback) {
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Request failed: " + errorBody);
                    callback.onError(errorBody);
                    return;
                }

                String responseBody = response.body().string();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                currentUserToken = jsonResponse.get("access_token").getAsString();
                currentUserId = jsonResponse.get("user").getAsJsonObject().get("id").getAsString();
                callback.onSuccess(currentUserToken);
            } catch (IOException e) {
                Log.e(TAG, "Request failed", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    private void executeDataRequest(Request request, DataCallback callback) {
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Request failed: " + errorBody);
                    callback.onError(errorBody);
                    return;
                }

                String responseBody = response.body().string();
                Log.d(TAG, "Response body: " + responseBody);
                
                // Parse the response as a JsonElement first to check its type
                JsonElement jsonElement = JsonParser.parseString(responseBody);
                
                if (jsonElement.isJsonArray()) {
                    // If it's an array, take the first element if available
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    if (jsonArray.size() > 0) {
                        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
                        callback.onSuccess(jsonObject);
                    } else {
                        // Return an empty object if the array is empty
                        callback.onSuccess(new JsonObject());
                    }
                } else if (jsonElement.isJsonObject()) {
                    // If it's already an object, use it directly
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    callback.onSuccess(jsonObject);
                } else {
                    // Handle other cases (like primitives)
                    JsonObject result = new JsonObject();
                    result.add("value", jsonElement);
                    callback.onSuccess(result);
                }
            } catch (IOException e) {
                Log.e(TAG, "Request failed", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    private void executeDataListRequest(Request request, DataListCallback callback) {
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Request failed: " + errorBody);
                    callback.onError(errorBody);
                    return;
                }

                String responseBody = response.body().string();
                Log.d(TAG, "Response body: " + responseBody);
                
                // Parse the response as a JsonElement first to check its type
                JsonElement jsonElement = JsonParser.parseString(responseBody);
                
                if (jsonElement.isJsonArray()) {
                    // If it's an array, use it directly
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    callback.onSuccess(jsonArray);
                } else if (jsonElement.isJsonObject()) {
                    // If it's an object, wrap it in an array
                    JsonArray jsonArray = new JsonArray();
                    jsonArray.add(jsonElement);
                    callback.onSuccess(jsonArray);
                } else {
                    // Handle other cases (like primitives)
                    JsonArray jsonArray = new JsonArray();
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("value", jsonElement.getAsString());
                    jsonArray.add(jsonObject);
                    callback.onSuccess(jsonArray);
                }
            } catch (IOException e) {
                Log.e(TAG, "Request failed", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }
} 