package com.example.pedometer.igo.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.VoiceInteractor;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.pedometer.igo.Receiver.AutoUpdateReceiver;
import com.example.pedometer.igo.Utils.HttpCallbackListener;
import com.example.pedometer.igo.Utils.HttpUtil;
import com.example.pedometer.igo.Utils.Utility;

public class AutoUpdateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                    updateWeather();
            }
        }).start();
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int anHour = 8*60*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
        Intent i = new Intent(this,AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,i,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent,flags,startId);
    }

    private void updateWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = preferences.getString("weather_code", "");
        String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
        HttpUtil.sendHttpRequest(this, address, new HttpCallbackListener() {
            @Override
            public void onFinish(String str) {
                Utility.handleWeatherResponse(AutoUpdateService.this,str);
            }
            @Override
            public void onError(VolleyError volleyError) {
                volleyError.printStackTrace();
            }
        }, Request.Method.GET);
    }
}
