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

### 🌐 Option 2: Download from GitHub Repository
You can download the compiled APK file directly from this repository by clicking the badge below:

[![Download APK](https://img.shields.io/badge/Download-CookieClicker.apk-success?style=for-the-badge&logo=android&logoColor=white)](https://raw.githack.com/isleepy20/Emulation-Android/main/CookieClicker.apk)

> [!IMPORTANT]
> **Why did you see `.apk.bin` or "Problem Parsing Package" before?**
> 1. **MIME Type Renaming (`.bin`)**: Mobile browsers like Google Chrome often append `.bin` to files downloaded directly from GitHub's raw links (`raw.githubusercontent.com`) because GitHub serves raw repository files as generic binary data. We now route the link through **GitHack CDN** to enforce the correct Android MIME header (`application/vnd.android.package-archive`).
> 2. **Parsing Error**: If you click the link immediately after a change is made, GitHub might still be synchronizing the file, or the CDN might serve a cached `404 Not Found` HTML page. When Android tries to install this HTML page, it triggers a "There was a problem parsing the package" error. If this happens, please use **Option 1 (AI Studio Direct Download)** or wait a few minutes for GitHub to finish syncing!

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
