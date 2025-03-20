package com.example.energyapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class InsertActivity extends AppCompatActivity {
    DatabaseHelper dbHelper;
    int userId;
    EditText meterReadingInput, consumptionValueInput;
    Button calculateButton, nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        dbHelper = new DatabaseHelper(this);
        userId = getUserId(); // Retrieve user ID dynamically

        Button insertButton = findViewById(R.id.insert_button);
        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertApplianceData();
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

    private void insertApplianceData() {
        insertAppliance("Fans", R.id.fans, 0.75);
        insertAppliance("Fridge", R.id.fridge, 1.2);
        insertAppliance("AC", R.id.ac, 3.5);
        insertAppliance("Lights", R.id.lights, 0.5);
        insertAppliance("Oven", R.id.oven, 2.0);
        insertAppliance("TV", R.id.tv, 0.2);
        insertAppliance("Bulbs", R.id.bulbs, 0.1);
        insertAppliance("Cooler", R.id.cooler, 1.5);
        insertAppliance("Water Filter", R.id.water_filter, 0.3);
        insertAppliance("Chargers", R.id.chargers, 0.05);
        insertAppliance("Inverter", R.id.inverter, 1.0);
        insertAppliance("Router", R.id.router, 0.1);
        insertAppliance("Computer", R.id.computer, 0.4);
        insertAppliance("Heater", R.id.heater, 2.5);
        insertAppliance("Washing Machine", R.id.washing_machine, 1.8);

        Toast.makeText(this, "Data Inserted Successfully!", Toast.LENGTH_SHORT).show();
    }

    private void insertAppliance(String applianceName, int editTextId, double powerFactor) {
        EditText editText = findViewById(editTextId);
        String countStr = editText.getText().toString();
        int count = countStr.isEmpty() ? 0 : Integer.parseInt(countStr);

        if (count > 0) {
            dbHelper.insertAppliance(userId, applianceName, count, powerFactor);
        }
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
                Intent intent = new Intent(InsertActivity.this, MeterdataActivity.class);
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
