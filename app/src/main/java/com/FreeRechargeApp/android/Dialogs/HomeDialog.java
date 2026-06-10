package com.FreeRechargeApp.android.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.FreeRechargeApp.android.Modules.ModuleFirebase;
import com.FreeRechargeApp.R;

import java.util.Objects;

public class HomeDialog {

    static Dialog dialog;

    public static void setUpHomeDialog(Context context, ModuleFirebase moduleAlertDialog) {


        String dialogImgUrl = moduleAlertDialog.getDialogImgUrl();
        Boolean isDialogCancelable = moduleAlertDialog.getDialogCancelable();
        String dialogMessage = moduleAlertDialog.getDialogMessage();
        String dialogTitle = moduleAlertDialog.getDialogTitle();

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_home_message);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.dr_bg_mystyle_white));
        dialog.setCancelable(isDialogCancelable);
        dialog.show();

        TextView txtTitleHD = dialog.findViewById(R.id.txtTitleHD);
        TextView txtDscrptnHD = dialog.findViewById(R.id.txtDscrptnHD);
        ImageView imageViewHD = dialog.findViewById(R.id.imageViewHD);
        ImageButton close = dialog.findViewById(R.id.btnClose);

        txtTitleHD.setText(dialogTitle);

        if (!dialogMessage.isEmpty()) {
            txtDscrptnHD.setText(dialogMessage);
        } else {
            txtDscrptnHD.setVisibility(View.GONE);
        }

        if (!dialogImgUrl.isEmpty()) {
            Glide.with(context).load(dialogImgUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageViewHD);
        } else {
            imageViewHD.setVisibility(View.GONE);
        }


        if (!isDialogCancelable) {
            close.setVisibility(View.GONE);
        }
        close.setOnClickListener(view1 -> {
            dialog.dismiss();
        });



    }


}
