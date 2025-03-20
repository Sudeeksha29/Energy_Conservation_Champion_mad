package com.example.energyapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class UserDatabaseActivity extends AppCompatActivity {
    private ListView listView;
    private MeterDataAdapter adapter;
    private List<MeterData> meterDataList;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_database);

        listView = findViewById(R.id.listViewMeterData);
        databaseHelper = new DatabaseHelper(this);

        int userId = databaseHelper.getUserId(this);
        loadMeterData(userId);
    }

    private void loadMeterData(int userId) {
        meterDataList = new ArrayList<>();
        Cursor cursor = databaseHelper.getAllMeterData(userId);

        if (cursor.moveToFirst()) {
            do {
                int meterId = cursor.getInt(0);
                double currentReading = cursor.getDouble(1);
                double consumptionEnergy = cursor.getDouble(2);
                double consumptionAfterWeek = cursor.getDouble(3);

                MeterData meterData = new MeterData(meterId, currentReading, consumptionEnergy, consumptionAfterWeek);
                meterDataList.add(meterData);
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new MeterDataAdapter(this, meterDataList);
        listView.setAdapter(adapter);
    }
}
