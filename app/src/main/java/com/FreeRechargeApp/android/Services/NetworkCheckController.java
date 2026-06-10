package com.FreeRechargeApp.android.Services;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.FreeRechargeApp.NoInternetActivity;

public class NetworkCheckController extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (context != null && intent != null) {
                if (!isInternetConnected(context)) {
                    Intent intent1 = new Intent(context,
                            NoInternetActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent1);
                    ((Activity) context).finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // or handle the exception as needed
            Log.e("Code Error", "Error: " + e.getMessage());
        }
    }

    public static boolean isInternetConnected(Context context) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager)
                                context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    Network network = connectivityManager.getActiveNetwork();
                    if (network != null) {
                        NetworkCapabilities capabilities =
                                connectivityManager.getNetworkCapabilities(network);
                        return capabilities != null &&

                                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||

                                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
                    }
                }
            } else {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager)
                                context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    NetworkInfo networkInfo =
                            connectivityManager.getActiveNetworkInfo();
                    return networkInfo != null && networkInfo.isConnected();
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // or handle the exception as needed
            Log.e("Code Error", "Error: " + e.getMessage());
        }

        return false;


    }
}
