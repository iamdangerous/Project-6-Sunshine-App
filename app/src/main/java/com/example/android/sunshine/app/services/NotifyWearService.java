package com.example.android.sunshine.app.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.application.Initializer;
import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.modal.WearDataModal;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

import javax.inject.Inject;

public class NotifyWearService extends IntentService {

    @Inject
    GoogleApiClient mGoogleApiClient;

    private static int count = 0;
    private static final String WEAR_PATH = "/wear_path";
    private static final String WEATHER_ID_KEY = "id_key";
    private static final String WEATHER_MIN_TEMP = "min_temp_key";
    private static final String WEATHER_MAX_TEMP = "max_temp_key";
    private static final String WEATHER_UNIT_KEY = "weather_unit_key";

    final static String TAG = "NotifyWearService";

    public NotifyWearService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((Initializer)getApplicationContext()).getNetComponent().inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final WearDataModal wearData  = getWearData(getApplicationContext());

        if(null == wearData)
            return;

        PutDataMapRequest dataMap = PutDataMapRequest.create(WEAR_PATH);
        dataMap.getDataMap().putInt(WEATHER_ID_KEY, wearData.getWeatherId());
        dataMap.getDataMap().putFloat(WEATHER_MAX_TEMP, wearData.getMaxTemp());
        dataMap.getDataMap().putFloat(WEATHER_MIN_TEMP, wearData.getMinTemp());
        dataMap.getDataMap().putString(WEATHER_UNIT_KEY, wearData.getWeatherUnit());

        PutDataRequest request = dataMap.asPutDataRequest();

        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {

                        Log.d(TAG,"weather Id:"+wearData.getWeatherId());
                        Log.d(TAG, "Sending data was successful: " + dataItemResult.getStatus()
                                .isSuccess());
                    }
                });

    }

    public static void sendWearData(Context context)
    {
        Intent intent = new Intent(context,NotifyWearService.class);
        context.startService(intent);
    }

    private static WearDataModal getWearData(Context context)
    {
        String mUri;
        String mProjection[] = {
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
        };
//        String
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        String locationSetting = Utility.getPreferredLocation(context);
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        Cursor cursor = context.getContentResolver().query(weatherForLocationUri,mProjection,null,null,sortOrder);
        WearDataModal modal = null;
        if(cursor.moveToFirst())
        {
            int weatherId = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            float maxTemp = cursor.getFloat(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
            float minTemp = cursor.getFloat(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
            String weatherUnit = sp.getString(context.getString(R.string.pref_units_key),context.getString(R.string.pref_units_metric));
            modal = new WearDataModal(weatherId,maxTemp,minTemp,weatherUnit);
        }
        return modal;
    }



}
