package com.example.android.sunshine.app;

/**
 * Created by rkrde on 15-12-2016.
 */

public class WearDataModal {
    int weatherId;
    float minTemp;
    float maxTemp;
    String weatherUnit;

    public WearDataModal(int weatherId, float minTemp, float maxTemp,String weatherUnit) {
        this.weatherId = weatherId;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.weatherUnit = weatherUnit;
    }

    public int getWeatherId() {
        return weatherId;
    }

    public float getMinTemp() {
        return minTemp;
    }

    public float getMaxTemp() {
        return maxTemp;
    }

    public String getWeatherUnit() {
        return weatherUnit;
    }
}
