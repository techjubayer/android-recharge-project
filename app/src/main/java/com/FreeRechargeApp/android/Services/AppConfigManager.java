package com.FreeRechargeApp.android.Services;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.FreeRechargeApp.android.Modules.ModuleFirebase;

import java.lang.reflect.Type;

public class AppConfigManager {

    //Api Alert Dialog Function-----------------START
    public static void storeFirebaseAppConfig(Context context, ModuleFirebase moduleFirebase) {
        SharedPreferences sp = context.getSharedPreferences("AlertDialogDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        Gson gson = new Gson();
        String jsonString = gson.toJson(moduleFirebase);

        editor.putString("AlertDialogDetails", jsonString);
        editor.apply();
    }

    public static ModuleFirebase getFirebaseAppConfig(Context context) {
        Gson gson = new Gson();
        ModuleFirebase moduleFirebase;

        SharedPreferences sp = context.getSharedPreferences("AlertDialogDetails", Context.MODE_PRIVATE);
        if (sp.contains("AlertDialogDetails")) {
            String jsonString = sp.getString("AlertDialogDetails", "");
            Type type = new TypeToken<ModuleFirebase>() {
            }.getType();
            moduleFirebase = gson.fromJson(jsonString, type);
        } else {
            moduleFirebase = null;
        }


        return moduleFirebase;
    }
    //Api Alert Dialog Function-----------------END



// 🔥 Cache expiry time (48h control)
    public static void storeCacheTime(Context context, long time) {
        SharedPreferences sp = context.getSharedPreferences("AlertDialogDetails", Context.MODE_PRIVATE);
        sp.edit().putLong("cache_time", time).apply();
    }

    public static long getCacheTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences("AlertDialogDetails", Context.MODE_PRIVATE);
        return sp.getLong("cache_time", 0);
    }

    // 🔥 Clear full Firebase cache
    public static void clearFirebaseCache(Context context) {
        SharedPreferences sp = context.getSharedPreferences("AlertDialogDetails", Context.MODE_PRIVATE);
        sp.edit()
                .remove("AlertDialogDetails")
                .remove("cache_time")
                .apply();
    }
}
