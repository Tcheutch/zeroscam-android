plugins {
    // Module Android Library classique, cohérent avec le reste du projet
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.zeroscam.core_ml"   // avec underscore comme tu l'as précisé
    compileSdk = 36                      // aligné sur :app

    defaultConfig {
        minSdk = 29                      // aligné sur ton app (ou plus bas si tu le décides)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        // Même niveau que le reste du projet
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Le module ML dépend du domaine pur (interfaces, modèles, use cases)
    implementation(project(":core-domain"))

    // Lib Android de base (optionnelle pour l'instant)
    implementation(libs.androidx.core.ktx)

    // Tests unitaires et instrumentés
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Plus tard : TFLite, etc.
    // implementation("org.tensorflow:tensorflow-lite:VERSION")
    // implementation("org.tensorflow:tensorflow-lite-support:VERSION")
}
