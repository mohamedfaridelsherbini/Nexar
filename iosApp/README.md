# Swift Static Analysis Setup

This project uses **SwiftLint** to maintain Swift code quality in the `iosApp` target.

## Setup

1. **Install SwiftLint**:
   ```bash
   brew install swiftlint
   ```

2. **Xcode Integration**:
   The configuration lives in `iosApp/.swiftlint.yml`. SwiftLint is best run as a Build Phase in Xcode to surface warnings directly in the editor.

3. **Check via CLI**:
   Run the following command from the root of the repository:
   ```bash
   swiftlint lint iosApp --strict
   ```

4. **Auto-correct**:
   To automatically fix simple formatting issues:
   ```bash
   swiftlint --fix iosApp
   ```
