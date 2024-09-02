plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.bookblossom"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bookblossom"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures{
        viewBinding = true
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
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation (libs.logging.interceptor)
    implementation (libs.okhttp)
    implementation(libs.constraintlayout.v210)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.viewpager2)
    implementation(libs.picasso)
    implementation(libs.picasso)
    implementation (libs.lifecycle.viewmodel)
    implementation (libs.lifecycle.livedata)
    implementation(libs.firebase.storage)
    implementation (libs.firebase.auth.v2101)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.database)
}