plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")

}

android {
    signingConfigs {
        create("secure") {
            val keystoreFile = project.findProperty("KEYSTORE_FILE") as String?
            val keystorePassword = project.findProperty("KEYSTORE_PASSWORD") as String?
            val keyAlias = project.findProperty("KEY_ALIAS") as String?
            val keyPassword = project.findProperty("KEY_PASSWORD") as String?

            if (keystoreFile != null) {
                storeFile = file(keystoreFile)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }


    namespace = "com.FreeRechargeApp"
    compileSdk = 36



    defaultConfig {
        applicationId = "com.FreeRechargeApp"
        minSdk = 24
        targetSdk = 35
        versionCode = 61
        versionName = "6.3.1.61"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.google.firebase.messaging)
    implementation(libs.integrity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

//    New Added---------S
    implementation(libs.glide)
    implementation(libs.gson)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.com.google.firebase.firebase.analytics)
    implementation(libs.firebase.database)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.google.firebase.appcheck.playintegrity)
    implementation(libs.installreferrer)
    implementation(libs.browser)
    implementation(libs.biometric)
    implementation(libs.cardview)
    implementation(libs.lottie)
    implementation(libs.security.crypto)
    implementation(libs.play.services.auth.api.phone)


//    New Added---------E
}