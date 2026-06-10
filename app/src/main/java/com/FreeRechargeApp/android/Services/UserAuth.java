package com.FreeRechargeApp.android.Services;


import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.FreeRechargeApp.android.Modules.ModuleUserDetails;


import java.lang.reflect.Type;

public class UserAuth {


    public static ModuleUserDetails getUserDetails(Context context) {
        Gson gson = new Gson();
        ModuleUserDetails moduleUserDetails = null;

        try {
            if (context == null) {
                return null;
            }

            SharedPreferences sp = context.getSharedPreferences("userDetails", Context.MODE_PRIVATE);
            if (sp.contains("userDetails")) {
                String jsonString = sp.getString("userDetails", "");
                if (!jsonString.isEmpty()) {
                    Type type = new TypeToken<ModuleUserDetails>() {
                    }.getType();
                    moduleUserDetails = gson.fromJson(jsonString, type);
                } else {
                    moduleUserDetails = null;
                }

            } else {
                moduleUserDetails = null;
            }
        } catch (Exception e) {
            moduleUserDetails = null;
        }


        return moduleUserDetails;
    }


    public static void clearUserDetails(Context context) {
        SharedPreferences sp = context.getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    public static void storeUserDetails(Context context, ModuleUserDetails moduleUserDetails) {
        SharedPreferences sp = context.getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        Gson gson = new Gson();
        String jsonString = gson.toJson(moduleUserDetails);

        editor.putString("userDetails", jsonString);
        editor.apply();
    }



}
