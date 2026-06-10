package com.FreeRechargeApp.android.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.FreeRechargeApp.android.Modules.ModuleContact;

import java.util.ArrayList;
import java.util.List;

public class ContactListManager {
    Context context;

    public ContactListManager(Context context) {
        this.context = context;
    }

    public boolean hasContactPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public List<ModuleContact> getUserContactList() {
        List<ModuleContact> data = new ArrayList<>();

        Cursor getPhoneNumber = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (getPhoneNumber != null) {
            if (getPhoneNumber.getCount() == 0) {
                Toast.makeText(context, "No contacts in your contact list.", Toast.LENGTH_LONG).show();
            }

            while (getPhoneNumber.moveToNext()) {
                @SuppressLint("Range") String name = getPhoneNumber.getString(
                        getPhoneNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                @SuppressLint("Range") String phoneNumber = getPhoneNumber.getString(
                                getPhoneNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        .trim()
                        .replace("+91", "")
                        .replace(" ", "");

                ModuleContact moduleContact = new ModuleContact();
                moduleContact.setMobileNumber(phoneNumber);
                moduleContact.setName(name);
                data.add(moduleContact);
            }
            getPhoneNumber.close();
        }

        return data;
    }
}

