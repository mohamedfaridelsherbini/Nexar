plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover) apply false
}

subprojects {
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)
    apply(plugin = rootProject.libs.plugins.ktlint.get().pluginId)

    dependencies {
        "detektPlugins"(rootProject.libs.detekt.formatting)
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        baseline = file("$rootDir/config/detekt/baseline.xml")
        buildUponDefaultConfig = true
        allRules = false
        autoCorrect = true
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(false)
        verbose.set(true)
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        filter {
            exclude { it.file.path.contains("generated") || it.file.path.contains("build") }
        }
    }
}
