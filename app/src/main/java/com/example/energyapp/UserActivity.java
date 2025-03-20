package com.example.energyapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserActivity extends AppCompatActivity {
    Button btnInsert, btnUpdate, btnUserDashboard;
    TextView tvUserGreeting;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user); // Ensure correct layout is referenced

        tvUserGreeting = findViewById(R.id.tvUserGreeting);
        dbHelper = new DatabaseHelper(this);

        // Retrieve user ID from shared preferences
        int userId = dbHelper.getUserId(this);

        if (userId != -1) {
            // Fetch the username from the database
            String userName = dbHelper.getUserName(userId);
            tvUserGreeting.setText("Hello " + userName + "!");
        } else {
            tvUserGreeting.setText("Hello !");
        }

        // Initialize buttons
        btnInsert = findViewById(R.id.btnInsert);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnUserDashboard=findViewById(R.id.btnUserdashboard);

        // Insert button action
        btnInsert.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, InsertActivity.class);
            startActivity(intent);
        });

        // Update button action (FIXED: Should go to UpdateActivity)
        btnUpdate.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, UpdateActivity.class);
            startActivity(intent);
        });

        // User Dashboard button action

        btnUserDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, UserDatabaseActivity.class);
            startActivity(intent);
        });
    }
}
