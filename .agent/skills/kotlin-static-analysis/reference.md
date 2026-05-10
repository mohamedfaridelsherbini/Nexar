# Kotlin analysis — reference snippets

## detekt (Gradle Kotlin DSL sketch)

```kotlin
plugins {
    id("io.gitlab.arturbosch.detekt") version "<version>"
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline = file("$rootDir/config/detekt-baseline.xml") // optional
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:<version>")
}
```

## ktlint (Gradle plugin sketch)

```kotlin
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "<version>"
}

ktlint {
    filter { exclude { it.file.path.contains("/build/") } }
}
```

## Commands

```bash
./gradlew detekt
./gradlew ktlintCheck
./gradlew ktlintFormat
./gradlew :composeApp:lintDebug
```

Replace module and variant names to match the repo.
