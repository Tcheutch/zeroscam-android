plugins {
    // Même style que le reste du projet
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    // Tu as précisé que les namespaces sont avec underscore
    namespace = "com.zeroscam.core_ui"
    compileSdk = 36

    defaultConfig {
        minSdk = 29

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

    // Java/Kotlin alignés avec :app
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    // Compose activé dans ce module UI
    buildFeatures {
        compose = true
    }
    composeOptions {
        // À aligner avec la valeur utilisée dans :app
        // Exemple typique (si tu as une version dans le catalog) :
        // kotlinCompilerExtensionVersion = libs.versions.androidxComposeCompiler.get()
        //
        // Sinon, copie exactement la même string que dans app/build.gradle.kts
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

dependencies {
    // Le module UI réutilise le domaine
    implementation(project(":core-domain"))

    // Libs Android de base (si tu en as besoin ici)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose : à adapter selon ton libs.versions.toml
    // Exemple si tu utilises un BOM dans le catalog :
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    debugImplementation(libs.androidx.compose.ui.tooling)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
