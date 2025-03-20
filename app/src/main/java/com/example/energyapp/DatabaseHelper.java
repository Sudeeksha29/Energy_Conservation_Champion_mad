package com.example.energyapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UserDB.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    private static final String TABLE_APPLIANCES = "appliances";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_APPLIANCE_ID = "appliance_id";
    private static final String COLUMN_APPLIANCE_NAME = "appliance_name";
    private static final String COLUMN_COUNT = "count";
    private static final String COLUMN_POWER_FACTOR = "power_factor";
    private static final String COLUMN_WEEKLY_CONSUMPTION = "weekly_consumption";

    private static final String TABLE_METER_DATA = "meter_data";
    private static final String COLUMN_METER_ID = "meter_id";
    private static final String COLUMN_CURRENT_READING = "current_meter_reading";
    public static final String COLUMN_CONSUMPTION_ENERGY = "consumption_energy";
    private static final String COLUMN_CONSUMPTION_AFTER_WEEK = "consumption_after_week";
    private static final String COLUMN_START_DATE = "start_date";
    private static final String COLUMN_END_DATE = "end_date";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_APPLIANCES_TABLE = "CREATE TABLE " + TABLE_APPLIANCES + " (" +
                COLUMN_APPLIANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER, " +
                COLUMN_APPLIANCE_NAME + " TEXT, " +
                COLUMN_COUNT + " INTEGER, " +
                COLUMN_POWER_FACTOR + " REAL, " +
                COLUMN_WEEKLY_CONSUMPTION + " REAL, " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_APPLIANCES_TABLE);

        String CREATE_METER_DATA_TABLE = "CREATE TABLE " + TABLE_METER_DATA + " (" +
                COLUMN_METER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER, " +
                COLUMN_CURRENT_READING + " REAL, " +
                COLUMN_CONSUMPTION_ENERGY + " REAL, " +
                COLUMN_CONSUMPTION_AFTER_WEEK + " REAL DEFAULT 0, " +
                COLUMN_START_DATE + " TEXT, " +
                COLUMN_END_DATE + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_METER_DATA_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPLIANCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_METER_DATA);
        onCreate(db);
    }

    // Insert user with hashed password
    public int insertUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        if (result == -1) return -1; // Insert failed

        return getLastInsertedUserId();
    }

    public int getLastInsertedUserId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + COLUMN_ID + ") FROM " + TABLE_USERS, null);
        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(0);
            cursor.close();
            return userId;
        }
        cursor.close();
        return -1;
    }
    // Check user login with password verification
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_PASSWORD}, COLUMN_EMAIL + "=?",
                new String[]{email}, null, null, null);

        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);
            cursor.close();
            return storedPassword.equals(hashPassword(password)); // Compare hashed passwords
        }
        cursor.close();
        return false;
    }

    // Hashing password with SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean insertAppliance(int userId, String applianceName, int count, double powerFactor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_APPLIANCE_NAME, applianceName);
        values.put(COLUMN_COUNT, count);
        values.put(COLUMN_POWER_FACTOR, powerFactor);
        values.put(COLUMN_WEEKLY_CONSUMPTION, count * powerFactor * 7);

        long result = db.insert(TABLE_APPLIANCES, null, values);
        return result != -1;
    }

    // Fetch appliance data for a specific user
    public Cursor getApplianceData(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT appliance_id, appliance_name, count FROM appliances WHERE user_id = ?", new String[]{String.valueOf(userId)});
    }

    // Update appliance count
    public boolean updateAppliance(int applianceId, int count) {
        SQLiteDatabase db = this.getWritableDatabase();
        double powerFactor = getPowerFactor(applianceId);
        double weeklyConsumption = count * powerFactor * 7;

        ContentValues values = new ContentValues();
        values.put("count", count);
        values.put("weekly_consumption", weeklyConsumption);

        int result = db.update("appliances", values, "appliance_id = ?", new String[]{String.valueOf(applianceId)});
        return result > 0;
    }

    public String getUserName(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_NAME + " FROM " + TABLE_USERS + " WHERE " + COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            String userName = cursor.getString(0);
            cursor.close();
            return userName;
        }
        cursor.close();
        return "User"; // Default fallback name
    }


    // Fetch power factor for an appliance (hardcoded per appliance type)
    private double getPowerFactor(int applianceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT power_factor FROM appliances WHERE appliance_id = ?", new String[]{String.valueOf(applianceId)});
        if (cursor.moveToFirst()) {
            double powerFactor = cursor.getDouble(0);
            cursor.close();
            return powerFactor;
        }
        cursor.close();
        return 0.0;
    }




    // Get total weekly consumption from appliances table
    public double getTotalWeeklyConsumption(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(weekly_consumption) FROM appliances WHERE user_id = ?", new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            double totalConsumption = cursor.getDouble(0);
            cursor.close();
            return totalConsumption;
        }
        cursor.close();
        return 0.0;
    }

        public void saveUserId(Context context, int userId) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("userId", userId);
            editor.apply();
        }

        // Retrieve user ID from shared preferences
        public int getUserId(Context context) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            return sharedPreferences.getInt("userId", -1);
        }

        public boolean insertMeterData(int userId, double currentReading, double consumptionEnergy, String startDate, String endDate) {
            SQLiteDatabase db = this.getWritableDatabase();

            // Check if userId is valid before inserting
            if (userId == -1) {
                return false; // User ID is invalid
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_ID, userId);
            values.put(COLUMN_CURRENT_READING, currentReading);
            values.put(COLUMN_CONSUMPTION_ENERGY, consumptionEnergy);
            values.put(COLUMN_CONSUMPTION_AFTER_WEEK, 0);
            values.put(COLUMN_START_DATE, startDate);
            values.put(COLUMN_END_DATE, endDate);

            long result = db.insert(TABLE_METER_DATA, null, values);
            return result != -1;
        }

    // Retrieve the last meter data entry for the given user
    public Cursor getLastMeterData(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + COLUMN_CONSUMPTION_ENERGY + " FROM " + TABLE_METER_DATA +
                        " WHERE " + COLUMN_USER_ID + " = ? ORDER BY " + COLUMN_METER_ID + " DESC LIMIT 1",
                new String[]{String.valueOf(userId)});
    }

    public boolean updateConsumptionAfterWeek(int userId, double weekReading) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONSUMPTION_AFTER_WEEK, weekReading);

        int rowsUpdated = db.update(TABLE_METER_DATA, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        return rowsUpdated > 0;
    }

    public Cursor getAllMeterData(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT meter_id, current_meter_reading, consumption_energy, consumption_after_week, start_date, end_date FROM meter_data WHERE user_id = ?", new String[]{String.valueOf(userId)});
    }


}

