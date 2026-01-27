plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zeroscam.coredata"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // Domain (ports + models)
    implementation(project(":core-domain"))

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Room (KSP only)
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // DataStore + WorkManager
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)

    // Instrumentation tests (optionnels)
    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(libs.androidx.room.testing)
}

ktlint {
    android.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(false)
}
