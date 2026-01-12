import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.serialization)
    jacoco
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.immutableList)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // Navigation
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tab.navigator)
            implementation(libs.voyager.bottomsheet.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.koin)
            // DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            // Dates
            implementation(libs.kotlinx.datetime)
            // FilePicker
            implementation(libs.filekit.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.mockito.core)
            implementation(libs.mockito.inline)
            implementation(libs.mockito.kotlin)
        }
        jvmTest.dependencies {
            implementation(libs.apache.poi.ooxml)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            // Excel (JVM only)
            implementation(libs.apache.poi.ooxml)
            implementation(libs.apache.logging.api)
            runtimeOnly(libs.apache.logging.core)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.contentnegotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.cio)
        }
    }
}

compose.desktop {
    application {
        mainClass = "ru.jengle88.klarkclient.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ru.jengle88.klarkclient"
            packageVersion = "1.0.0"
        }
    }
}

ktlint {
    debug.set(false)
    verbose.set(true)
    android.set(false) // установите true, если проект под Android
    outputToConsole.set(true)
    ignoreFailures.set(false) // если true, проект соберется даже с ошибками стиля
    filter {
        exclude("**/generated/**") // исключить сгенерированный код
    }
}

// Jacoco configuration for code coverage
jacoco {
    toolVersion = "0.8.12"
}

// Configure Jacoco test report for JVM tests
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("jvmTest"))

    reports {
        // Generate XML report for Codecov
        xml.required.set(true)
        xml.outputLocation.set(
            file("${layout.buildDirectory.get()}/reports/jacoco/test/jacocoTestReport.xml"),
        )

        // Generate HTML report for manual review
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/test/html"))
    }

    // Configure source and class directories for multiplatform project
    val coverageSourceDirs =
        listOf(
            "src/commonMain/kotlin",
            "src/jvmMain/kotlin",
        )

    sourceDirectories.setFrom(files(coverageSourceDirs))
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("classes/kotlin/jvm/main")) {
            exclude(
                // Exclude generated files if needed
                "**/BuildConfig.*",
            )
        },
    )

    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("jacoco/jvmTest.exec")
        },
    )
}
