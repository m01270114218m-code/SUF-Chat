# Voice Chat App - Gaming & Gifting Platform 🎮

Real-time voice chat app with gaming, gifting, and live broadcasting features.

## 📁 Project Structure

```
voice-chat-app/
├── backend/                 # Node.js + Socket.io Server
│   ├── server.js           # Main backend server
│   ├── package.json        # Node dependencies
│   └── .gitignore
│
├── flutter_app/            # Flutter Mobile Application
│   ├── lib/
│   │   ├── main.dart       # Complete UI & Logic
│   │   └── screens/
│   ├── pubspec.yaml        # Flutter dependencies
│   └── android/            # Android build config
│
└── README.md
```

## ✨ Features

✅ **Real-time Voice Chat** - Socket.io powered live communication
✅ **10-Seat Gaming Layout** - King seat + 9 user seats
✅ **Gift Distribution System** - 40/20/10/5% split logic
✅ **Game Integration** - Ludo, Teen Patti, Carrom, Domino
✅ **Live Broadcast Marquee** - Real-time event notifications
✅ **User Wallet System** - Gold, Diamonds, Rubies currencies
✅ **Direct Messaging** - User-to-user communication
✅ **Block User Feature** - Control interactions
✅ **Self-Gifting Support** - Users can gift to any seat
✅ **Room Reward Pool** - Collective earnings tracking

## 🚀 Quick Start

### Backend Setup

```bash
cd backend
npm install
npm start
```

Backend runs on `http://localhost:3000`

### Flutter App Setup

```bash
cd flutter_app
flutter pub get
flutter run
```

## 📱 Build APK

```bash
cd flutter_app
flutter build apk --release
```

**APK Location:** `flutter_app/build/app/outputs/flutter-apk/app-release.apk`

## 🔧 Backend API Endpoints

### Socket.io Events

- **join_room** - User joins a voice chat room
- **send_gift** - Send gift with automatic distribution
- **game_win** - Broadcast game victory
- **lucky_bag** - Lucky bag opened event

### REST API

```
GET  /                    - Test endpoint
GET  /api/users           - Get all users
POST /api/exchange-ruby   - Convert Ruby to Gold (1:1)
```

## 💰 Gift Distribution Logic

When a user sends a gift worth X coins:
- **Receiver:** 40% Diamonds + 20% Rubies
- **Agency:** 10% Diamonds
- **Room Pool:** 5% Rubies
- **Platform:** 25% (implicit)

## 🔐 User Wallet

```javascript
{
  "name": "King Rahul",
  "gold": 10000,      // Spending currency
  "diamonds": 0,      // Premium currency from gifts
  "rubies": 0         // Exchange currency
}
```

## 📚 Room Features

- **King Seat** - Premium position (Seat 1)
- **User Seats** - Seats 2-10
- **Live Marquee** - Real-time event broadcasts
- **Voice Controls** - Mute, Speaker on/off
- **Message Box** - Live chat in room
- **Game Launcher** - Play mini-games during stream

## 🎮 Supported Games

1. Ludo Classic
2. Teen Patti VIP
3. Carrom Master
4. Domino King

## 🔄 Data Flow

```
User Action (Gift Send)
         ↓
   Socket.io Event
         ↓
   Backend Processing
         ↓
   Update User Wallets
         ↓
   Broadcast to Room
         ↓
   Display in Live Marquee
```

## 📦 Dependencies

### Backend
- `express` - Web framework
- `socket.io` - Real-time communication
- `cors` - Cross-origin support

### Frontend
- `flutter` - UI framework
- `socket_io_client` - Socket.io client for Flutter

## 🚢 Deployment

For production deployment:
1. Update backend URL in `flutter_app/lib/main.dart`
2. Deploy backend to cloud (Heroku, AWS, Railway, etc.)
3. Build release APK
4. Publish to Google Play Store

## 📝 Development Notes

- Default backend URL: `http://localhost:3000`
- Update socket URL for production deployments
- Ensure CORS is properly configured
- Test WebSocket connectivity before deployment

## 📧 Support

For issues or questions, create a GitHub issue.

---

**Happy Coding!** 🚀
