import java.util.Properties
plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.saborpraticidade.marmita"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.saborpraticidade.marmita"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        // Load Supabase credentials from android-app/local.properties or env vars
    val props = Properties()
        val localFile = rootProject.file("local.properties")
        if (localFile.exists()) {
            props.load(localFile.inputStream())
        }
        val supabaseUrl = (props.getProperty("SUPABASE_URL") ?: System.getenv("SUPABASE_URL") ?: "")
        val supabaseKey = (props.getProperty("SUPABASE_KEY") ?: System.getenv("SUPABASE_KEY") ?: "")
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    // With Kotlin 2.0 + compose plugin, no need to set compiler extension version manually
    packaging.resources.excludes += 
        setOf("META-INF/AL2.0", "META-INF/LGPL2.1")
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    implementation("androidx.navigation:navigation-compose:2.8.2")
    implementation("com.google.android.material:material:1.12.0")
    // Window size classes for responsive UI
    implementation("androidx.compose.material3:material3-window-size-class:1.3.0")

    // Paging
    implementation("androidx.paging:paging-runtime-ktx:3.3.2")
    implementation("androidx.paging:paging-compose:3.3.2")

    // Material (provides pullRefresh APIs in androidx.compose.material.pullrefresh)
    implementation("androidx.compose.material:material")

    // DI
    implementation("io.insert-koin:koin-android:3.5.6")
    implementation("io.insert-koin:koin-androidx-compose:3.5.6")

    // Supabase (Auth, Postgrest, Storage) - using BOM
        // Supabase via HTTP (PostgREST endpoints) using Ktor
        implementation("io.ktor:ktor-client-android:2.3.12")
        implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
        implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")

    // Networking
    implementation("io.ktor:ktor-client-android:2.3.12")

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // Images
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
