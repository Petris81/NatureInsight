package com.example.natureinsight;

import android.content.Context;
import android.content.SharedPreferences;
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

    private static final String PREFS_NAME = "NatureInsightPrefs";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";

    private static SupabaseAuth instance;
    private String currentUserToken;
    private String currentUserId;
    private String currentUserEmail;
    private String refreshToken;
    private Context appContext;

    private SupabaseAuth() {
    }

    public static synchronized SupabaseAuth getInstance() {
        if (instance == null) {
            instance = new SupabaseAuth();
        }
        return instance;
    }

    public void init(Context context) {
        this.appContext = context.getApplicationContext();
        loadStoredCredentials();
    }

    private void loadStoredCredentials() {
        if (appContext == null)
            return;

        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUserToken = prefs.getString(KEY_AUTH_TOKEN, null);
        currentUserId = prefs.getString(KEY_USER_ID, null);
        currentUserEmail = prefs.getString(KEY_USER_EMAIL, null);
        refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null);

        Log.d(TAG, "Loaded stored credentials: " + (currentUserToken != null ? "Token exists" : "No token"));
    }

    private void saveCredentials() {
        if (appContext == null)
            return;

        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (currentUserToken != null) {
            editor.putString(KEY_AUTH_TOKEN, currentUserToken);
            editor.putString(KEY_USER_ID, currentUserId);
            editor.putString(KEY_USER_EMAIL, currentUserEmail);
            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        } else {
            editor.remove(KEY_AUTH_TOKEN);
            editor.remove(KEY_USER_ID);
            editor.remove(KEY_USER_EMAIL);
            editor.remove(KEY_REFRESH_TOKEN);
        }

        editor.apply();
    }

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
        saveCredentials();
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
        currentUserEmail = null;
        refreshToken = null;
        saveCredentials();
    }

    public boolean isAuthenticated() {
        return currentUserToken != null;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

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
                .url(SUPABASE_URL + "/rest/v1/" + table + "?" + id)
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
            String scientificName,
            PlantObservationCallback callback) {

        if (!isAuthenticated()) {
            callback.onError("User not authenticated");
            return;
        }

        JsonObject observation = new JsonObject();
        observation.addProperty("userid", currentUserId);
        observation.addProperty("plantname", plantName);
        observation.addProperty("scientificname", scientificName);
        observation.addProperty("observationdatetime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        observation.addProperty("latitude", String.valueOf(latitude));
        observation.addProperty("longitude", String.valueOf(longitude));
        observation.addProperty("confidenceinidentification", confidenceInIdentification);
        observation.addProperty("altitudeofobservation", altitudeOfObservation);
        observation.addProperty("pictureofobservation", pictureOfObservation);
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

        // query with limit=50 and order by observationdatetime in descending order
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/plant_observations?userid=eq." + currentUserId
                        + "&order=observationdatetime.desc&limit=50")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + currentUserToken)
                .get()
                .build();

        executeDataListRequest(request, callback);
    }

    public void uploadImage(byte[] imageData, String fileName, FileUploadCallback callback) {
        if (!isAuthenticated()) {
            callback.onError("User not authenticated");
            return;
        }

        // supabase storage bucket policy is that each user only has access to the
        // folder
        // haveing their id as the name of the folder
        String filePath = currentUserId + "/" + fileName;

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,
                        RequestBody.create(MediaType.parse("image/jpeg"), imageData))
                .build();
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/storage/v1/object/plantimages/" + filePath)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + currentUserToken)
                .post(requestBody)
                .build();
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Upload failed: " + errorBody);
                    callback.onError(errorBody);
                    return;
                }
                String fileUrl = SUPABASE_URL + "/storage/v1/object/public/plantimages/" + filePath;
                callback.onSuccess(fileUrl);
            } catch (IOException e) {
                Log.e(TAG, "Upload failed", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    public void getSignedImageUrl(String filePath, FileUploadCallback callback) {
        if (!isAuthenticated()) {
            callback.onError("User not authenticated");
            return;
        }
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("token", currentUserToken);
        jsonBody.addProperty("expiresIn", 3600);

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/storage/v1/object/sign/plantimages/" + filePath)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + currentUserToken)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Failed to get signed URL: " + errorBody);
                    callback.onError(errorBody);
                    return;
                }
                String responseBody = response.body().string();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

                if (jsonResponse.has("signedURL")) {
                    String signedUrl = jsonResponse.get("signedURL").getAsString();
                    callback.onSuccess(signedUrl);
                } else {
                    callback.onError("No signed URL in response");
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to get signed URL", e);
                callback.onError(e.getMessage());
            }
        }).start();
    }

    private void executeAuthRequest(Request request, AuthCallback callback) {
        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    currentUserToken = jsonResponse.get("access_token").getAsString();
                    currentUserId = jsonResponse.get("user").getAsJsonObject().get("id").getAsString();

                    if (jsonResponse.has("refresh_token")) {
                        refreshToken = jsonResponse.get("refresh_token").getAsString();
                        Log.d(TAG, "Refresh token saved successfully");
                    } else {
                        Log.e(TAG, "No refresh token in response");
                    }

                    saveCredentials();
                    Log.d(TAG, "Credentials saved: " + (currentUserToken != null ? "Token exists" : "No token") +
                            ", Refresh token: " + (refreshToken != null ? "exists" : "missing"));

                    callback.onSuccess(currentUserToken);
                } else {
                    Log.e(TAG, "Auth error: " + responseBody);
                    callback.onError("Authentication failed: " + responseBody);
                }
            } catch (IOException e) {
                Log.e(TAG, "Auth request failed", e);
                callback.onError("Network error: " + e.getMessage());
            }
        }).start();
    }

    private void refreshToken(AuthCallback callback) {
        if (refreshToken == null) {
            Log.e(TAG, "pas de refresh token");
            currentUserToken = null;
            currentUserId = null;
            currentUserEmail = null;
            refreshToken = null;
            saveCredentials();
            callback.onError("Session expired.");
            return;
        }
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("refresh_token", refreshToken);

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=refresh_token")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        executeAuthRequest(request, callback);
    }

    private void executeDataRequest(Request request, DataCallback callback) {
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (request.body() != null) {
                    Log.d(TAG, "Request body: " + request.body().toString());
                }

                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";

                    if (response.code() == 401 && errorBody.contains("JWT expired")) {
                        refreshToken(new AuthCallback() {
                            @Override
                            public void onSuccess(String token) {
                                Request newRequest = request.newBuilder()
                                        .header("Authorization", "Bearer " + token)
                                        .build();
                                executeDataRequest(newRequest, callback);
                            }

                            @Override
                            public void onError(String error) {
                                callback.onError("Token refresh failed: " + error);
                            }
                        });
                        return;
                    }

                    callback.onError(errorBody);
                    return;
                }

                String responseBody = response.body().string();
                if (responseBody.equals("[]") && request.method().equals("PATCH")) {
                    JsonObject successResponse = new JsonObject();
                    successResponse.addProperty("success", true);
                    callback.onSuccess(successResponse);
                    return;
                }

                JsonElement jsonElement = JsonParser.parseString(responseBody);

                if (jsonElement.isJsonArray()) {
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    if (jsonArray.size() > 0) {
                        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
                        callback.onSuccess(jsonObject);
                    } else {
                        JsonObject emptyResponse = new JsonObject();
                        emptyResponse.addProperty("success", true);
                        callback.onSuccess(emptyResponse);
                    }
                } else if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    callback.onSuccess(jsonObject);
                } else {
                    JsonObject result = new JsonObject();
                    result.add("value", jsonElement);
                    callback.onSuccess(result);
                }
            } catch (IOException e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    private void executeDataListRequest(Request request, DataListCallback callback) {
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    if (response.code() == 401 && errorBody.contains("JWT expired")) {
                        refreshToken(new AuthCallback() {
                            @Override
                            public void onSuccess(String token) {
                                Request newRequest = request.newBuilder()
                                        .header("Authorization", "Bearer " + token)
                                        .build();
                                executeDataListRequest(newRequest, callback);
                            }

                            @Override
                            public void onError(String error) {
                                callback.onError("Token refresh failed: " + error);
                            }
                        });
                        return;
                    }

                    Log.e(TAG, "Request failed: " + errorBody);
                    callback.onError(errorBody);
                    return;
                }

                String responseBody = response.body().string();
                Log.d(TAG, "Response body: " + responseBody);
                JsonElement jsonElement = JsonParser.parseString(responseBody);
                if (jsonElement.isJsonArray()) {
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    callback.onSuccess(jsonArray);
                } else if (jsonElement.isJsonObject()) {
                    JsonArray jsonArray = new JsonArray();
                    jsonArray.add(jsonElement);
                    callback.onSuccess(jsonArray);
                } else {
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