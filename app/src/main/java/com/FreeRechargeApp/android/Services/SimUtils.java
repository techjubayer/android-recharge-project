package com.FreeRechargeApp.android.Services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import androidx.core.app.ActivityCompat;

import com.FreeRechargeApp.android.Modules.SimDetails;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SimUtils {

    public static List<SimDetails> getSimDetails(Context context) {

        List<SimDetails> simList = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
        ) != PackageManager.PERMISSION_GRANTED) {
            return simList;
        }

        SubscriptionManager manager =
                (SubscriptionManager) context.getSystemService(
                        Context.TELEPHONY_SUBSCRIPTION_SERVICE
                );

        if (manager != null) {

            List<SubscriptionInfo> list = manager.getActiveSubscriptionInfoList();

            if (list != null) {

                for (SubscriptionInfo info : list) {

                    SimDetails sim = new SimDetails();

                    sim.simSlotIndex = info.getSimSlotIndex();
                    sim.number = info.getNumber();
                    sim.displayName = String.valueOf(info.getDisplayName());

                    simList.add(sim);
                }
            }
        }

        // Sort by SIM slot
        simList.sort(new Comparator<SimDetails>() {
            @Override
            public int compare(SimDetails a, SimDetails b) {
                return Integer.compare(a.simSlotIndex, b.simSlotIndex);
            }
        });

        return simList;
    }

}