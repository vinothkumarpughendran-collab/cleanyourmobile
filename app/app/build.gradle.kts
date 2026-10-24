plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cleanyourmobile"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.cleanyourmobile"
        minSdk = 28
        targetSdk = 30
        versionCode = 1
        versionName = "0.1"
    }
    sourceSets["main"].assets.srcDirs("../../lists")
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
}
