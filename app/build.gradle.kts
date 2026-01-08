plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

android {
    // ✅ IMPORTANT: doit matcher tes packages Kotlin (com.zeroscam.app.*)
    namespace = "com.zeroscam.app"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.zeroscam.app"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ Kotlin DSL: parenthèses
        buildConfigField("String", "RESEARCH_BASE_URL", "\"http://10.0.2.2:8080/\"")
    }

    buildTypes {
        debug {
            buildConfigField("String", "RESEARCH_BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
        release {
            buildConfigField("String", "RESEARCH_BASE_URL", "\"https://api.zeroscam-research.prod/\"")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }

    buildFeatures {
        compose = true
        // ✅ sinon BuildConfig peut être absent
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // ✅ Si tu gardes AppCompat/Fragment Debug UI:
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Add these lines for Android instrumentation tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.junit)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.bundles.compose.debug)

    implementation(project(":core-domain"))
    implementation(project(":core-ui"))

    // Retrofit + Moshi + OkHttp + Debug (bundles)
    implementation(libs.bundles.retrofit.ecosystem)
    implementation(libs.bundles.okhttp.bundle)
}
