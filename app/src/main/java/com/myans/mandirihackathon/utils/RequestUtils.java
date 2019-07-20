package com.myans.mandirihackathon.utils;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.myans.mandirihackathon.interfaces.BaseRequestListener;
import com.myans.mandirihackathon.interfaces.Const;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequestUtils {
    public static void getAPI(final String url, int requestMethod, JSONObject param, final BaseRequestListener listener, Context context, final String token) {

        Log.d(Const.TAG, "getAPI: "+ url);
        try {
            Log.d(Const.TAG, param.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(requestMethod, url , param, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d(Const.TAG, "onResponse: " +response.toString(4));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listener.onRequestFinish(url, response, response.toString());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(Const.TAG, "onErrorResponse: " + error.getMessage());
                Log.d(Const.TAG, "onErrorResponse: " + error.getLocalizedMessage());
                Log.d(Const.TAG, "onErrorResponse: " + error.getNetworkTimeMs());
                listener.onRequestFinish(url, null, error.getMessage());
            }
        }

        ){
            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Accept", "application/json");
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        VolleyUtils.getInstance(context).addToRequestQueue(request);

    }

    public static void getToken(final BaseRequestListener listener, Context context){
        Log.d(Const.TAG, "getAPI: "+ Const.TOKEN_URL);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Const.TOKEN_URL , new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d(Const.TAG, "onResponse: " +response.toString(4));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listener.onRequestFinish(Const.TOKEN_URL, response, response.toString());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(Const.TAG, "onErrorResponse: " + error.getMessage());
                Log.d(Const.TAG, "onErrorResponse: " + error.getLocalizedMessage());
                Log.d(Const.TAG, "onErrorResponse: " + error.getNetworkTimeMs());
                listener.onRequestFinish(Const.TOKEN_URL, null, error.getMessage());
            }
        }

        ){
            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Accept", "application/json");
                String credentials = Const.USERNAME+":"+Const.PASSWORD;
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                params.put("Authorization", auth);
                return params;
            }
        };
        VolleyUtils.getInstance(context).addToRequestQueue(request);

    }
}
