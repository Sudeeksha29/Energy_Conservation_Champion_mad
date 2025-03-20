package com.example.energyapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.graphics.Color;
import java.util.List;

public class MeterDataAdapter extends BaseAdapter {
    private Context context;
    private List<MeterData> meterDataList;

    // Constructor
    public MeterDataAdapter(Context context, List<MeterData> meterDataList) {
        this.context = context;
        this.meterDataList = meterDataList;
    }

    @Override
    public int getCount() {
        return meterDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return meterDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Create and bind data to each row in the ListView
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.meter_data_row, parent, false);
        }

        // Get references to TextViews in meter_data_row.xml
        TextView tvMeterId = convertView.findViewById(R.id.tvMeterId);
        TextView tvCurrentReading = convertView.findViewById(R.id.tvCurrentReading);
        TextView tvConsumptionEnergy = convertView.findViewById(R.id.tvConsumptionEnergy);
        TextView tvConsumptionAfterWeek = convertView.findViewById(R.id.tvConsumptionAfterWeek);

        // Get the current meter data
        MeterData meterData = meterDataList.get(position);

        // Set values in TextViews
        tvMeterId.setText(String.valueOf(meterData.getMeterId()));
        tvCurrentReading.setText(String.valueOf(meterData.getCurrentReading()));
        tvConsumptionEnergy.setText(String.valueOf(meterData.getConsumptionEnergy()));
        tvConsumptionAfterWeek.setText(String.valueOf(meterData.getConsumptionAfterWeek()));

        // Highlight row in RED if Consumption Energy > Consumption After Week
        if (meterData.getConsumptionEnergy() < meterData.getConsumptionAfterWeek()) {
            convertView.setBackgroundColor(Color.RED);
        } else {
            convertView.setBackgroundColor(Color.WHITE);
        }

        return convertView;
    }
}
