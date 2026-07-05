<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />

# 🍪 Cookie Clicker: Bakery Edition

An addictive, highly-polished offline-first mobile Cookie Clicker game featuring progressive upgrades, achievements, offline production, clicking fever events, and interactive live stats!

## 📲 Download & Install APK

You can download the compiled APK file directly to your Android device from the repository by clicking the button below:

[![Download APK](https://img.shields.io/badge/Download-CookieClicker.apk-success?style=for-the-badge&logo=android&logoColor=white)](https://raw.githack.com/isleepy20/Emulation-Android/main/CookieClicker.apk)

> [!NOTE]
> **Why did you see `.apk.bin` before?** 
> Mobile browsers like Google Chrome often append `.bin` to files downloaded directly from GitHub's raw link (`raw.githubusercontent.com`) because GitHub serves raw repository files as generic binary data. 
> To prevent this and make downloading frictionless, we now route the download through **GitHack CDN**, which sends the correct Android APK headers (`application/vnd.android.package-archive`). The button above will download clean, unconfusing `CookieClicker.apk` directly to your phone!

### ⚙️ How to Install:
1. Click the **Download APK** badge above on your Android device.
2. Open the downloaded `CookieClicker.apk` file on your device (if your browser still downloads it with a `.bin` extension, simply rename the file to remove `.bin` and keep `.apk`).
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
