package com.FreeRechargeApp.android.Modules;


import com.google.gson.annotations.SerializedName;

public class ModuleContact {

    @SerializedName("name")
    public String name;
    @SerializedName("number")
    public String number;


    public void setName(String name) {
        this.name = name;
    }

    public void setMobileNumber(String mobileNumber) {
        this.number = mobileNumber;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }
}
