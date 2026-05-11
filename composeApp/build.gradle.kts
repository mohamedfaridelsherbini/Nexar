import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kover)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.21"
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.google.material)
            implementation(libs.koin.android)

            // CameraX
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)

            // ML Kit
            implementation(libs.google.mlkit.document.scanner)
            implementation(libs.google.mlkit.text.recognition)

            // Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.ktx)
            implementation(libs.androidx.sqlite.bundled)

            // DocumentFile for SAF
            implementation(libs.androidx.documentfile)

            // WorkManager for background notifications
            implementation(libs.androidx.work.runtime.ktx)

            // Glance for Home Screen Widgets
            implementation(libs.androidx.glance.appwidget)
        }
        iosMain.dependencies {
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Navigation 3
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.androidx.material3.adaptive.navigation3)

            // Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            // Serialization
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

android {
    namespace = "com.mohamedfaridelsherbini.nexar"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = true
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.mohamedfaridelsherbini.nexar"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
            excludes += "/META-INF/INDEX.LIST"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

kover {
    reports {
        filters {
            excludes {
                androidGeneratedClasses()
                packages(
                    "com.mohamedfaridelsherbini.nexar.ui",
                    "com.mohamedfaridelsherbini.nexar.ui.components",
                    "com.mohamedfaridelsherbini.nexar.ui.theme",
                    "com.mohamedfaridelsherbini.nexar.navigation",
                    "com.mohamedfaridelsherbini.nexar.notifications",
                    "com.mohamedfaridelsherbini.nexar.platform",
                    "com.mohamedfaridelsherbini.nexar.storage",
                    "com.mohamedfaridelsherbini.nexar.widget",
                    "com.mohamedfaridelsherbini.nexar.di",
                    "com.mohamedfaridelsherbini.nexar.data.db",
                    "com.mohamedfaridelsherbini.nexar.data.repo",
                    "nexar.composeapp.generated.resources",
                )
                classes(
                    "com.mohamedfaridelsherbini.nexar.AppKt",
                    "com.mohamedfaridelsherbini.nexar.BuildConfig",
                    "com.mohamedfaridelsherbini.nexar.ComposableSingletons*",
                    "com.mohamedfaridelsherbini.nexar.MainActivity*",
                    "com.mohamedfaridelsherbini.nexar.NexarApplication*",
                    "com.mohamedfaridelsherbini.nexar.ScannerBridge*",
                    "com.mohamedfaridelsherbini.nexar.domain.usecase.AndroidOcrProcessor*",
                    "com.mohamedfaridelsherbini.nexar.domain.usecase.IosOcrProcessor*",
                    "com.mohamedfaridelsherbini.nexar.domain.usecase.NexarSettingsPreferences",
                    "com.mohamedfaridelsherbini.nexar.domain.usecase.OcrProcessor_*",
                )
            }
        }
        verify {
            rule {
                minBound(80)
            }
        }
    }
}
