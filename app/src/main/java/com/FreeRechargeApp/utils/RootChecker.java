package com.FreeRechargeApp.utils;

import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class RootChecker {

    public static boolean isDeviceRooted() {
        return checkBuildTags()
                || checkSuPaths()
                || checkMagisk()
                || canRunSuCommand();
    }

    private static boolean checkBuildTags() {
        String tags = Build.TAGS;
        return tags != null && tags.contains("test-keys");
    }

    private static boolean checkSuPaths() {
        String[] paths = {
                "/system/bin/su",
                "/system/xbin/su",
                "/sbin/su",
                "/system/su",
                "/system/bin/.ext/.su",
                "/system/app/Superuser.apk",
                "/data/local/su",
                "/data/local/bin/su",
                "/data/local/xbin/su"
        };

        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean checkMagisk() {
        String[] magiskPaths = {
                "/sbin/magisk",
                "/data/adb/magisk",
                "/cache/magisk.log",
                "/system/app/Magisk.apk"
        };

        for (String path : magiskPaths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean canRunSuCommand() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
