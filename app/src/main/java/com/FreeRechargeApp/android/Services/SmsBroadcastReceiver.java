package com.FreeRechargeApp.android.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public interface OtpListener {
        void onOtpReceived(String otp);
    }

    private static OtpListener listener;

    public static void bindListener(OtpListener otpListener) {
        listener = otpListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {

            Bundle extras = intent.getExtras();
            if (extras == null) return;

            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            if (status == null) return;

            switch (status.getStatusCode()) {

                case CommonStatusCodes.SUCCESS:

                    String message =
                            (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    Log.d("OTP_MESSAGE", message);

                    if (message != null) {
                        String otp = extractOtp(message);
                        if (listener != null && otp != null) {
                            listener.onOtpReceived(otp);
                        }
                    }

                    break;

                case CommonStatusCodes.TIMEOUT:
                    Log.d("OTP", "OTP Retriever Timeout");
                    break;
            }
        }
    }

    private String extractOtp(String message) {

        Pattern pattern = Pattern.compile("\\b\\d{4,6}\\b");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return matcher.group(0);
        }

        return null;
    }
}