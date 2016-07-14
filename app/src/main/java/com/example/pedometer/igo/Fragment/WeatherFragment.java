package com.example.pedometer.igo.Fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.pedometer.igo.R;
import com.example.pedometer.igo.Service.AutoUpdateService;
import com.example.pedometer.igo.Utils.HttpCallbackListener;
import com.example.pedometer.igo.Utils.HttpUtil;
import com.example.pedometer.igo.Utils.Utility;


public class WeatherFragment extends Fragment {
    private Button switchCity;
    private Button refreshWeather;
    private LinearLayout weatherInfoLayout;
    private TextView cityNameText;
    private TextView publishText;
    private TextView weatherDespText;
    private TextView temp1Text;
    private TextView temp2Text;
    private TextView currentDateText;
    private static final String TAG = "Pedometer";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.weather_layout,container,false);
        weatherInfoLayout = (LinearLayout) rootView.findViewById(R.id.weather_info_layout);
        cityNameText = (TextView)rootView.findViewById(R.id.city_name);
        publishText = (TextView) rootView.findViewById(R.id.publish_text);
        weatherDespText = (TextView)rootView.findViewById(R.id.weather_desp);
        temp1Text = (TextView)rootView.findViewById(R.id.temp1);
        temp2Text = (TextView)rootView.findViewById(R.id.temp2);
        currentDateText = (TextView)rootView.findViewById(R.id.current_date);
        switchCity=(Button)rootView.findViewById(R.id.switch_city);
        refreshWeather=(Button)rootView.findViewById(R.id.refresh_weather);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume_weather");
        switchCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseAreaFragment chooseAreaFragment = new ChooseAreaFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("from_weather_fragment",true);
                chooseAreaFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container,  chooseAreaFragment).commit();
            }
        });

        refreshWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishText.setText("同步中...");
                String weatherCode = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("weather_code","");
                if(!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String countyCode = "";
        if(getArguments()!=null) {
            countyCode = getArguments().getString("county_code");
        }
        Log.d(TAG, "getArguments()");
        if(!TextUtils.isEmpty(countyCode)) {
            publishText.setText("同步中...");
            Log.d(TAG, "同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            Log.d(TAG, " queryWeatherCodebefore");
            queryWeatherCode(countyCode);
        } else{
            Log.d(TAG, "showWeather()");
            showWeather();
        }
    }

    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
        queryFromServer(address,"countyCode");
    }

    private void queryWeatherInfo(String weatherCode) {
        String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
        queryFromServer(address, "weatherCode");
    }

    private void queryFromServer(final String address,final String type) {
        HttpUtil.sendHttpRequest(getActivity(), address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            Log.d(TAG, "queryWeatherInfo");
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    Log.d(TAG, "handleWeatherResponse");
                    Utility.handleWeatherResponse(getActivity(), response);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        }, Request.Method.GET);
    }
    private void showWeather() {
        Log.d(TAG, "showWeather");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        cityNameText.setText(prefs.getString("city_name",""));
        temp1Text.setText(prefs.getString("temp1",""));
        temp2Text.setText(prefs.getString("temp2",""));
        weatherDespText.setText(prefs.getString("weather_desp",""));
        publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
        currentDateText.setText(prefs.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        Intent intent = new Intent(getActivity(), AutoUpdateService.class);
        getActivity().startService(intent);
    }
}
