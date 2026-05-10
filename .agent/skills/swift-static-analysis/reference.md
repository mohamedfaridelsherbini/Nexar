# Swift analysis — reference

## Minimal `.swiftlint.yml` starter

```yaml
disabled_rules: []
opt_in_rules:
  - explicit_init
  - closure_spacing
line_length: 140
identifier_name:
  min_length: 2
excluded:
  - Pods
  - .build
```

Tune per project; keep excludes aligned with generated paths.

## GitHub Actions sketch (macOS runner)

```yaml
jobs:
  swiftlint:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
      - name: SwiftLint
        run: swiftlint lint --strict
        working-directory: iosApp
```

Adjust `working-directory` if `.swiftlint.yml` lives at repo root.

## Analyzer

```bash
xcodebuild -project iosApp/Nexar.xcodeproj -scheme Nexar -destination 'platform=iOS Simulator,name=iPhone 16' analyze
```

Use `.xcworkspace` if the project uses CocoaPods.
