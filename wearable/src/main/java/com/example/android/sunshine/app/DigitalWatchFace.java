package com.example.android.sunshine.app;

/**
 * Created by rkrde on 20-12-2016.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DigitalWatchFace extends CanvasWatchFaceService {
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);
    private static String TAG = DigitalWatchFace.class.getSimpleName();
    private static int c = 0;
    SharedPreferences sp;
    public DigitalWatchFace() {
    }

    @Override
    public CanvasWatchFaceService.Engine onCreateEngine() {
        return new MyEngine();
    }

    private class MyEngine extends CanvasWatchFaceService.Engine
    {
        static final String COLON_STRING = ":";
        //all var
        static final int MSG_UPDATE_TIME = 0;

        //offset
        float mXOffset,mYOffset;
        float mDateXOffset,mDateYOffset;
        float mCenterLineXOffset,mCenterLineYOffset;
        float mBmpXOffset, mBmpYOffset;
        float mMaxTempXOffset,mMaxTempYOffset;
        Calendar mCalendar;

        // device features
        boolean mLowBitAmbient;
        private boolean mBurnInProtection;

        // graphic objects
//        Bitmap mBackgroundBitmap;
        Bitmap mBackgroundScaledBitmap;
        Paint mHourPaint;
        Paint mMinutePaint;
        Paint mBackgroundPaint;
        Paint mColonPaint;
        Paint mDatePaint;
        Paint mCentreLinePaint;
        Paint mMaxTempPaint;
        Paint mMinTempPaint;



        //Colors
        private int mWatchHourColor;
        private int mWatchMinuteColor;

        //Br Receiver
        private boolean mRegisteredTimeZoneReceiver = false;

        // handler to update the time once a second in interactive mode
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                    - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler
                                    .sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        // receiver to update the time zone
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };


        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            configureSystemUI();
            initializeWatchFaceElements();
            sp = getSharedPreferences(getString(R.string.sp_name),Context.MODE_PRIVATE);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                mHourPaint.setAntiAlias(antiAlias);
                mMinutePaint.setAntiAlias(antiAlias);
//                mSecondPaint.setAntiAlias(antiAlias);
//                mTickPaint.setAntiAlias(antiAlias);
            }
            invalidate();
            updateTimer();
        }



        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);

            mCalendar.setTimeInMillis(System.currentTimeMillis());

            int width = bounds.width();
            int height = bounds.height();

            //draw background
            canvas.drawRect(0,0,width,height,mBackgroundPaint);

            boolean is24Hour = DateFormat.is24HourFormat(DigitalWatchFace.this);

            //draw hour
            float x = mXOffset;float y = mYOffset;
            String hourString;
            if (is24Hour) {
                hourString = Constants.formatTwoDigitNumber(mCalendar.get(Calendar.HOUR_OF_DAY));
            } else {
                int hour = mCalendar.get(Calendar.HOUR);
                if (hour == 0) {
                    hour = 12;
                }
                hourString = String.valueOf(hour);
            }
            canvas.drawText(hourString, x, mYOffset, mHourPaint);
            //draw colon
            x += mHourPaint.measureText(hourString);
            canvas.drawText(COLON_STRING, x, mYOffset, mColonPaint);
            x += mColonPaint.measureText(COLON_STRING);

            //draw minute
            String minuteString = Constants.formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE));
            canvas.drawText(minuteString, x, mYOffset, mMinutePaint);

            //draw date

            String dateString = "Fri,5 JUL 2016";
            int year = mCalendar.get(Calendar.YEAR);
            String month = mCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            String day = mCalendar.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.LONG, Locale.getDefault());
            int date = mCalendar.get(Calendar.DATE);
            dateString = day.substring(0,3)
                    .concat(", ")
                    .concat(month.substring(0,3)+" ")
                    .concat(String.valueOf(date)+" ")
                    .concat(String.valueOf(year))
                    .toUpperCase();
            canvas.drawText(dateString,mDateXOffset,mDateYOffset,mDatePaint);

            //draw 1dp line
            canvas.drawLine(mCenterLineXOffset,mCenterLineYOffset,mCenterLineXOffset+60,mCenterLineYOffset,mDatePaint);

            //draw bitmap os weather
            int weatherId = sp.getInt(getString(R.string.weather_id),-1);
            if(weatherId!=-1)
            {
                int iconId = Constants.getIconResourceForWeatherCondition(weatherId);
                Bitmap bmp = BitmapFactory.decodeResource(getResources(),iconId);
                Bitmap weatherBmp = Bitmap.createScaledBitmap(bmp,60,60,false);
                canvas.drawBitmap(weatherBmp, mBmpXOffset, mBmpYOffset,null);
            }

            //draw max temp
            float tempXOffset = mMaxTempXOffset;

            Float maxTemp = sp.getFloat(getString(R.string.max_temp),0);
            String maxTempText = Constants.formatTemperature(getApplicationContext(), maxTemp);
            canvas.drawText(maxTempText,tempXOffset,mMaxTempYOffset,mMaxTempPaint);

            //draw min temp
            tempXOffset+=mMaxTempPaint.measureText(maxTempText)+15f;
            Float minTemp = sp.getFloat(getString(R.string.min_temp),0);
            String minTempText = Constants.formatTemperature(getApplicationContext(), minTemp);
            canvas.drawText(minTempText,tempXOffset,mMaxTempYOffset,mMinTempPaint);


        }


        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible and
            // whether we're in ambient mode, so we may need to start or stop the timer
            updateTimer();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION,
                    false);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            Resources resources = DigitalWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);
            float amPmSize = resources.getDimension(isRound
                    ? R.dimen.digital_am_pm_size_round : R.dimen.digital_am_pm_size);
            mHourPaint.setTextSize(textSize);
            mMinutePaint.setTextSize(textSize);
            mColonPaint.setTextSize(textSize);
            Log.d(TAG,"mXoffset = "+mXOffset);
        }

        void initializeWatchFaceElements()
        {
            /* Set defaults for colors */
            mWatchHourColor = Color.WHITE;
            mWatchMinuteColor = Color.RED;

            // create graphic styles
            initPaints();

            // allocate a Calendar to calculate local time using the UTC time and time zone
            mCalendar = Calendar.getInstance();

            //offsets
            initOffsets();
        }


        //TIMER====
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        //Broadcast Receiver Reg and Unreg
        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            DigitalWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            DigitalWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        void configureSystemUI()
        {
            mCalendar = Calendar.getInstance();

            // configure the system UI
            setWatchFaceStyle(new WatchFaceStyle.Builder(DigitalWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle
                            .BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

//            mBackgroundBitmap = BitmapFactory.decodeResource(getResources(),Color.BLUE);

        }
        private void initOffsets()
        {
            Resources resources = DigitalWatchFace.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);
            mDateXOffset = resources.getDimension(R.dimen.digital_date_x_offset);
            mDateYOffset = resources.getDimension(R.dimen.digital_date_y_offset);
            mCenterLineXOffset = resources.getDimension(R.dimen.digital_center_line_x_offset);
            mCenterLineYOffset = resources.getDimension(R.dimen.digital_center_line_y_offset);
            mBmpXOffset = resources.getDimension(R.dimen.digital_weather_x_offset);
            mBmpYOffset = resources.getDimension(R.dimen.digital_weather_y_offset);
            mMaxTempXOffset = resources.getDimension(R.dimen.digital_max_temp_x_offset);
            mMaxTempYOffset = resources.getDimension(R.dimen.digital_max_temp_y_offset);
        }

        private void initPaints()
        {
            mHourPaint = Constants.createTextPaint((ContextCompat.getColor(getApplicationContext(),R.color.white)));
            mColonPaint = Constants.createTextPaint((ContextCompat.getColor(getApplicationContext(),R.color.white)));
            mMinutePaint = Constants.createTextPaint((ContextCompat.getColor(getApplicationContext(),R.color.white)),Constants.LIGHT_TYPEFACE);

            mDatePaint = Constants.createTextPaint((ContextCompat.getColor(getApplicationContext(),R.color.white)));
            mDatePaint.setTextSize(getResources().getDimension(R.dimen.digital_date_text_size));
            mDatePaint.setAlpha(getResources().getInteger(R.integer.date_alpha));

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(ContextCompat.getColor(getApplicationContext(),R.color.light_blue));

            mMaxTempPaint = Constants.createTextPaint((ContextCompat.getColor(getApplicationContext(),R.color.white)));
            mMaxTempPaint.setTextSize(getResources().getDimension(R.dimen.digital_temp_size));

            mMinTempPaint = Constants.createTextPaint((ContextCompat.getColor(getApplicationContext(),R.color.white)),Constants.LIGHT_TYPEFACE);
            mMinTempPaint.setTextSize(getResources().getDimension(R.dimen.digital_temp_size));
            mMinTempPaint.setAlpha(getResources().getInteger(R.integer.date_alpha));

        }




    }

}
