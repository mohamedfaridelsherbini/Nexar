# Kotlin Static Analysis Setup

This project uses **detekt** and **ktlint** to maintain code quality.

## Android Studio Setup

1. **EditorConfig**:
   - Ensure **Settings > Editor > Code Style > Enable EditorConfig support** is checked.
   - This will automatically apply the rules in the root `.editorconfig`.

2. **Detekt Plugin**:
   - Install the "detekt" plugin from the Marketplace.
   - In **Settings > Tools > detekt**:
     - Check **Enable detekt**.
     - Configuration file: `config/detekt/detekt.yml`.
     - Baseline file: `config/detekt/baseline.xml`.

3. **Check/Format via Gradle**:
   - `./gradlew ktlintCheck`: Check for style violations.
   - `./gradlew ktlintFormat`: Automatically fix style violations.
   - `./gradlew detekt`: Run static analysis.
