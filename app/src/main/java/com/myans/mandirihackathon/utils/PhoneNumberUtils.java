package com.myans.mandirihackathon.utils;

import android.util.Log;

import com.myans.mandirihackathon.interfaces.Const;

public class PhoneNumberUtils {

    public static final int TELKOMSEL = 1;
    public static final int XL = 2;
    public static final int THREE= 3;
    public static final int UNKNOWN= 4;

    public static int getProvider(String phoneNumber){
        String startNumber = phoneNumber.substring( 0 , 4 );
        Log.d(Const.TAG, "provider number: " + startNumber);
        if(startNumber.equals("0811") || startNumber.equals("0812") ||
                startNumber.equals("0813") || startNumber.equals("0821") ||
                startNumber.equals("0822") || startNumber.equals("0823") ||
                startNumber.equals("0852") || startNumber.equals("0851") ){
            return TELKOMSEL;
        }
        else if(startNumber.equals("0817") || startNumber.equals("0818") ||
                startNumber.equals("0819") || startNumber.equals("0859") ||
                startNumber.equals("0877") || startNumber.equals("0878") ){
            return XL;
        }

        else if(startNumber.equals("0896") || startNumber.equals("0897") ||
                startNumber.equals("0898") || startNumber.equals("0899") ){
            return THREE;
        }
        else
            return UNKNOWN;
    }

    public static String getProviderName(int providerCode){
        if(providerCode == TELKOMSEL)
            return "telkomsel";
        else if(providerCode == XL)
            return "XL";
        else return "tri";
    }

}
