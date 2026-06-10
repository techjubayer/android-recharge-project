package com.FreeRechargeApp.android.Modules;

import com.google.gson.annotations.SerializedName;

public class ModuleFirebase {
    @SerializedName("dialogImgUrl")
    String dialogImgUrl;

    @SerializedName("dialogMessage")
    String dialogMessage;

    @SerializedName("dialogTitle")
    String dialogTitle;

    @SerializedName("rootUrl")
    String rootUrl;

    @SerializedName("isDialogCancelable")
    Boolean isDialogCancelable;

    public ModuleFirebase(String dialogImgUrl, String dialogMessage, String dialogTitle, Boolean isDialogCancelable, String rootUrl) {

        this.dialogImgUrl = dialogImgUrl;
        this.dialogMessage = dialogMessage;
        this.dialogTitle = dialogTitle;
        this.isDialogCancelable = isDialogCancelable;
        this.rootUrl = rootUrl;
    }



    public String getRootUrl() {
        return rootUrl;
    }

    public String getDialogImgUrl() {
        return dialogImgUrl;
    }

    public String getDialogMessage() {
        return dialogMessage;
    }

    public String getDialogTitle() {
        return dialogTitle;
    }

    public Boolean getDialogCancelable() {
        return isDialogCancelable;
    }
}
