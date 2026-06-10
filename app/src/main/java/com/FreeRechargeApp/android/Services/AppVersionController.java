package com.FreeRechargeApp.android.Services;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppVersionController {

    public static int getAppVersion(Context context) {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (info != null) {
            return info.versionCode;
        }
        return 0;
    }

    public static int getPreviousVersion(Context context) {
        SharedPreferences sp = context.getSharedPreferences("appVersion", Context.MODE_PRIVATE);
        if (sp.contains("appVersion")) {
            return sp.getInt("appVersion", 0);
        }
        return 0;
    }

    public static void storeAppVersion(Context context, int appVersion){
        SharedPreferences sp = context.getSharedPreferences("appVersion", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putInt("appVersion", appVersion);
        editor.apply();
    }
}
