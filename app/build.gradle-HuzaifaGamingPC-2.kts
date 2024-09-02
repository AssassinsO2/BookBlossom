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
        versionName = "1.0.2"

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
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation("com.google.android.material:material:1.3.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.5.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation(libs.firebase.storage)
    implementation (libs.firebase.auth.v2101)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.database)
}