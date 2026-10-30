plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.aurabeat.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aurabeat.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // Spotify OAuth
        buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"79bb90ca4c5f46609b69995f9f99505a\"")
        buildConfigField("String", "SPOTIFY_REDIRECT_URI", "\"com.aurabeat.app://spotify-callback\"")
        manifestPlaceholders["spotifyRedirectScheme"] = "com.aurabeat.app"
        manifestPlaceholders["spotifyRedirectHost"] = "spotify-callback"
    }

    signingConfigs {
        // Populate keystore path/passwords in gradle.properties (NOT committed):
        //   AURABEAT_KEYSTORE=/absolute/path/to/aurabeat-release.jks
        //   AURABEAT_KEYSTORE_PASSWORD=...
        //   AURABEAT_KEY_ALIAS=aurabeat
        //   AURABEAT_KEY_PASSWORD=...
        // Signed builds are skipped silently if any of these are missing.
        create("release") {
            val keystorePath = (project.findProperty("AURABEAT_KEYSTORE") as String?)
            if (!keystorePath.isNullOrBlank() && file(keystorePath).exists()) {
                storeFile = file(keystorePath)
                storePassword = project.findProperty("AURABEAT_KEYSTORE_PASSWORD") as String? ?: ""
                keyAlias = project.findProperty("AURABEAT_KEY_ALIAS") as String? ?: "aurabeat"
                keyPassword = project.findProperty("AURABEAT_KEY_PASSWORD") as String? ?: ""
            }
        }
    }

    buildTypes {
        release {
            // Minification disabled: Spotify's App Remote .aar references Jackson
            // and @NotNull classes it doesn't bundle, which breaks R8. Keeping
            // code un-shrunk sidesteps that at the cost of ~2 MB on the APK.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val cfg = signingConfigs.getByName("release")
            if (cfg.storeFile?.exists() == true) signingConfig = cfg
        }
        // debug uses the same applicationId as release so raw resource URIs
        // (`android.resource://com.aurabeat.app/raw/…`) resolve on debug installs.
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.animation)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.ui)
    implementation(libs.media3.common)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.datastore.preferences)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.browser)
    implementation(libs.coil.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging)

    // Spotify App Remote SDK — dropped in app/libs/ manually
    implementation(files("libs/spotify-app-remote-release-0.8.0.aar"))
    // Transitive deps the AAR references but doesn't bundle
    implementation(libs.gson)
    implementation(libs.protobuf.javalite)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
