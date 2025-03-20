package com.example.energyapp;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;




public class MeterdataActivity extends AppCompatActivity {
    DatabaseHelper dbHelper;
    EditText editTextWeekReading, editTextCanConsume;
    Button btnCompare, setalarm;

    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meterdata);

        dbHelper = new DatabaseHelper(this);
        editTextWeekReading = findViewById(R.id.editTextWeekReading);
        editTextCanConsume = findViewById(R.id.editTextCanConsume);
        btnCompare = findViewById(R.id.btnCompare);
        setalarm = findViewById(R.id.btnSetAlarm);

        userId = getUserId();

        // Retrieve previous consumption value
        double previousConsumption = getPreviousConsumption(userId);
        editTextCanConsume.setText(String.valueOf(previousConsumption));

        btnCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compareConsumption(previousConsumption);
            }
        });


        setalarm.setOnClickListener((new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                Intent intent=new Intent(AlarmClock.ACTION_SET_ALARM);
                intent.putExtra(AlarmClock.EXTRA_HOUR,11);
                intent.putExtra(AlarmClock.EXTRA_MINUTES,30);
                startActivity(intent);
            }
        }));






    }

    private int getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getInt("userId", -1); // Default -1 if no user is found
    }



    private double getPreviousConsumption(int userId) {
        Cursor cursor = dbHelper.getLastMeterData(userId);
        if (cursor != null && cursor.moveToFirst()) {
            double consumption = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONSUMPTION_ENERGY));
            cursor.close();
            return consumption;
        }
        return 0.0; // Default value if no data found
    }

    private void compareConsumption(double canConsume) {
        String weekReadingStr = editTextWeekReading.getText().toString();

        if (!weekReadingStr.isEmpty()) {
            double weekReading = Double.parseDouble(weekReadingStr);

            // Store the new meter reading into the database
            boolean success = dbHelper.updateConsumptionAfterWeek(userId, weekReading);
            if (!success) {
                Toast.makeText(getApplicationContext(), "Failed to update meter data!", Toast.LENGTH_LONG).show();
                return;
            }

            if (weekReading <= canConsume) {
                Toast.makeText(getApplicationContext(), "Good job! Keep it up! You are consuming enough!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "You are consuming more!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please enter this week's meter reading.", Toast.LENGTH_LONG).show();
        }
    }
}

