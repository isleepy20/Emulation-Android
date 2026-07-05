<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />

# 🍪 Cookie Clicker: Bakery Edition

An addictive, highly-polished offline-first mobile Cookie Clicker game featuring progressive upgrades, achievements, offline production, clicking fever events, and interactive live stats!

## 📲 Download & Install APK

### 🌟 Option 1: Direct Download from Google AI Studio (Recommended & Instantaneous)
If you are inside the Google AI Studio environment, you can download the clean compiled APK instantly without waiting for GitHub to synchronize:
1. Open the **Settings** or **Project Menu** in the AI Studio interface.
2. Select **Generate APK** (or **Download APK** / **Export**).
3. This will trigger a direct, clean download of the latest compiled APK directly from the workspace to your device.

---

### 🌐 Option 2: Download from GitHub Repository (via jsDelivr CDN - Guaranteed No `.bin` or Parse Errors)
We route the GitHub file through **jsDelivr CDN**, which explicitly serves `.apk` files with the correct Android MIME header (`application/vnd.android.package-archive`). This ensures that mobile Chrome downloads it directly as `CookieClicker.apk` (never `.bin`) for effortless, immediate installation!

[![Download APK](https://img.shields.io/badge/Download-CookieClicker.apk-success?style=for-the-badge&logo=android&logoColor=white)](https://cdn.jsdelivr.net/gh/isleepy20/Emulation-Android@main/CookieClicker.apk)

*Alternative mirror (GitHack CDN):* [Download via GitHack](https://raw.githack.com/isleepy20/Emulation-Android/main/CookieClicker.apk)

> [!TIP]
> **Why is this CDN route better?**
> 1. **No MIME Type Renaming (`.bin`)**: Standard GitHub raw links (`raw.githubusercontent.com`) serve files as generic binary streams, causing mobile browsers to rename them to `.apk.bin`. jsDelivr overrides this to enforce the correct Android MIME type.
> 2. **Instant Delivery**: jsDelivr is extremely reliable, high-speed, and serves the latest `CookieClicker.apk` compiled directly from the `main` branch. If you get a parse error, it means the compilation is still finishing up—try again in 10 seconds or use **Option 1 (AI Studio Direct Download)**!

### ⚙️ How to Install:
1. Use either of the options above to download the `CookieClicker.apk` file.
2. Open the downloaded file on your device (if your browser still downloads it with a `.bin` extension, simply rename the file to remove `.bin` and keep `.apk`).
3. If prompted, enable **"Install from Unknown Sources"** for your browser or file manager.
4. Tap **Install** and enjoy baking endless cookies! 🍪🔥
</div>

---

## 🛠️ View in AI Studio
View and iterate on your app in AI Studio: https://ai.studio/apps/c981b2a3-9adc-42c5-869c-1992452bb0e5

## 💻 Run Locally

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
