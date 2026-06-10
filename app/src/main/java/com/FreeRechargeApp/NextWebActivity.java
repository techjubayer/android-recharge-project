package com.FreeRechargeApp;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.FreeRechargeApp.android.Modules.SimDetails;
import com.FreeRechargeApp.android.Services.AddMoney;
import com.FreeRechargeApp.android.Services.AppConfigManager;
import com.FreeRechargeApp.android.Services.AppSignatureHelper;
import com.FreeRechargeApp.android.Services.AppVersionController;
import com.FreeRechargeApp.android.Services.Biometric;
import com.FreeRechargeApp.android.Services.PermissionUtils;
import com.FreeRechargeApp.android.Services.SimUtils;
import com.FreeRechargeApp.android.Services.SmsBroadcastReceiver;
import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.FreeRechargeApp.android.Dialogs.HomeDialog;
import com.FreeRechargeApp.android.Modules.ModuleContact;
import com.FreeRechargeApp.android.Modules.ModuleFirebase;
import com.FreeRechargeApp.android.Modules.ModuleUserDetails;
import com.FreeRechargeApp.android.Services.ContactListManager;
import com.FreeRechargeApp.android.Services.MyPublicClass;
import com.FreeRechargeApp.android.Services.NetworkCheckController;
import com.FreeRechargeApp.android.Services.UserAuth;

import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class NextWebActivity extends AppCompatActivity {

    private WebView webView;

    private boolean hasRetried = false;
    private RelativeLayout lottieLoader;

    String rootUrl, rootEndPoint, currentUrl;
    private ValueCallback<Uri[]> uploadMessage = null;
    private final int FILE_CHOOSER_REQUEST_CODE = 800;
    private boolean isPaymentOpen = false;

    BroadcastReceiver broadcastReceiver;
    ModuleUserDetails moduleUserDetails;
    SharedPreferences spAppTheme;

    private LinearLayout mainLayout;

    ModuleFirebase firebaseModule;


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showPermissionRequiredDialog() {
        boolean shouldShowRationale = androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS) ||
                androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_PHONE_STATE) ||
                androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_PHONE_NUMBERS);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("SIM and Contact permissions are mandatory to use this app. Please grant them to continue.")
                .setCancelable(false)
                .setNegativeButton("Deny", (d, w) -> runOnUiThread(() ->
                        webView.evaluateJavascript(
                                "if (typeof onGetSimDetailsResponse === 'function') { onGetSimDetailsResponse('[]'); }",
                                null
                        )
                ));

        if (shouldShowRationale) {
            builder.setPositiveButton("Grant Permission", (d, w) -> PermissionUtils.requestSimAndContactPermission(this));
        } else {
            builder.setMessage("Permissions were permanently denied. Please enable SIM and Contact permissions from App Settings to continue.");
            builder.setPositiveButton("Open Settings", (d, w) -> {
                android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                android.net.Uri uri = android.net.Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            });
        }

        builder.show();
    }


    // Sim Number-----------
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.SIM_CONTACT_PERMISSION_CODE) {
            boolean allGranted = grantResults.length > 0;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                fetchSim();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    showPermissionRequiredDialog();
                }
            }
        }
    }


    private void fetchSim() {
        if (PermissionUtils.hasSimAndContactPermission(this)) {
            List<SimDetails> simList = SimUtils.getSimDetails(this);
            Gson gson = new Gson();
            String jsonContactList = gson.toJson(simList);
            runOnUiThread(() ->
                    webView.evaluateJavascript("if (typeof onGetSimDetailsResponse === 'function') { onGetSimDetailsResponse(" + JSONObject.quote(jsonContactList) + "); }", null)
            );
        } else {
            runOnUiThread(() ->
                    webView.evaluateJavascript("if (typeof onGetSimDetailsResponse === 'function') { onGetSimDetailsResponse('[]'); }", null)
            );
        }
    }

    // Sim Number-----------


    @SuppressLint({"SetJavaScriptEnabled", "UnspecifiedRegisterReceiverFlag", "ObsoleteSdkInt"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstOpen = prefs.getBoolean("is_first_open", true);
        if (isFirstOpen) {
            Intent intent = new Intent(NextWebActivity.this, PermissionActivity.class);
            startActivity(intent);
            finish();
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_next_web);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        spAppTheme = getSharedPreferences("app_theme", Context.MODE_PRIVATE);


        broadcastReceiver = new NetworkCheckController();

        FirebaseApp.initializeApp(NextWebActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());
        FirebaseAnalytics.getInstance(NextWebActivity.this);
        setUpPushNotification();


        firebaseModule = AppConfigManager.getFirebaseAppConfig(NextWebActivity.this);
        moduleUserDetails = UserAuth.getUserDetails(NextWebActivity.this);

        boolean isLoggedIn = moduleUserDetails != null
                && moduleUserDetails.getToken() != null
                && !moduleUserDetails.getToken().trim().isEmpty();

        rootEndPoint = isLoggedIn ? "" : "/auth/login";

        mainLayout = findViewById(R.id.main);
        rootUrl = getString(R.string.next_root_url).concat(rootEndPoint);
        webView = findViewById(R.id.webview);
        lottieLoader = findViewById(R.id.lottieLoader);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }


        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setAllowFileAccess(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);


        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                // This will print all javascript console.log and console.error messages to your Android Studio Logcat
                Log.e("WebViewConsole", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());

                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                }
                uploadMessage = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, "Choose File"), FILE_CHOOSER_REQUEST_CODE);
                return true;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                Log.d("WebError", errorResponse.toString());

                if (request.isForMainFrame()) {
                    if (!hasRetried) {
                        hasRetried = true;
                        view.postDelayed(() -> view.reload(), 1000);
                    } else {
                        Intent intent1 = new Intent(NextWebActivity.this, NoInternetActivity.class);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent1);
                        finish();
                    }
                }
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Log.d("WebError", error.toString());

                // Only handle main page request
                if (request.isForMainFrame()) {

                    if (!hasRetried) {
                        hasRetried = true;
                        view.postDelayed(() -> view.reload(), 1000);
                    } else {
                        // Already retried → go to NoInternetActivity
                        Intent intent1 = new Intent(NextWebActivity.this, NoInternetActivity.class);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent1);
                        finish();
                    }
                }
                super.onReceivedError(view, request, error);
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();


                if (
                        url.contains("mailto:") ||
                                url.contains("play.google") ||
                                url.contains("tel:") ||
                                url.contains("wa.me") ||
                                url.contains(".instagram") ||
                                url.contains(".youtube") ||
                                url.contains(".facebook") ||
                                url.contains(".telegram") ||
                                url.contains("t.me") ||
                                url.contains("x.com") ||
                                url.contains("twitter") ||
                                url.contains(".whatsapp") ||
                                url.contains(".android")
                ) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }


                if (!url.contains("http") && !url.contains("https")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }

                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, request);
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);


                view.evaluateJavascript(
                        "(function() {" +
                                "function notifyUrlChange() {" +
                                "Android.onUrlChange(window.location.href);" +
                                "}" +
                                "window.addEventListener('popstate', notifyUrlChange);" +
                                "history.pushState = ( f => function pushState(){ var ret = f.apply(this, arguments); notifyUrlChange(); return ret; })(history.pushState);" +
                                "history.replaceState = ( f => function replaceState(){ var ret = f.apply(this, arguments); notifyUrlChange(); return ret; })(history.replaceState);" +
                                "})();",
                        null
                );
                isResponsePageVisible = false;

                new Handler().postDelayed(() -> {
                    webView.setVisibility(View.VISIBLE);
                    lottieLoader.setVisibility(View.GONE);
                    hasRetried = false;
                }, 600);
            }


        });
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

            String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
            if (!fileName.endsWith(".pdf")) {
                fileName += ".pdf";
            }

            // Set proper headers
            request.setMimeType("application/pdf");
            request.addRequestHeader("User-Agent", userAgent);
            request.setDescription("Downloading PDF...");
            request.setTitle(fileName);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationUri(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)));

            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            long downloadId = downloadManager.enqueue(request);

            // Handle download completion
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (id == downloadId) {
                        Toast.makeText(NextWebActivity.this, "Download complete!", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED);
                }
            } else {
                registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            }
        });
        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isResponsePageVisible) {
                    isResponsePageVisible = false;
                    webView.loadUrl(rootUrl);
                } else {
                    String checkUrl = currentUrl != null ? currentUrl : webView.getUrl();
                    boolean isAtRootOrLogin = false;

                    if (checkUrl != null && rootUrl != null) {
                        String cleanRoot = rootUrl.endsWith("/") ? rootUrl.substring(0, rootUrl.length() - 1) : rootUrl;
                        String cleanCheck = checkUrl.endsWith("/") ? checkUrl.substring(0, checkUrl.length() - 1) : checkUrl;
                        
                        if (cleanCheck.equals(cleanRoot) || cleanCheck.equals(cleanRoot + "/auth/login")) {
                            isAtRootOrLogin = true;
                        }
                    }

                    if (isAtRootOrLogin) {
                        showExitDialog();
                    } else if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        // In SPAs, native history might be empty even if we navigated, so use JS history.back
                        webView.evaluateJavascript("history.back();", null);
                    }
                }
            }

            private void showExitDialog() {
                android.app.Dialog dialog = new android.app.Dialog(NextWebActivity.this);
                dialog.setContentView(R.layout.dialog_exit_modern);
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.setCancelable(false);

                dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
                dialog.findViewById(R.id.btnExit).setOnClickListener(v -> finishAffinity());

                dialog.show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);


        getFirebaseAppConfig();
        changeAppTheme();
        checkBiometric(moduleUserDetails);
    }


    private void fetchContacts() {
        ContactListManager contactListManager = new ContactListManager(NextWebActivity.this);
        List<ModuleContact> moduleContactList = contactListManager.getUserContactList();

        Gson gson = new Gson();
        String jsonContactList = gson.toJson(moduleContactList);
        runOnUiThread(() ->
                webView.evaluateJavascript("if (typeof onReceivedContacts === 'function') { onReceivedContacts(" + JSONObject.quote(jsonContactList) + "); }", null)
        );
    }


    private final ActivityResultLauncher<String> requestContactsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchContacts();
                } else {
                    runOnUiThread(() ->
                            webView.evaluateJavascript("if (typeof onReceivedContacts === 'function') { onReceivedContacts('[]'); }", null)
                    );
                }
            });

    private final ActivityResultLauncher<String[]> requestSimPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allGranted = true;
                for (Boolean isGranted : permissions.values()) {
                    if (!isGranted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    fetchSim();
                } else {
                    boolean shouldShowRationale = androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS) ||
                            androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_PHONE_STATE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        shouldShowRationale = shouldShowRationale || androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_PHONE_NUMBERS);
                    }

                    if (!shouldShowRationale) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            showPermissionRequiredDialog();
                        }
                    }
                    runOnUiThread(() ->
                            webView.evaluateJavascript("if (typeof onGetSimDetailsResponse === 'function') { onGetSimDetailsResponse('[]'); }", null)
                    );
                }
            });



    boolean isResponsePageVisible = false;

    public class WebAppInterface {

        @JavascriptInterface
        public void playStatusSound(String status) {
            runOnUiThread(() -> {
                if (status.toLowerCase().contains("success")) {
                    MediaPlayer mediaPlayer = MediaPlayer.create(NextWebActivity.this, R.raw.success);
                    mediaPlayer.start();
                } else {
                    MediaPlayer mediaPlayer = MediaPlayer.create(NextWebActivity.this, R.raw.failure);
                    mediaPlayer.start();
                }
            });
        }


        @JavascriptInterface
        public void getFirebaseAppConfigForced() {
            runOnUiThread(() -> {
                AppConfigManager.clearFirebaseCache(NextWebActivity.this);
                getFirebaseAppConfig();
            });
        }

        @JavascriptInterface
        public void onUrlChange(String url) {
            runOnUiThread(() -> {

                currentUrl = url;

                Log.d("DDDDD", url);

                // Prevent going back to login page
                if (url.contains("/auth/login")) {
                    new Handler().postDelayed(() -> {
                        webView.clearHistory();
                    }, 500);
                }

                if (url.equals(rootUrl)) {
                    new Handler().postDelayed(() -> {
                        webView.clearHistory();
                    }, 500);
                }

                if (
                        url.contains("addmoney/response") ||
                                url.contains("services/response")
                ) {
                    isResponsePageVisible = true;
                }
            });
        }

        @JavascriptInterface
        public void getSystemContacts() {
            runOnUiThread(() -> {
                ContactListManager contactListManager = new ContactListManager(NextWebActivity.this);

                if (contactListManager.hasContactPermission()) {
                    fetchContacts();
                } else {
                    requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
                }
            });
        }

        @JavascriptInterface
        public void oldAppUserCheck() {
            ModuleUserDetails moduleUserDetails = UserAuth.getUserDetails(NextWebActivity.this);
            if (moduleUserDetails != null) {
                String token = moduleUserDetails.getToken();
                runOnUiThread(() -> webView.evaluateJavascript("if (typeof onOldAppUserCheck === 'function') { onOldAppUserCheck(" + JSONObject.quote(token) + "); }", null));
            }
        }

        @JavascriptInterface
        public void checkAndroidReferCode() {
            InstallReferrerClient referrerClient;
            referrerClient = InstallReferrerClient.newBuilder(NextWebActivity.this).build();
            referrerClient.startConnection(new InstallReferrerStateListener() {
                @Override
                public void onInstallReferrerSetupFinished(int responseCode) {

                    if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        try {
                            ReferrerDetails response = referrerClient.getInstallReferrer();
                            String referrerUrl = response.getInstallReferrer();

                            if (referrerUrl != null && referrerUrl.contains("ref=")) {
                                String refCode = referrerUrl.split("ref=")[1].split("&")[0];
                                if (refCode != null) {
                                    runOnUiThread(() -> webView.evaluateJavascript("if (typeof onRefCodeReceived === 'function') { onRefCodeReceived(" + JSONObject.quote(refCode) + "); }", null));
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    referrerClient.endConnection();
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {

                }
            });
        }

        @JavascriptInterface
        public void clearOldAppUserData() {
            UserAuth.clearUserDetails(NextWebActivity.this);
        }


        @JavascriptInterface
        public void storeAppUserData(String jwtToken) {
            ModuleUserDetails userDetails = UserAuth.getUserDetails(NextWebActivity.this);
            if (userDetails != null) {
                userDetails.setToken(jwtToken);
            } else {
                userDetails = new ModuleUserDetails("", "", "", "", "", "", "off", "", "", "", jwtToken);
            }
            UserAuth.storeUserDetails(NextWebActivity.this, userDetails);

            runOnUiThread(() -> {
                new Handler().postDelayed(() -> {
                    webView.clearHistory();
                }, 500);
            });
        }

        @JavascriptInterface
        public void setBioMetric(String bioMetricStatus) {
            ModuleUserDetails userDetails = UserAuth.getUserDetails(NextWebActivity.this);
            if (userDetails != null) {
                userDetails.setBiometric(bioMetricStatus);
            } else {
                userDetails = new ModuleUserDetails("", "", "", "", "", "", bioMetricStatus, "", "", "", "");
            }
            setBiometric(userDetails);
        }

        @JavascriptInterface
        public void getBioMetric() {
            ModuleUserDetails userDetails = UserAuth.getUserDetails(NextWebActivity.this);
            String biometricStatus = (userDetails != null) ? userDetails.getBiometric() : "off";
            if (userDetails != null) {
                runOnUiThread(() -> webView.evaluateJavascript("if (typeof onGotBioMetricReceived === 'function') { onGotBioMetricReceived(" + JSONObject.quote(biometricStatus) + "); }", null));
            }
        }

        @JavascriptInterface
        public void testFunction(Object param) {
            try {
                Gson gson = new Gson();
                String json = gson.toJson(param);
                MyPublicClass.showMyCustomDialog(NextWebActivity.this, "Test", json);
            } catch (Exception e) {
                Log.e("WEB_INTERFACE", "Error converting to JSON", e);
            }
        }

        @JavascriptInterface
        public void getSimDetails() {
            runOnUiThread(() -> {
                if (PermissionUtils.hasSimAndContactPermission(NextWebActivity.this)) {
                    try {
                        fetchSim();
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(NextWebActivity.this, "There is a problem on sim fetch", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    requestSimPermissionsLauncher.launch(new String[]{
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_PHONE_NUMBERS
                    });
                }
            });
        }


        @JavascriptInterface
        public void startDeviceOtpListener() {
            try {
                startOtpListener();
            } catch (ActivityNotFoundException e) {
                Toast.makeText(NextWebActivity.this, "Otp retrieve issue", Toast.LENGTH_SHORT).show();
            }

        }


        @JavascriptInterface
        public void openAndroidIntent(String payment_url, String redirect_url) {
            try {
                AddMoney.storeAddMoneyResponseURl(NextWebActivity.this, redirect_url);
                if (payment_url.contains("http")) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    // Customize the toolbar color
                    builder.setToolbarColor(ContextCompat.getColor(NextWebActivity.this, R.color.logo_color));
                    builder.addDefaultShareMenuItem();
                    builder.setShowTitle(true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(NextWebActivity.this, Uri.parse(payment_url));
                    isPaymentOpen = true;
                } else {
                    isPaymentOpen = true;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payment_url));
                    int PAYMENT_INTENT_CODE = 100;
                    startActivityForResult(intent, PAYMENT_INTENT_CODE);
                }
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(NextWebActivity.this, "UPI app not found on this device", Toast.LENGTH_SHORT).show();
            }
        }


        @JavascriptInterface
        public void share(String title, String text, String url) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TITLE, title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, text + " " + url);
            try {
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            } catch (android.content.ActivityNotFoundException ex) {
                Log.e("WEB_INTERFACE", "No sharing apps installed.", ex);
            }
        }

        @SuppressLint("CommitPrefEdits")
        @JavascriptInterface
        public void changeStatusBarColor(final String colorHex, final String theme) {
            runOnUiThread(() -> {
                SharedPreferences.Editor editor = spAppTheme.edit();
                editor.putString("appThemeColor", colorHex);
                editor.putString("appTheme", theme);
                editor.apply();

                if ("dark".equalsIgnoreCase(theme)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else if ("light".equalsIgnoreCase(theme)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }

            });
        }
    }


    private void changeAppTheme() {
        try {
            String appThemeColor = spAppTheme.getString("appThemeColor", "#f5f7fb");
            String appTheme = spAppTheme.getString("appTheme", null);
            if (mainLayout != null) {
                int color = Color.parseColor(appThemeColor);
                mainLayout.setBackgroundColor(color);
            }
            Window window = getWindow();
            if ("dark".equals(appTheme)) {
                window.setStatusBarColor(Color.BLACK);
                WindowInsetsControllerCompat insetsController =
                        new WindowInsetsControllerCompat(window, window.getDecorView());
                insetsController.setAppearanceLightStatusBars(false);
            } else {
                window.setStatusBarColor(Color.WHITE);
                WindowInsetsControllerCompat insetsController =
                        new WindowInsetsControllerCompat(window, window.getDecorView());
                insetsController.setAppearanceLightStatusBars(true);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNightModeChanged(int mode) {
        super.onNightModeChanged(mode);
        changeAppTheme();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (uploadMessage == null) return;
            Uri[] result = (data != null && resultCode == RESULT_OK) ? new Uri[]{data.getData()} : null;
            uploadMessage.onReceiveValue(result);
            uploadMessage = null;
        }

    }

    //OTP-----------
    private SmsBroadcastReceiver smsReceiver;

    @Override
    protected void onStart() {
        super.onStart();
        smsReceiver = new SmsBroadcastReceiver();
        SmsBroadcastReceiver.bindListener(otp -> {
            if (otp.length() == 6) {
                runOnUiThread(() ->
                        webView.evaluateJavascript(
                                "if (typeof onDeviceOtpListenerResponse === 'function') { " +
                                        "onDeviceOtpListenerResponse(" + JSONObject.quote(otp) + "); " +
                                        "}",
                                null
                        )
                );
            }
        });


        IntentFilter filter =
                new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);

        ContextCompat.registerReceiver(
                this,
                smsReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (smsReceiver != null) {
            try {
                unregisterReceiver(smsReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        CookieManager.getInstance().flush();
    }

    @Override
    protected void onDestroy() {
        CookieManager.getInstance().flush();
        super.onDestroy();
    }

    private void startOtpListener() {

        Log.d("OTP_Hash", Objects.requireNonNull(AppSignatureHelper.getAppHash(NextWebActivity.this)));

        SmsRetriever.getClient(NextWebActivity.this)
                .startSmsRetriever()
                .addOnSuccessListener(aVoid ->
                        Log.d("OTP", "SMS Retriever Started"))
                .addOnFailureListener(e ->
                        Log.e("OTP", "SMS Retriever Failed"));
    }
    //OTP-----------

    @Override
    protected void onRestart() {
        super.onRestart();
    }


    //    Push Notification------S
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        runOnUiThread(() -> {
            if (webView != null) {
                webView.evaluateJavascript(
                        "if (typeof onPermissionResult === 'function') { " +
                                "onPermissionResult(" + isGranted + "); " +
                                "}",
                        null
                );

            }
        });
    });

    public void setUpPushNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {


            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
    //    Push Notification------E


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        if (isPaymentOpen) {
            isPaymentOpen = false;
            runOnUiThread(() -> webView.evaluateJavascript("if (typeof onIntentResponse === 'function') { onIntentResponse(" + JSONObject.quote(AddMoney.getAddMoneyResponseURl(NextWebActivity.this)) + "); }", null));
        }
    }

    String versionCode;

    private void getFirebaseAppConfigOld() {
        int appVersion = AppVersionController.getAppVersion(NextWebActivity.this);
        int appPreviousVersion = AppVersionController.getPreviousVersion(NextWebActivity.this);
        if (appVersion > appPreviousVersion) {
            AppVersionController.storeAppVersion(NextWebActivity.this, appVersion);
        }
        versionCode = "v" + appVersion;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("next").child(versionCode);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e("FirebaseData", "Data:" + snapshot);

                if (snapshot.exists()) {
                    String appUpdate = snapshot.child("apUpd").getValue(String.class);
                    String dialogImgUrl = snapshot.child("img").getValue(String.class);
                    boolean isDialogCancelable = Boolean.TRUE.equals(snapshot.child("isC").getValue(Boolean.class));
                    String dialogMessage = snapshot.child("mssg").getValue(String.class);
                    String fbUrl = snapshot.child("rootUrl").getValue(String.class);
                    String dialogTitle = snapshot.child("title").getValue(String.class);
                    int diloadDelay = snapshot.child("diloadDelay").getValue(Integer.class);

                    if (fbUrl != null && !Objects.equals(fbUrl, rootUrl)) {
                        rootUrl = fbUrl.concat(rootEndPoint);
                        webView.loadUrl(rootUrl);
                    }

                    ModuleFirebase firebaseModule = new ModuleFirebase(dialogImgUrl, dialogMessage, dialogTitle, isDialogCancelable, fbUrl);
                    AppConfigManager.storeFirebaseAppConfig(NextWebActivity.this, firebaseModule);
                    assert appUpdate != null;
                    if (appUpdate.equalsIgnoreCase("yes")) {
                        customDialog(appUpdate);
                    }


                    assert dialogTitle != null;
                    if (!dialogTitle.isEmpty()) {
                        new Handler().postDelayed(() -> HomeDialog.setUpHomeDialog(NextWebActivity.this, firebaseModule), (diloadDelay * 1000L));
                    }

                } else {
                    Log.d("FirebaseData", "No data found for versionCode: " + versionCode);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseData", "Error fetching data: " + error.getMessage());
            }
        });
    }

    private void getFirebaseAppConfig() {
        int appVersion = AppVersionController.getAppVersion(this);
        int previousVersion = AppVersionController.getPreviousVersion(this);

        // 🔥 If app updated → clear cache
        if (appVersion > previousVersion) {
            AppConfigManager.clearFirebaseCache(this);
            AppVersionController.storeAppVersion(this, appVersion);
        }

        // 🔥 Check 48h cache
        long cacheExpiry = AppConfigManager.getCacheTime(this);
        long currentTime = System.currentTimeMillis();

        if (cacheExpiry > currentTime) {
            ModuleFirebase cachedData = AppConfigManager.getFirebaseAppConfig(this);
            if (cachedData != null) {
                applyFirebaseConfig(cachedData);
                return; // ✅ Skip Firebase call
            }
        }

        // 🔥 Firebase call
        String versionCode = "v" + appVersion;

        DatabaseReference myRef = FirebaseDatabase.getInstance()
                .getReference("next")
                .child(versionCode);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.e("FirebaseData", "Data:" + snapshot);

                if (!snapshot.exists()) return;

                String appUpdate = snapshot.child("apUpd").getValue(String.class);
                String dialogImgUrl = snapshot.child("img").getValue(String.class);
                boolean isDialogCancelable = Boolean.TRUE.equals(snapshot.child("isC").getValue(Boolean.class));
                String dialogMessage = snapshot.child("mssg").getValue(String.class);
                String fbUrl = snapshot.child("rootUrl").getValue(String.class);
                String dialogTitle = snapshot.child("title").getValue(String.class);
                Integer diloadDelay = snapshot.child("diloadDelay").getValue(Integer.class);

                ModuleFirebase firebaseModule = new ModuleFirebase(
                        dialogImgUrl,
                        dialogMessage,
                        dialogTitle,
                        isDialogCancelable,
                        fbUrl
                );

                // ✅ Save cache for 7 days
                long expiryTime = System.currentTimeMillis() + (7L * 24L * 60 * 60 * 1000);

                AppConfigManager.storeFirebaseAppConfig(NextWebActivity.this, firebaseModule);
                AppConfigManager.storeCacheTime(NextWebActivity.this, expiryTime);

                applyFirebaseConfig(firebaseModule);

                if ("yes".equalsIgnoreCase(appUpdate)) {
                    customDialog(appUpdate);
                }

                if (dialogTitle != null && !dialogTitle.isEmpty()) {
                    new Handler().postDelayed(() ->
                                    HomeDialog.setUpHomeDialog(NextWebActivity.this, firebaseModule),
                            (diloadDelay != null ? diloadDelay : 0) * 1000L
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseData", error.getMessage());
            }
        });
    }


    private void applyFirebaseConfig(ModuleFirebase firebaseModule) {
        if (firebaseModule.getRootUrl() != null &&
                !firebaseModule.getRootUrl().equals(rootUrl)) {
            rootUrl = firebaseModule.getRootUrl().concat(rootEndPoint);
            webView.loadUrl(rootUrl);
        }
    }

    public void customDialog(String updt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_app_update, null);
        TextView txtDscrptn = view.findViewById(R.id.txtDscrptn);
        Button btnUpdate = view.findViewById(R.id.btnUpdate);
        Button close = view.findViewById(R.id.btnClose);

        if (updt.equals("YES") || updt.equals("Yes")) {
            close.setVisibility(View.GONE);
            txtDscrptn.setText(getString(R.string.YESUdtDscptn));
        }
        builder.setView(view);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);

        btnUpdate.setOnClickListener(view12 -> {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getString(R.string.app_package_name));
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        });

        close.setOnClickListener(view1 -> dialog.dismiss());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (broadcastReceiver != null) {
            try {
                unregisterReceiver(broadcastReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        CookieManager.getInstance().flush();
    }


    private void checkBiometric(ModuleUserDetails moduleUserDetails) {
        if (moduleUserDetails != null && "on".equals(moduleUserDetails.getBiometric())) {
            Biometric biometric = new Biometric();
            biometric.setBiometricPrompt(NextWebActivity.this, new Biometric.BiometricCallback() {
                @Override
                public void onSuccess() {
                    webView.loadUrl(rootUrl);
                }

                @Override
                public void onError(String error) {
                    finish();
                }

                @Override
                public void onFailed() {
                    finish();
                }
            });
        } else {
            webView.loadUrl(rootUrl);
        }
    }

    private void setBiometric(ModuleUserDetails moduleUserDetails) {
        Biometric biometric = new Biometric();
        biometric.setBiometricPrompt(NextWebActivity.this, new Biometric.BiometricCallback() {
            @Override
            public void onSuccess() {
                UserAuth.storeUserDetails(NextWebActivity.this, moduleUserDetails);
                runOnUiThread(() -> webView.evaluateJavascript("if (typeof onGotBioMetricReceived === 'function') { onGotBioMetricReceived(" + JSONObject.quote(moduleUserDetails.getBiometric()) + "); }", null));
            }

            @Override
            public void onError(String error) {

            }

            @Override
            public void onFailed() {

            }
        });
    }


}