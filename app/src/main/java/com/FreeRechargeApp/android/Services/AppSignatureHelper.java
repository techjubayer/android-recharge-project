package com.FreeRechargeApp.android.Services;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import java.security.MessageDigest;
import java.util.Arrays;



//This class only for generate the otp sms hash key generate
public class AppSignatureHelper {


    public static String getAppHash(Context context) {

        try {

            String packageName = context.getPackageName();

            Signature[] signatures =
                    context.getPackageManager()
                            .getPackageInfo(packageName,
                                    PackageManager.GET_SIGNATURES).signatures;

            String appInfo = packageName + " " + signatures[0].toCharsString();

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(appInfo.getBytes());

            byte[] hash = Arrays.copyOfRange(md.digest(), 0, 9);

            String base64Hash =
                    Base64.encodeToString(hash,
                            Base64.NO_PADDING | Base64.NO_WRAP);

            return base64Hash.substring(0, 11);

        } catch (Exception e) {
            return null;
        }
    }
}