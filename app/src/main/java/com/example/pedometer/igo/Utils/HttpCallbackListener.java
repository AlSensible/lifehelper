package com.example.pedometer.igo.Utils;

import com.android.volley.VolleyError;

public interface HttpCallbackListener {
    void onFinish(String str);
    void onError(VolleyError volleyError);
}
