plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.rudra.smartworktracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rudra.smartworktracker"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        debug {
            isMinifyEnabled = false
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
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.lifecycle.runtime.ktx.v270)

    // Compose
    implementation(libs.androidx.activity.compose.v180)
    implementation(platform(libs.androidx.compose.bom.v20231001))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material3.window.size.class1)
    // Navigation Compose (official - no Accompanist needed)
    implementation(libs.androidx.navigation.compose)
    // Room Database (KSP only)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.core)
    ksp(libs.androidx.room.compiler)

    // Animation
    implementation(libs.lottie.compose)
    implementation("nl.dionsegijn:konfetti-compose:2.0.2")

    // Charts
    implementation(libs.compose)
    implementation(libs.compose.m3)
    implementation("co.yml:ycharts:2.1.0")
    implementation("com.google.code.gson:gson:2.11.0")
    // DateTime
    implementation(libs.kotlinx.datetime)

    // Accompanist (for additional Compose features)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.navigation.animation)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

   //Other

    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.compose.material:material-icons-extended")
// https://mvnrepository.com/artifact/org.mnode.ical4j/ical4j
    implementation("org.mnode.ical4j:ical4j:4.2.1")

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)
    androidTestImplementation(platform(libs.androidx.compose.bom.v20251001))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}