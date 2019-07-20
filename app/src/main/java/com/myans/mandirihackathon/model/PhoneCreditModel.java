package com.myans.mandirihackathon.model;

import android.util.Log;

import com.myans.mandirihackathon.interfaces.Const;

public class PhoneCreditModel {
    private String amount;
    private String productType;
    private String phoneNumber;

    public PhoneCreditModel() {
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void debug(){
        Log.d(Const.TAG, "debug amount: " + getAmount());
        Log.d(Const.TAG, "debug phone number: " + getPhoneNumber());
    }
}
