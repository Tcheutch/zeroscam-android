
plugins {
    // Module Kotlin pur (pas de plugin Android ici)
    kotlin("jvm")
}

java {
    // On cible un bytecode compatible Java 11 (comme les modules Android)
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    // Kotlin génère aussi du bytecode compatible Java 11
    kotlinOptions.jvmTarget = "11"
}

dependencies {
    // Kotlin standard library
    implementation(kotlin("stdlib"))

    // Tests unitaires (via ton catalogue libs)
    testImplementation(libs.junit)
}
