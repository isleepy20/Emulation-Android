<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />

# 🍪 Cookie Clicker: Bakery Edition

An addictive, highly-polished offline-first mobile Cookie Clicker game featuring progressive upgrades, achievements, offline production, clicking fever events, and interactive live stats!

## 📲 Download & Install APK

If you are getting a **"Cannot Download"**, **"Failed to Parse"**, or **`.bin` extension** error, here are the exact solutions:

### 🌟 Solution 1: Direct Download from Google AI Studio (Guaranteed & Safest)
The most reliable way to download the clean compiled APK instantly without any cache or repository settings issues is straight from the AI Studio Workspace:
1. Look at the top-right / sidebar of the Google AI Studio interface.
2. Tap the **Settings icon** (or the **Project Menu**).
3. Select **Generate APK** (or **Download APK** / **Export Project**).
4. This downloads the latest compiled `CookieClicker.apk` directly to your local computer/device with no intermediate services!

---

### 🌐 Solution 2: Official GitHub Download (Guaranteed to bypass `.bin` and CDNs)
If your repository is **private**, CDNs like jsDelivr or GitHack will fail to access it and instead download a `404 Not Found` HTML page, resulting in a parsing error on your phone. To download directly from the secure repository:
1. Navigate directly to the official file page on GitHub:
   👉 **[Click Here to open CookieClicker.apk on GitHub](https://github.com/isleepy20/Emulation-Android/blob/main/CookieClicker.apk)**
2. Tap the **"Download"** button (or the "View Raw" icon) on that page.
3. Because you are using the official GitHub page, **mobile Google Chrome will download it perfectly as `CookieClicker.apk` without renaming it to `.bin`** and without any CDN errors!

---

### ⚡ Solution 3: Direct CDN Hotlinks (For Public Repositories Only)
*If and only if* your GitHub repository is public, you can use these direct download badges:

[![Download APK](https://img.shields.io/badge/Download-CookieClicker.apk-success?style=for-the-badge&logo=android&logoColor=white)](https://cdn.jsdelivr.net/gh/isleepy20/Emulation-Android@main/CookieClicker.apk)

*Alternative mirror (GitHack CDN):* [Download via GitHack](https://raw.githack.com/isleepy20/Emulation-Android/main/CookieClicker.apk)

---

### ⚙️ How to Install:
1. Use any of the solutions above to get the `CookieClicker.apk` file on your Android device.
2. **If your browser still appends `.bin`** (making it `CookieClicker.apk.bin`), simply open your device's File Manager, locate the downloaded file, select **Rename**, and delete `.bin` from the end so it ends with exactly `.apk`.
3. Tap on `CookieClicker.apk` to begin installation.
4. If prompted, enable **"Install from Unknown Sources"** for your browser or file manager.
5. Tap **Install** and start baking endless cookies! 🍪🔥
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
