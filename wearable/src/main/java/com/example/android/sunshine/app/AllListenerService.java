package com.example.android.sunshine.app;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by rkrde on 12-12-2016.
 */

public class AllListenerService extends WearableListenerService {
    String TAG = "All_Listener";

    private static final String WEAR_PATH = "/wear_path";
    private static final String WEATHER_ID_KEY = "id_key";
    private static final String WEATHER_MIN_TEMP = "min_temp_key";
    private static final String WEATHER_MAX_TEMP = "max_temp_key";
    private static final String WEATHER_UNIT_KEY = "weather_unit_key";



    GoogleApiClient mGoogleApiClient;
    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate");
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
        Log.d(TAG,"onDataChanged123");
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(WEAR_PATH) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    WearDataModal modal = new WearDataModal(dataMap.getInt(WEATHER_ID_KEY),
                            dataMap.getFloat(WEATHER_MIN_TEMP),
                            dataMap.getFloat(WEATHER_MAX_TEMP),
                            dataMap.getString(WEATHER_UNIT_KEY)
                    );
                    updateCount(getApplicationContext(),modal);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    private void updateCount(Context context,WearDataModal modal)
    {
        Log.d(TAG,"Weather ID:"+modal.getWeatherId()+",min temp:"+modal.getMinTemp());
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d(TAG,"onMessage Received");
    }

}
