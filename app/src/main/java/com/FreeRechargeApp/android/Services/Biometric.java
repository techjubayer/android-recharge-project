package com.FreeRechargeApp.android.Services;



import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.FreeRechargeApp.R;

import java.util.concurrent.Executor;

public class Biometric {

    private Executor executor;
    private BiometricPrompt.PromptInfo promptInfo;

    public interface BiometricCallback {
        void onSuccess();
        void onError(String error);
        void onFailed();
    }

    public void setBiometricPrompt(FragmentActivity activity, BiometricCallback callback) {
        executor = ContextCompat.getMainExecutor(activity);

        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode,
                                                      @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (callback != null) callback.onError(errString.toString());
                    }

                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        if (callback != null) callback.onSuccess();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        if (callback != null) callback.onFailed();
                    }
                });

        // ✅ Use biometric OR device credentials (PIN/Pattern/Password)
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock the App")
                .setSubtitle("Unlock your screen with PIN, Pattern, Password, Face, or Fingerprint")
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG |
                                BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build();

        BiometricManager biometricManager = BiometricManager.from(activity);
        int canAuth = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );

        switch (canAuth) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                biometricPrompt.authenticate(promptInfo);
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                if (callback != null) callback.onError("This device has no biometric hardware.");
                break;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                if (callback != null) callback.onError("Biometric hardware is currently unavailable.");
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                if (callback != null) callback.onError("No biometric enrolled. Please set up fingerprint/face.");
                break;
            default:
                if (callback != null) callback.onError("Biometric authentication not available.");
                break;
        }
    }
}
