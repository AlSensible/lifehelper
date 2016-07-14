package com.example.pedometer.igo.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.pedometer.igo.Db.WeatherDB;
import com.example.pedometer.igo.Model.City;
import com.example.pedometer.igo.Model.County;
import com.example.pedometer.igo.Model.Province;
import com.example.pedometer.igo.R;
import com.example.pedometer.igo.Utils.HttpCallbackListener;
import com.example.pedometer.igo.Utils.HttpUtil;
import com.example.pedometer.igo.Utils.Utility;


import java.util.ArrayList;
import java.util.List;

public class ChooseAreaFragment extends Fragment {
    @Nullable
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private WeatherDB weatherDB;
    private List<String> dataList = new ArrayList<String>();

    private List<Province> provinceList;

    private List<City> cityList;

    private List<County> countyList;

    private Province selectedProvince;

    private City selectedCity;

    private County selectedCounty;
    private boolean isFromWeatherFragment=false;
    private int currentLevel;
    private static final String TAG = "Pedometer";
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.choose_area,container,false);
        listView = (ListView) rootView.findViewById(R.id.list_view);
        titleText = (TextView) rootView.findViewById(R.id.title_text);
        weatherDB = WeatherDB.getInstance(getActivity());
        return rootView;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(getArguments()!=null){
            isFromWeatherFragment=getArguments().getBoolean("from_weather_fragment",false);
        }
        if(prefs.getBoolean("city_selected",false)&& !isFromWeatherFragment) {
            WeatherFragment weatherFragment = new WeatherFragment();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container,weatherFragment).commit();
        }
        adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if(currentLevel == LEVEL_COUNTY) {
                    String countyCode = countyList.get(position).getCountyCode();
                    WeatherFragment weatherFragment = new WeatherFragment();
                    Log.d(TAG, "new Bundle()");
                    Bundle bundle = new Bundle();
                    bundle.putString("county_code", countyCode);
                    weatherFragment.setArguments(bundle);
                    Log.d(TAG, "replace");
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container,weatherFragment).commit();
                }
            }
        });
        queryProvinces();
    }
    private void queryProvinces() {
        provinceList = weatherDB.loadProvinces();
        if(provinceList.size()>0) {
            dataList.clear();
            for(Province province:provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(null,"province");
        }
    }

    private void queryCities() {
        cityList = weatherDB.loadCities(selectedProvince.getId());
        if(cityList.size()>0) {
            dataList.clear();
            for(City city:cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }
    private void queryCounties() {
        countyList = weatherDB.loadCounties(selectedCity.getId());
        if(countyList.size()>0) {
            dataList.clear();
            for(County county:countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }

    private void queryFromServer(final String code,final String type) {
        String address;
        if(!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
        } else {
            address =  "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(getActivity(),address, new HttpCallbackListener(){
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if("province".equals(type)) {
                    result = Utility.handleProvincesResponse(weatherDB,response);
                }else if("city".equals(type)) {
                    result = Utility.handleCitiesResponse(weatherDB, response,selectedProvince.getId());
                }else if("county".equals(type)) {
                    result = Utility.handleCountiesResponse(weatherDB,response,selectedCity.getId());
                }
                if(result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

         @Override
          public void onError(VolleyError volleyError) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    });
              }
        }, Request.Method.GET);
    }

    private void showProgressDialog() {
        if(progressDialog==null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if(progressDialog!=null) {
            progressDialog.dismiss();
        }
    }

/*
    @Override
    public void onBackPressed() {
        if(currentLevel==LEVEL_COUNTY) {
            queryCities();
        } else if(currentLevel==LEVEL_CITY) {
            queryProvinces();
        } else {
            getActivity().finish();
        }
    }
*/

}
