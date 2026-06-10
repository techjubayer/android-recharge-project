package com.FreeRechargeApp.android.Services;

import android.content.Context;
import android.content.SharedPreferences;


public class AddMoney {
    public static void storeAddMoneyResponseURl(Context context, String responseUrl) {
        String addMoneySpKey = "AddMoneyCredential";
        SharedPreferences sp = context.getSharedPreferences(addMoneySpKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("responseUrl", responseUrl);
        editor.apply();
    }

    public static String getAddMoneyResponseURl(Context context) {
        String addMoneySpKey = "AddMoneyCredential";
        SharedPreferences sp = context.getSharedPreferences(addMoneySpKey, Context.MODE_PRIVATE);
        if (sp.contains("responseUrl")) {
            return sp.getString("responseUrl", "");
        } else {
            return null;
        }
    }
}
