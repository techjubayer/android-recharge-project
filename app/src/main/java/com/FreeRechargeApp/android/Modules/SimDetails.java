package com.FreeRechargeApp.android.Modules;

import com.google.gson.annotations.SerializedName;

public class SimDetails {

    @SerializedName("simSlotIndex")
    public int simSlotIndex;

    @SerializedName("number")
    public String number;

    @SerializedName("displayName")
    public String displayName;

    public int getSimSlotIndex() {
        return simSlotIndex;
    }

    public void setSimSlotIndex(int simSlotIndex) {
        this.simSlotIndex = simSlotIndex;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}