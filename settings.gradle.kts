pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MyApp"
include(":app")
include(":core-domain")
include(":core-data")
include(":core-ml")
include(":core-ui")


include(":feature-callguard")
include(":feature-messageguard")
include(":feature-paymentguardian")
include(":feature-contextguardian")
include(":feature-awareness")
