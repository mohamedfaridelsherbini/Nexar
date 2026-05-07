# Nexar iOS App Icon Setup

To set up the app icon for the iOS version of Nexar:

1.  Open the `iosApp` project in **Xcode**.
2.  Navigate to `iosApp > Assets.xcassets`.
3.  Select `AppIcon`.
4.  Drag and drop the appropriate icon sizes into the placeholder slots.
    *   You can use the adaptive icon design from the Android version (located in `composeApp/src/androidMain/res/drawable/ic_launcher_foreground.xml`) as a reference.
    *   Since iOS icons must be flat images (no transparency required for the background), ensure you provide a square image with the vibrant blue background (`#0061A4`) and the white scanner logo centered.

## Recommended Tool
You can use tools like [App Icon Generator](https://appicon.co/) to generate all necessary sizes from a single high-resolution image (1024x1024).
