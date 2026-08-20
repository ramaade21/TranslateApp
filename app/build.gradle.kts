import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    kotlin("plugin.serialization") version "1.9.24"
}

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace = "com.linguatranslate.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.linguatranslate.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val debugBaseUrl = (localProperties.getProperty("LINGUATRANSLATE_BASE_URL_DEBUG")
            ?: providers.gradleProperty("LINGUATRANSLATE_BASE_URL_DEBUG").orNull
            ?: "http://10.0.2.2:3000/")
        buildConfigField("String", "BASE_URL_DEBUG", "\"$debugBaseUrl\"")

        val releaseBaseUrl = (localProperties.getProperty("LINGUATRANSLATE_BASE_URL_RELEASE")
            ?: "https://api.linguatranslate.example.com/")
        buildConfigField("String", "BASE_URL_RELEASE", "\"$releaseBaseUrl\"")

        // Shared secret sent as the X-App-Key header on every backend
        // request - must match APP_API_KEY on the server. Read from
        // local.properties (gitignored) so it is never committed to
        // source control. Falls back to an empty string so the project
        // still compiles for anyone who hasn't set it yet (requests will
        // simply get 401s from a server that enforces the key).
        val appApiKey = localProperties.getProperty("LINGUATRANSLATE_APP_API_KEY") ?: ""
        buildConfigField("String", "APP_API_KEY", "\"$appApiKey\"")
    }

    signingConfigs {
        create("release") {
            // Populated from environment variables in CI (see
            // .github/workflows/android-build.yml) so the keystore and
            // its passwords are never committed to source control.
            // Locally, assembleRelease will just produce an unsigned
            // APK if these aren't set - that's fine for local testing.
            val keystorePath = System.getenv("LINGUA_KEYSTORE_PATH")
            if (!keystorePath.isNullOrBlank()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("LINGUA_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("LINGUA_KEY_ALIAS")
                keyPassword = System.getenv("LINGUA_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            buildConfigField("boolean", "DEBUG_LOGGING", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            val hasSigningEnv = !System.getenv("LINGUA_KEYSTORE_PATH").isNullOrBlank()
            if (hasSigningEnv) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("boolean", "DEBUG_LOGGING", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // DataStore (settings persistence)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Dependency injection
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("com.google.truth:truth:1.4.2")

    // Instrumented / UI tests
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
