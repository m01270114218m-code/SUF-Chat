// lib/main.dart
import 'package:flutter/material.dart';
import 'package:socket_io_client/socket_io_client.dart' as IO;

void main() {
  SocketService.initSocket();
  runApp(const MyApp());
}

// Socket Service
class SocketService {
  static late IO.Socket socket;
  
  static void initSocket() {
    // Backend URL - Update with your server URL
    socket = IO.io('http://localhost:3000', <String, dynamic>{
      'transports': ['websocket'],
      'autoConnect': true,
    });
    
    socket.on('connect', (_) {
      print('✅ Connected to backend');
    });
    
    socket.on('disconnect', (_) {
      print('❌ Disconnected from backend');
    });
  }
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData.dark(),
      home: const MainNavigationPage(),
    );
  }
}

// --- 1. HOME & LOBBY NAVIGATION BASE ---
class MainNavigationPage extends StatefulWidget {
  const MainNavigationPage({super.key});

  @override
  State<MainNavigationPage> createState() => _MainNavigationPageState();
}

class _MainNavigationPageState extends State<MainNavigationPage> {
  int _currentIndex = 0;
  final List<Widget> _pages = [
    const HomePage(),
    const GameLobbyPage(),
    const MessagePage(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _pages[_currentIndex],
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (index) => setState(() => _currentIndex = index),
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home), label: 'Home'),
          BottomNavigationBarItem(icon: Icon(Icons.videogame_asset), label: 'Lobby'),
          BottomNavigationBarItem(icon: Icon(Icons.message), label: 'Messages'),
        ],
      ),
    );
  }
}

// --- 2. HOME PAGE (ROOM LISTS) ---
class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Trending Live Rooms')),
      body: ListView.builder(
        itemCount: 5,
        itemBuilder: (context, index) {
          return Card(
            margin: const EdgeInsets.all(10),
            child: ListTile(
              leading: const CircleAvatar(backgroundColor: Colors.purple, child: Icon(Icons.graphic_eq)),
              title: Text('Rockstars Club Zone ${index + 1}'),
              subtitle: const Text('🔥 Playing Ludo | 10 Seats'),
              trailing: ElevatedButton(
                onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const VoiceChatRoom())),
                child: const Text('Join'),
              ),
            ),
          );
        },
      ),
    );
  }
}

// --- 3. GAME LOBBY PAGE ---
class GameLobbyPage extends StatelessWidget {
  const GameLobbyPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Game Lobby')),
      body: GridView.count(
        crossAxisCount: 2,
        padding: const EdgeInsets.all(15),
        children: ['Ludo Classic', 'Teen Patti VIP', 'Carrom Master', 'Domino King'].map((game) {
          return Card(
            color: Colors.blueGrey[800],
            child: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.casino, size: 50, color: Colors.amber),
                  const SizedBox(height: 10),
                  Text(game, style: const TextStyle(fontWeight: FontWeight.bold)),
                ],
              ),
            ),
          );
        }).toList(),
      ),
    );
  }
}

// --- 4. MESSAGE & BLOCK PAGE ---
class MessagePage extends StatelessWidget {
  const MessagePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Direct Messages')),
      body: ListView(
        children: [
          ListTile(
            leading: const CircleAvatar(child: Text('U1')),
            title: const Text('Rahul Kumar'),
            subtitle: const Text('Hey! Lets play Ludo.'),
            trailing: IconButton(
              icon: const Icon(Icons.block, color: Colors.red),
              onPressed: () {
                ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('User Blocked Successfully!')));
              },
            ),
          )
        ],
      ),
    );
  }
}

// --- 5. CHAT ROOM PAGE (10 SEATS LAYOUT, MARQUEE BROADCAST & CONTROLS) ---
class VoiceChatRoom extends StatefulWidget {
  const VoiceChatRoom({super.key});

  @override
  State<VoiceChatRoom> createState() => _VoiceChatRoomState();
}

class _VoiceChatRoomState extends State<VoiceChatRoom> {
  bool isMuted = false;
  bool isSpeakerOn = true;
  String currentBroadcast = "📢 Welcome to the Chat Room! Send gifts to support hosts.";

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.indigo[900],
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: const Text('👑 King\'s Mansion Room'),
        actions: [
          IconButton(icon: const Icon(Icons.minimize), onPressed: () => Navigator.pop(context)),
          IconButton(icon: const Icon(Icons.close, color: Colors.red), onPressed: () => Navigator.pop(context)),
        ],
      ),
      body: Column(
        children: [
          // 1. Top Scrolling Live Broadcasting Marquee Banner
          Container(
            color: Colors.black54,
            width: double.infinity,
            padding: const EdgeInsets.symmetric(vertical: 6, horizontal: 10),
            child: SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Text(currentBroadcast, style: const TextStyle(color: Colors.amber, fontWeight: FontWeight.bold)),
            ),
          ),
          const SizedBox(height: 20),

          // 2. 10 Seats Matrix Grid Layout
          Expanded(
            child: GridView.builder(
              padding: const EdgeInsets.all(15),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(crossAxisCount: 5, mainAxisSpacing: 15, crossAxisSpacing: 15),
              itemCount: 10,
              itemBuilder: (context, index) {
                bool isKing = index == 0;
                return Column(
                  children: [
                    CircleAvatar(
                      radius: 24,
                      backgroundColor: isKing ? Colors.amber : Colors.grey[700],
                      child: Icon(isKing ? Icons.star : Icons.person, color: isKing ? Colors.black : Colors.white),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      isKing ? 'King' : 'Seat ${index + 1}',
                      style: TextStyle(fontSize: 11, color: isKing ? Colors.amber : Colors.white70),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                );
              },
            ),
          ),

          // 3. Bottom Controls Panel Bar
          Container(
            color: Colors.black87,
            padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 15),
            child: Column(
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    // Mic Mute Option
                    IconButton(
                      icon: Icon(isMuted ? Icons.mic_off : Icons.mic, color: isMuted ? Colors.red : Colors.green),
                      onPressed: () => setState(() => isMuted = !isMuted),
                    ),
                    // Speaker Control
                    IconButton(
                      icon: Icon(isSpeakerOn ? Icons.volume_up : Icons.volume_off, color: Colors.white),
                      onPressed: () => setState(() => isSpeakerOn = !isSpeakerOn),
                    ),
                    // Launch Game Inside Room
                    IconButton(
                      icon: const Icon(Icons.videogame_asset, color: Colors.cyan),
                      onPressed: () => _showGamePanel(context),
                    ),
                    // Settings/Rewards
                    IconButton(
                      icon: const Icon(Icons.settings, color: Colors.grey),
                      onPressed: () => _showSettingsPanel(context),
                    ),
                    // Gifts / Self Gifting Panel Trigger
                    IconButton(
                      icon: const Icon(Icons.card_giftcard, color: Colors.pinkAccent, size: 32),
                      onPressed: () => _showGiftPanel(context),
                    ),
                  ],
                ),
                // Text Message Box In Room
                TextField(
                  decoration: InputDecoration(
                    hintText: "Type live message...",
                    hintStyle: const TextStyle(color: Colors.white54, fontSize: 13),
                    suffixIcon: IconButton(icon: const Icon(Icons.send, color: Colors.blue), onPressed: () {}),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  void _showGiftPanel(BuildContext context) {
    showModalBottomSheet(
      context: context,
      builder: (context) {
        return Container(
          height: 250,
          padding: const EdgeInsets.all(15),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('Send Gifts (Self Gifting Enabled)', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
              const SizedBox(height: 15),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  _giftItem(context, '👑 Crown', '1000 Coins'),
                  _giftItem(context, '🚀 Rocket', '500 Coins'),
                  _giftItem(context, '🌹 Rose', '10 Coins'),
                ],
              ),
              const SizedBox(height: 15),
              const Text('Select Emojis (Min 20 Available)', style: TextStyle(color: Colors.grey)),
              const SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(children: [Text('😂❤️🔥😭👍🎉😎🌟👏✨🥳💔👑💯🚀🍀🍿🍬🎈🎂🎸🕺')]),
              )
            ],
          ),
        );
      },
    );
  }

  Widget _giftItem(BuildContext context, String name, String cost) {
    return ElevatedButton(
      onPressed: () {
        Navigator.pop(context);
        setState(() => currentBroadcast = "🎁 User1 sent $name ($cost) to King Seat! 🚀 Distribution Triggered.");
      },
      child: Column(children: [Text(name, style: const TextStyle(fontSize: 24)), Text(cost, style: const TextStyle(fontSize: 10))]),
    );
  }

  void _showSettingsPanel(BuildContext context) {
    showModalBottomSheet(
      context: context,
      builder: (context) {
        return Container(
          padding: const EdgeInsets.all(20),
          height: 180,
          child: Column(
            children: [
              ListTile(
                leading: const Icon(Icons.card_giftcard, color: Colors.amber),
                title: const Text('Collect 5% Room Reward'),
                onTap: () => Navigator.pop(context),
              ),
              ListTile(
                leading: const Icon(Icons.color_lens, color: Colors.green),
                title: const Text('Change Room Theme Wallpaper'),
                onTap: () => Navigator.pop(context),
              ),
            ],
          ),
        );
      },
    );
  }

  void _showGamePanel(BuildContext context) {
    showModalBottomSheet(
      context: context,
      builder: (context) => Container(
        height: 150,
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            const Text('Start Mini-Games inside Room'),
            ElevatedButton(
              onPressed: () {
                Navigator.pop(context);
                setState(() => currentBroadcast = "🏆 Victory! King Rahul won 500 Coins in Ludo Master!");
              },
              child: const Text('Launch Ludo Classic'),
            )
          ],
        ),
      ),
    );
  }
}
