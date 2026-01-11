plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.webviewpoc"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.webviewpoc"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)



    // ============================================
    // WebView Dependencies
    // ============================================

    // 1. Accompanist WebView - Easy Compose integration
    implementation("com.google.accompanist:accompanist-webview:0.36.0")

    // 2. AndroidX WebKit (Jetpack WebKit) - Advanced features
    // Provides WebViewCompat and WebViewFeature APIs
    implementation("androidx.webkit:webkit:1.15.0")

    // ============================================
    // Alternative WebView Engines (Optional)
    // ============================================

    // GeckoView (Mozilla Firefox engine) - Uncomment to test
    // implementation("org.mozilla.geckoview:geckoview:120.0.20231208170842")

    // ============================================
    // Additional Useful Dependencies
    // ============================================

    // Timber for logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Hilt for Dependency Injection
    implementation("com.google.dagger:hilt-android:2.57.2")
    // kapt("com.google.dagger:hilt-compiler:2.50") // If using kapt

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")


}