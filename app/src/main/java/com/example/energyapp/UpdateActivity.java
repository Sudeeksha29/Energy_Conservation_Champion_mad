package com.example.energyapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class UpdateActivity extends AppCompatActivity {
    DatabaseHelper dbHelper;
    EditText meterReadingInput, consumptionValueInput;
    Button calculateButton, nextButton;
    int userId = 1; // Replace with actual logged-in user ID
    HashMap<String, Integer> applianceIds = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update); // Ensure you use the correct layout

        dbHelper = new DatabaseHelper(this);
        loadApplianceData(); // Fetch and display existing values

        Button updateButton = findViewById(R.id.update_button);
        updateButton.setText("UPDATE DATA"); // Set button text
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateApplianceData(); // Update data in the database
            }
        });

        meterReadingInput = findViewById(R.id.meter_reading);
        consumptionValueInput = findViewById(R.id.consumption_value);
        calculateButton = findViewById(R.id.calculate_button);
        nextButton = findViewById(R.id.next_button);

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateEnergyConsumption();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMeterData();
            }
        });
    }


    private int getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("userId", -1); // Default -1 if no user is found
    }

    private void loadApplianceData() {
        Cursor cursor = dbHelper.getApplianceData(userId);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int applianceId = cursor.getInt(0);
                String applianceName = cursor.getString(1);
                int count = cursor.getInt(2);

                applianceIds.put(applianceName, applianceId);
                setEditTextValue(applianceName, count);
            }
            cursor.close();
        }
    }

    private void setEditTextValue(String applianceName, int count) {
        int editTextId = getApplianceEditTextId(applianceName);
        if (editTextId != -1) {
            EditText editText = findViewById(editTextId);
            editText.setText(String.valueOf(count));
        }
    }

    private int getApplianceEditTextId(String applianceName) {
        switch (applianceName) {
            case "Fans": return R.id.fans;
            case "Fridge": return R.id.fridge;
            case "AC": return R.id.ac;
            case "Lights": return R.id.lights;
            case "Oven": return R.id.oven;
            case "TV": return R.id.tv;
            case "Bulbs": return R.id.bulbs;
            case "Cooler": return R.id.cooler;
            case "Water Filter": return R.id.water_filter;
            case "Chargers": return R.id.chargers;
            case "Inverter": return R.id.inverter;
            case "Router": return R.id.router;
            case "Computer": return R.id.computer;
            case "Heater": return R.id.heater;
            case "Washing Machine": return R.id.washing_machine;
            default: return -1;
        }
    }

    private void updateApplianceData() {
        for (String applianceName : applianceIds.keySet()) {
            int applianceId = applianceIds.get(applianceName);
            int editTextId = getApplianceEditTextId(applianceName);
            if (editTextId != -1) {
                EditText editText = findViewById(editTextId);
                String countStr = editText.getText().toString();
                int count = countStr.isEmpty() ? 0 : Integer.parseInt(countStr);

                dbHelper.updateAppliance(applianceId, count);
            }
        }
        Toast.makeText(this, "Data Updated Successfully!", Toast.LENGTH_SHORT).show();
    }

    private void moveToMeterData() {
        Intent intent = new Intent(UpdateActivity.this,MeterdataActivity.class);
        startActivity(intent);
        finish(); // Close the update activity
    }

    private void calculateEnergyConsumption() {
        double currentMeterReading = Double.parseDouble(meterReadingInput.getText().toString());
        double weeklyConsumption = dbHelper.getTotalWeeklyConsumption(userId);
        double totalConsumption = currentMeterReading + weeklyConsumption;

        consumptionValueInput.setText(String.valueOf(totalConsumption));
        Toast.makeText(this, "Consumption Energy Updated", Toast.LENGTH_SHORT).show();
    }

    private void saveMeterData() {
        try {
            if (userId == -1) {
                Toast.makeText(this, "Invalid User ID. Please log in again.", Toast.LENGTH_SHORT).show();
                return;
            }

            String meterReadingStr = meterReadingInput.getText().toString();
            String consumptionStr = consumptionValueInput.getText().toString();

            if (meterReadingStr.isEmpty() || consumptionStr.isEmpty()) {
                Toast.makeText(this, "Please enter both meter reading and consumption values", Toast.LENGTH_SHORT).show();
                return;
            }

            double currentMeterReading = Double.parseDouble(meterReadingStr);
            double consumptionEnergy = Double.parseDouble(consumptionStr);

            if (currentMeterReading < 0 || consumptionEnergy < 0) {
                Toast.makeText(this, "Values cannot be negative", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDate = dateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, 7);
            String endDate = dateFormat.format(calendar.getTime());

            boolean success = dbHelper.insertMeterData(userId, currentMeterReading, consumptionEnergy, startDate, endDate);
            if (success) {
                Toast.makeText(this, "Meter Data Saved Successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UpdateActivity.this, MeterdataActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Failed to save meter data. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input! Please enter valid numeric values.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occurred while saving data", Toast.LENGTH_SHORT).show();
        }
    }

}

