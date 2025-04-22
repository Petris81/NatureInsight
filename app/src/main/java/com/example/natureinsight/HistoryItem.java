package com.example.natureinsight;

import com.google.gson.JsonObject;

public class HistoryItem {
    public String title;
    public String date;
    public String pictureUrl;
    public double latitude;
    public double longitude;
    public int confidenceInIdentification;
    public int altitudeOfObservation;
    public String id;

    public HistoryItem(String title, String date) {
        this.title = title;
        this.date = date;
    }

    public HistoryItem(JsonObject jsonObject) {
        this.id = jsonObject.has("id") ? jsonObject.get("id").getAsString() : "";
        this.title = jsonObject.has("plantname") ? jsonObject.get("plantname").getAsString() : "";
        this.date = jsonObject.has("observationdatetime") ? jsonObject.get("observationdatetime").getAsString() : "";
        this.pictureUrl = jsonObject.has("pictureofobservation") ? jsonObject.get("pictureofobservation").getAsString() : "";
        this.latitude = jsonObject.has("latitude") ? Double.parseDouble(jsonObject.get("latitude").getAsString()) : 0.0;
        this.longitude = jsonObject.has("longitude") ? Double.parseDouble(jsonObject.get("longitude").getAsString()) : 0.0;
        this.confidenceInIdentification = jsonObject.has("confidenceinidentification") ? jsonObject.get("confidenceinidentification").getAsInt() : 0;
        this.altitudeOfObservation = jsonObject.has("altitudeofobservation") ? jsonObject.get("altitudeofobservation").getAsInt() : 0;
    }
}
