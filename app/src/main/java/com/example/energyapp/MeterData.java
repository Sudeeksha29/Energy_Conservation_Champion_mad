package com.example.energyapp;

public class MeterData {
    private int meterId;
    private double currentReading;
    private double consumptionEnergy;
    private double consumptionAfterWeek;

    public MeterData(int meterId, double currentReading, double consumptionEnergy, double consumptionAfterWeek) {
        this.meterId = meterId;
        this.currentReading = currentReading;
        this.consumptionEnergy = consumptionEnergy;
        this.consumptionAfterWeek = consumptionAfterWeek;
    }

    public int getMeterId() {
        return meterId;
    }

    public double getCurrentReading() {
        return currentReading;
    }

    public double getConsumptionEnergy() {
        return consumptionEnergy;
    }

    public double getConsumptionAfterWeek() {
        return consumptionAfterWeek;
    }
}
