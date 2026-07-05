<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />

# 🍪 Cookie Clicker: Bakery Edition

An addictive, highly-polished offline-first mobile Cookie Clicker game featuring progressive upgrades, achievements, offline production, clicking fever events, and interactive live stats!

## 📲 Download & Install APK

Choose either of the two direct methods below to get the application:

### 🌟 Option 1: Direct Download from Google AI Studio (Instant & Recommended)
This is the fastest, safest, and most reliable method:
1. Open the **Settings** or **Project Menu** in the Google AI Studio sidebar.
2. Select **Generate APK** (or **Download APK** / **Export Project**).
3. The latest compiled `CookieClicker.apk` will download directly from the workspace to your device.

---

### 🌐 Option 2: Download from GitHub Repository
To download directly from the official repository:
1. Navigate to the official file page on GitHub:
   👉 **[Click Here to open CookieClicker.apk on GitHub](https://github.com/isleepy20/CookieClicker/blob/main/CookieClicker.apk)**
2. Tap the blue **"View raw"** link (or the **"Download"** button if on desktop). This will download the actual `CookieClicker.apk` file directly to your device! (GitHub says it can't show the file because it is a binary app, which is completely normal).

---

### ⚙️ How to Install:
1. Download `CookieClicker.apk` using one of the options above.
2. Open the downloaded file on your Android device.
3. If prompted, enable **"Install from Unknown Sources"** for your file explorer or browser.
4. Tap **Install** and start baking endless cookies! 🍪🔥
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
