package com.myans.mandirihackathon.base_class;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.myans.mandirihackathon.MainActivity;

public class BaseSharedPref {
    public static final String SHARED ="shared";
    public static final String URL ="url";
    private Context context = null;

    private static SharedPreferences sp;
    private static SharedPreferences.Editor spEditor;



    public static void saveSPString(Context context, String keySP, String value){
        sp = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        spEditor = sp.edit();
        spEditor.putString(keySP, value);
        spEditor.commit();
    }


    public static String getSpString(Context context, String keySP){
        sp = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        spEditor = sp.edit();
        return sp.getString(keySP, "");
    }


    public static void logout(Context context)
    {
        sp = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        spEditor = sp.edit();
        spEditor.clear();
        spEditor.commit();
        Toast.makeText(context, "Berhasil Keluar", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void forceLogout(Context context)
    {
        sp = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
        spEditor = sp.edit();
        spEditor.clear();
        spEditor.commit();
        Toast.makeText(context, "Akun anda login di perangkat lain", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
