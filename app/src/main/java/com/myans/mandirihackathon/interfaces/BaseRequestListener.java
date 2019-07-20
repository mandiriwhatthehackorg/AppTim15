package com.myans.mandirihackathon.interfaces;

import org.json.JSONObject;

public interface BaseRequestListener {
    void onRequestFinish(String requestUrl, JSONObject param, String message);
}
