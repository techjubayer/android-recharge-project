package com.FreeRechargeApp.android.Services;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.FreeRechargeApp.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyPublicClass {

    @SuppressLint("StaticFieldLeak")
    static Context context;

    public MyPublicClass(Context context) {
        MyPublicClass.context = context;
    }

    //Password hashing setup
    public static String hashingAlgo(String pass) {
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            digester.update(pass.getBytes());
            byte[] hashByteArr = digester.digest();
            StringBuilder hashpass = new StringBuilder();
            for (byte a : hashByteArr) {
                hashpass.append(String.format("%02x", a));
            }

            return hashpass.toString();

        } catch (NoSuchAlgorithmException e) {
//            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
            return null;
        }
    }


    //To gen checksum
    public static String hashingAlgo2(String pass) {
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA-384");
            digester.update(pass.getBytes());
            byte[] hashByteArr = digester.digest();
            StringBuilder hashpass = new StringBuilder();
            for (byte a : hashByteArr) {
                hashpass.append(String.format("%02x", a));
            }

            return hashpass.toString();

        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    //To generate
    public static String genCheckSumSecreteKey() {
        try {
            long time = System.currentTimeMillis();
            String stringMsTime = String.valueOf(time);
            String stringMsTime2 = stringMsTime.substring(0, stringMsTime.length() - 5) + "00000";
            long mySecretNumber = Long.parseLong("254841");
            long time2 = Long.parseLong(stringMsTime2) + mySecretNumber;
            return String.valueOf(time2);
        } catch (Exception e) {
            e.printStackTrace(); // or handle the exception as needed
            Log.e("Code Error", "Error: " + e.getMessage());
            return null;
        }
    }

    public static void setClipboard(Context context, String text) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    //To gen checksum
    private static String hashingAlgo3(String pass) {
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA-256");
            digester.update(pass.getBytes());
            byte[] hashByteArr = digester.digest();
            StringBuilder hashpass = new StringBuilder();
            for (byte a : hashByteArr) {
                hashpass.append(String.format("%02x", a));
            }

            return hashpass.toString();

        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String getFirstNthStrings(String str, int n) {
        String[] sArr = str.split(" ");
        StringBuilder firstStrs = new StringBuilder();
        for (int i = 0; i < n; i++)
            firstStrs.append(sArr[i]).append(" ");
        return firstStrs.toString().trim();
    }

    public static Boolean isValidPass(String pass) {
        if (!pass.isEmpty()) {
            return pass.length() >= 6;
        } else {
            return false;
        }
    }



    public static void showMessageDialog(Context context, String title, String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setTitle(title);
        builder1.setMessage(message);
        builder1.setCancelable(true);

        builder1.setNegativeButton(
                "Ok",
                (dialog, id) -> dialog.cancel());

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public static void showMyCustomDialog(Context context, String title, String message) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.my_dialog_layout);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.dr_bg_mystyle_white));
        dialog.setCancelable(true);
        dialog.show();

        TextView txtTitle = dialog.findViewById(R.id.dialog_title);
        TextView txtMessage = dialog.findViewById(R.id.dialog_message);

        txtTitle.setText(title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txtMessage.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT));
        } else {
            txtMessage.setText(Html.fromHtml(message));
        }
    }


    public static boolean isValidMobileNo(String str) {
        if (str == null) {
            return false;
        }
        Pattern ptrn = Pattern.compile("(0/91)?[6-9][0-9]{9}");
        Matcher match = ptrn.matcher(str);
        return (match.find() && match.group().equals(str));
    }

    @SuppressLint("ResourceType")
    public static void toastShow(Context context, String message, int LENGTH) {
        if (context != null) {
            Toast toast = Toast.makeText(context, message, LENGTH);

            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View toastView = li.inflate(R.layout.toast, null);

            TextView text = (TextView) toastView.findViewById(R.id.txtToast);
            text.setText(message);
            text.setTextColor(context.getResources().getColor(R.color.white));
            toast.setView(toastView);
            toastView.setBackgroundResource(R.drawable.dr_bg_mystyle_color);
            toast.show();
        }else{
            Log.e("Code Error", "Context is null");
        }
    }


}
