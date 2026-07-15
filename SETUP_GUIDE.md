# Complete Setup Guide 📖

## Prerequisites

- Node.js (v14+) installed
- Flutter SDK installed
- Android Studio (for Android build)
- Git

## Step 1: Backend Setup

```bash
# Navigate to backend directory
cd backend

# Install dependencies
npm install

# Start the server
npm start
```

✅ Server should log: "Backend running on port 3000"

## Step 2: Flutter App Setup

```bash
# Navigate to flutter app directory
cd flutter_app

# Get dependencies
flutter pub get

# Run on connected device or emulator
flutter run
```

## Step 3: Update Socket Connection (Important!)

Edit `flutter_app/lib/main.dart` and update the socket URL:

```dart
// For Local Development:
socket = IO.io('http://localhost:3000', <String, dynamic>{
  'transports': ['websocket'],
  'autoConnect': true,
});

// For Production (replace with actual server):
socket = IO.io('https://your-backend-url.com', <String, dynamic>{
  'transports': ['websocket'],
  'autoConnect': true,
});
```

## Step 4: Build APK

```bash
cd flutter_app

# Build release APK
flutter build apk --release

# For split APKs (smaller size):
flutter build apk --split-per-abi
```

**Output:** `flutter_app/build/app/outputs/flutter-apk/app-release.apk`

## Step 5: Install APK on Phone

```bash
# Connect your Android device via USB
adb install flutter_app/build/app/outputs/flutter-apk/app-release.apk

# Or manually:
# 1. Transfer APK to phone
# 2. Open file manager
# 3. Enable "Unknown Sources" in Settings
# 4. Tap APK to install
```

## Testing the App

### Test Socket Connection

1. Start backend: `npm start` in backend folder
2. Run Flutter app
3. Check console for "✅ Connected to backend"

### Test Gift Sending

1. Open app
2. Join a room
3. Click gift icon (💝)
4. Select a gift
5. Check broadcast marquee for message

### Test Messages

1. Go to Messages tab
2. Click on a user
3. Send a message
4. Verify display

## Troubleshooting

### Backend Won't Start

```bash
# Check if port 3000 is already in use
lsof -i :3000

# Kill existing process
kill -9 <PID>

# Try again
npm start
```

### Flutter App Won't Connect to Backend

1. Check if backend is running
2. Verify socket URL is correct
3. Check firewall settings
4. Try `http://10.0.2.2:3000` for Android emulator

### APK Won't Install

```bash
# Clear previous installation
adb uninstall com.example.voice_chat_app

# Reinstall
adb install flutter_app/build/app/outputs/flutter-apk/app-release.apk
```

### Build Errors

```bash
# Clean and rebuild
flutter clean
flutter pub get
flutter build apk --release
```

## Environment Variables

Create `backend/.env` file:

```
PORT=3000
NODE_ENV=development
```

## Next Steps

1. ✅ Backend running
2. ✅ Flutter app connected
3. ✅ APK built and tested
4. 📝 Deploy backend to cloud
5. 📝 Update socket URL for production
6. 📝 Publish to Google Play Store

---

For more details, check the main README.md
