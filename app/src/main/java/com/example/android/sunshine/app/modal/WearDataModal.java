package com.example.android.sunshine.app.modal;

import lombok.Data;

/**
 * Created by rkrde on 15-12-2016.
 */
@Data
public class WearDataModal {
    int weatherId;
    float minTemp;
    float maxTemp;
    String weatherUnit;


    public WearDataModal(int weatherId,float minTemp,float maxTemp,String weatherUnit)
    {
        this.weatherId = weatherId;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.weatherUnit = weatherUnit;
    }
}
