package com.example.natureinsight;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager is a singleton that manages SQLite database
 */
public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private static final String DATABASE_NAME = "NatureInsightDB";
    private static final int DATABASE_VERSION = 1;
    
    // Table name and columns
    public static final String TABLE_ECOSYSTEM_SERVICES = "ecosystem_services";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_SERVICE = "service";
    public static final String COLUMN_SPECIES = "species";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_RELIABILITY = "reliability";

    private static final String CREATE_TABLE_ECOSYSTEM_SERVICES = 
            "CREATE TABLE " + TABLE_ECOSYSTEM_SERVICES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_SERVICE + " TEXT NOT NULL, " +
            COLUMN_SPECIES + " TEXT NOT NULL, " +
            COLUMN_VALUE + " REAL NOT NULL, " +
            COLUMN_RELIABILITY + " REAL NOT NULL);";
    
    private static DatabaseManager instance;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    
    /**
     * We private the constructor to avoid direct instantiation
     */
    private DatabaseManager() {}
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    public void init(Context context) {
        dbHelper = new DatabaseHelper(context.getApplicationContext());
        database = dbHelper.getWritableDatabase();
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_ECOSYSTEM_SERVICES);
            Log.d(TAG, "Database created");
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ECOSYSTEM_SERVICES);
            onCreate(db);
        }
    }

    public long insertEcosystemService(String service, String species, float value, float reliability) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SERVICE, service);
        values.put(COLUMN_SPECIES, species);
        values.put(COLUMN_VALUE, value);
        values.put(COLUMN_RELIABILITY, reliability);

        return database.insert(TABLE_ECOSYSTEM_SERVICES, null, values);
    }
    
    /**
     * query fo a ecosystem service from a specific specie
     * @return list of EcosystemService objects
     */
    public List<EcosystemService> queryEcosystemServices(String service, String species) {
        List<EcosystemService> services = new ArrayList<>();
        String selection = "";
        String[] selectionArgs = null;
        
        if (service != null && species != null) {
            selection = COLUMN_SERVICE + " = ? AND " + COLUMN_SPECIES + " = ?";
            selectionArgs = new String[]{service, species};
        } else if (service != null) {
            selection = COLUMN_SERVICE + " = ?";
            selectionArgs = new String[]{service};
        } else if (species != null) {
            selection = COLUMN_SPECIES + " = ?";
            selectionArgs = new String[]{species};
        }
        
        Cursor cursor = database.query(
                TABLE_ECOSYSTEM_SERVICES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_SERVICE + " ASC, " + COLUMN_SPECIES + " ASC"
        );
        
        if (cursor.moveToFirst()) {
            do {
                EcosystemService ecosystemService = new EcosystemService(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SPECIES)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_VALUE)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_RELIABILITY))
                );
                services.add(ecosystemService);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return services;
    }
    

    public int loadFromCSV(Context context, String fileName) {
        int count = 0;
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            database.beginTransaction();
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 4) {
                    String service = values[0].trim();
                    String species = values[1].trim();
                    float value = Float.parseFloat(values[2].trim());
                    float reliability = Float.parseFloat(values[3].trim());
                    
                    insertEcosystemService(service, species, value, reliability);
                    count++;
                }
            }
            database.setTransactionSuccessful();
            database.endTransaction();
            reader.close();
            inputStream.close();
            Log.d(TAG, "Loaded " + count + " records from CSV");
            return count;
        } catch (IOException e) {
            Log.e(TAG, "Error loading CSV: " + e.getMessage());
            return -1;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing CSV values: " + e.getMessage());
            return -1;
        }
    }
    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

} 