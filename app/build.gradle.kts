import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Release signing credentials live in keystore.properties (git-ignored), so the
// same stable key signs every build and users can always update in place.
// The report write key, git-ignored like the launcher's. Absent from a fresh clone, and
// then the send button is not offered rather than offered and broken.
val apiKeysFile = rootProject.file("apikeys.properties")
val apiKeys = Properties().apply {
    if (apiKeysFile.exists()) {
        load(FileInputStream(apiKeysFile))
    }
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

android {
    namespace = "com.tommasov.mg4swipenovalauncher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tommasov.mg4swipenovalauncher"
        minSdk = 28
        targetSdk = 34
        versionCode = 4
        // Suffixed because this build is not the published 1.4.1: it carries the launch
        // experiment, the report sender and the INTERNET permission that comes with it.
        // The report prints this string, so it also says which build produced a reading.
        versionName = "1.4.1-lab"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // The author's report endpoint. Write-only by design: it accepts a report and can do
        // nothing else — no reading back, no listing, no deleting, with any key. That is what
        // makes it safe to ship the write key inside an APK.
        buildConfigField(
            "String",
            "PROBE_URL",
            "\"https://ws2.tommasovietina.it/mg4/probe.php\""
        )
        buildConfigField("String", "PROBE_KEY", "\"${apiKeys.getProperty("probe.key", "")}\"")
        buildConfigField("String", "PROBE_APP", "\"swipe\"")
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}