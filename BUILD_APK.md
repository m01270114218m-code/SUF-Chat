# APK Build Instructions 📱

## Prerequisites
- Flutter SDK installed
- Android SDK installed
- Environment variables configured

## Step 1: Clone or Download Repository

```bash
git clone https://github.com/umarfarooquera-commits/SUF-Chat.git
cd SUF-Chat
```

## Step 2: Get Flutter Dependencies

```bash
cd flutter_app
flutter pub get
```

## Step 3: Build APK

### Option A: Build Release APK (Default)

```bash
flutter build apk --release
```

**Output:** `flutter_app/build/app/outputs/flutter-apk/app-release.apk`

### Option B: Build Split APKs (Smaller Size)

```bash
flutter build apk --split-per-abi
```

**Output:** 
- `flutter_app/build/app/outputs/flutter-apk/app-armeabi-v7a-release.apk`
- `flutter_app/build/app/outputs/flutter-apk/app-arm64-v8a-release.apk`
- `flutter_app/build/app/outputs/flutter-apk/app-x86-release.apk`
- `flutter_app/build/app/outputs/flutter-apk/app-x86_64-release.apk`

## Step 4: Find Your APK

After build completes:

```bash
ls flutter_app/build/app/outputs/flutter-apk/
```

You'll see: `app-release.apk` (usually 50-60 MB)

## Step 5: Install on Android Device

### Using ADB:

```bash
# Connect your Android device via USB
# Enable Developer Mode on your phone

adb install flutter_app/build/app/outputs/flutter-apk/app-release.apk
```

### Manual Installation:

1. Transfer APK to your phone via USB/email
2. Open file manager on phone
3. Go to Settings → Security → Enable "Unknown Sources"
4. Tap the APK file
5. Click "Install"
6. Open the app!

## Step 6: Test the App

1. Open Voice Chat App
2. Navigate through Home → Lobby → Messages tabs
3. Click "Join" on any room
4. Test gift sending (click 💝 icon)
5. Test game launcher

## APK Specifications

| Property | Value |
|----------|-------|
| App Name | Voice Chat App |
| Package | com.example.voice_chat_app |
| Minimum SDK | 21 (Android 5.0+) |
| Target SDK | 33+ |
| Size | ~50-60 MB |
| Platforms | ARM, x86 |

## Troubleshooting

### Build Fails with "Flutter SDK not found"

```bash
flutter config --android-sdk /path/to/android/sdk
flutter doctor
```

### APK Installation Fails

```bash
# Uninstall previous version
adb uninstall com.example.voice_chat_app

# Reinstall
adb install flutter_app/build/app/outputs/flutter-apk/app-release.apk
```

### "gradle build failed"

```bash
cd flutter_app
flutter clean
flutter pub get
flutter build apk --release
```

### "JAVA_HOME not set"

```bash
# Set JAVA_HOME
export JAVA_HOME=/usr/libexec/java_home
# Or for Windows: set JAVA_HOME=C:\Program Files\Java\jdk-XX

flutter build apk --release
```

## Download Directly (GitHub Releases)

Once built, you can upload to GitHub:

1. Go to your repository
2. Create a Release
3. Upload `app-release.apk`
4. Share download link

## Size Optimization

### Reduce APK Size:

```bash
# Build with split APKs (faster download)
flutter build apk --split-per-abi

# For 64-bit only:
flutter build apk --target-platform android-arm64
```

## Build Time Tips

- First build: 5-10 minutes
- Subsequent builds: 2-3 minutes
- Use `--release` for final APK
- Use `--debug` for testing

## Next Steps

1. ✅ APK built
2. ✅ Tested on device
3. 📝 Deploy backend server
4. 📝 Update socket URL in code
5. 📝 Upload to Google Play Store

---

**APK Ready to Use!** 🚀
