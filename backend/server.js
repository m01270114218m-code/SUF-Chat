// server.js
const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

const server = http.createServer(app);
const io = new Server(server, { cors: { origin: "*" } });

// Fake Databases
let users = {
    "user1": { name: "King Rahul", gold: 10000, diamonds: 0, rubies: 0 },
    "user2": { name: "Simran", gold: 500, diamonds: 0, rubies: 0 }
};
let agency = { id: "agency1", diamonds: 0 };
let roomRewardPool = 0;

// Socket.io for Live Voice Room Interactions & Broadcasting
io.on('connection', (socket) => {
    console.log('User connected: ' + socket.id);

    socket.on('join_room', (roomId) => {
        socket.join(roomId);
        console.log(`User joined room: ${roomId}`);
    });

    // Handle Gifting & Live Broadcast
    socket.on('send_gift', (data) => {
        const { senderId, receiverId, roomId, giftName, cost } = data;
        
        if (users[senderId] && users[senderId].gold >= cost) {
            // Deduct from Sender
            users[senderId].gold -= cost;

            // Distribution Logic (40%, 20%, 10%, 5%)
            const dReceiver = cost * 0.40;
            const rReceiver = cost * 0.20;
            const dAgency = cost * 0.10;
            const rRoom = cost * 0.05;

            if(users[receiverId]) {
                users[receiverId].diamonds += dReceiver;
                users[receiverId].rubies += rReceiver;
            }
            agency.diamonds += dAgency;
            roomRewardPool += rRoom;

            // 1. Gift Broadcast
            io.to(roomId).emit('broadcast', {
                type: 'GIFT',
                message: `🎁 ${users[senderId].name} sent ${giftName} to ${users[receiverId]?.name || 'Seat'}!`
            });
        }
    });

    // 2. Game Win Broadcast
    socket.on('game_win', (data) => {
        io.to(data.roomId).emit('broadcast', {
            type: 'GAME',
            message: `🏆 ${data.userName} won ${data.amount} Gold Coins in Ludo!`
        });
    });

    // 3. Lucky Bag Broadcast
    socket.on('lucky_bag', (data) => {
        io.to(data.roomId).emit('broadcast', {
            type: 'LUCKY_BAG',
            message: `💰 Lucky Bag Opened by ${data.userName}! 🔥`
        });
    });
});

// Ruby Exchange API (1 Ruby = 1 Gold)
app.post('/api/exchange-ruby', (req, res) => {
    const { userId, amount } = req.body;
    if (users[userId] && users[userId].rubies >= amount) {
        users[userId].rubies -= amount;
        users[userId].gold += amount;
        return res.json({ success: true, wallet: users[userId] });
    }
    res.status(400).json({ success: false, message: "Invalid amount" });
});

// GET endpoint for testing
app.get('/api/users', (req, res) => {
    res.json(users);
});

app.get('/', (req, res) => {
    res.json({ message: 'Voice Chat Backend Running!' });
});

server.listen(3000, () => console.log('Backend running on port 3000'));
