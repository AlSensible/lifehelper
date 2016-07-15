package com.example.pedometer.igo.Utils.Preferences;

import android.content.SharedPreferences;

import com.example.pedometer.igo.Utils.Utils;

/**
 * Created by vvv98 on 2016/5/28.
 */
public class PedometerSettings {
    SharedPreferences mSettings;
    public static int M_NONE =1;
    public static int M_PACE =2;
    public static int M_SPEED=3;

    public PedometerSettings(SharedPreferences settings) {
        mSettings = settings;
    }

    public boolean isMetric() {return mSettings.getString("units","imperial").equals("metric");}

    public float getStepLength() {
        try{
            return Float.valueOf(mSettings.getString("step_length","20").trim());
        }
        catch (NumberFormatException e) {
            return 0f;
        }
    }
