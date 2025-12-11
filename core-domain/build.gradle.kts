plugins {
    kotlin("jvm")
    alias(libs.plugins.ktlint)
    id("jacoco")
}

dependencies {
    // Kotlin standard library
    implementation(kotlin("stdlib"))

    // Tests unitaires JUnit 4
    testImplementation(libs.junit)
}

// --- Jacoco : configuration couverture tests pour :core-domain

tasks.test {
    useJUnit() // on utilise JUnit 4
    finalizedBy(tasks.jacocoTestReport) // après les tests, générer le rapport
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}
