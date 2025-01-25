plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.Alexandria"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.Alexandria"
        minSdk = 21
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat.v131)
    implementation(libs.material.v140)
    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout.v204)

    // https://mvnrepository.com/artifact/com.github.barteksc/android-pdf-viewer
    implementation ("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.v113)
    androidTestImplementation(libs.espresso.core.v340)
}
