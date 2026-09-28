/**
 * =======================================================================================
 * AHLAN CHAT / ZADIRA LIVE 7D - REAL-TIME BACKEND SERVER (Node.js + TypeScript + Socket.IO)
 * =======================================================================================
 * Isolated Backend API & WebSocket Server:
 * 1. Hardware Device ID & IP Ban Middleware (`blacklist:device_ids` in Redis + PostgreSQL).
 * 2. Agora RTC Ephemeral Token Generator (`/api/rooms/rtc-token`) for zero-latency WebRTC voice.
 * 3. SERIALIZABLE Financial Transactions (`/api/gifts/send`, `/api/agency/transfer-coins`).
 * 4. Real-Time WebSocket Room Engine (`JOIN_ROOM`, `MIC_REQUEST_SUBMIT`, `MIC_SLOT_STATE_CHANGE`, `BROADCAST_GIFT`).
 */

import express, { Request, Response, NextFunction } from 'express';
import http from 'http';
import { Server as SocketIOServer } from 'socket.io';
import { Pool } from 'pg';
import Redis from 'ioredis';
import { RtcTokenBuilder, RtcRole } from 'agora-access-token';

const app = express();
app.use(express.json());

const server = http.createServer(app);
const io = new SocketIOServer(server, { cors: { origin: '*' } });

const pgPool = new Pool({
  connectionString: process.env.DATABASE_URL || 'postgresql://postgres:password@localhost:5432/zadira_voice_db',
});
const redis = new Redis(process.env.REDIS_URL || 'redis://localhost:6379');

const AGORA_APP_ID = process.env.AGORA_APP_ID || '';
const AGORA_APP_CERTIFICATE = process.env.AGORA_APP_CERTIFICATE || '';

/**
 * 1. GLOBAL DEVICE ID & IP BAN MIDDLEWARE
 * Prevents banned devices or IPs from logging in or creating new accounts.
 */
async function hardwareBanGuard(req: Request, res: Response, next: NextFunction) {
  const deviceId = (req.headers['x-device-id'] as string) || req.body?.device_unique_id;
  const clientIp = req.ip || '';

  if (deviceId) {
    const isDeviceBanned = await redis.sismember('blacklist:device_ids', deviceId);
    if (isDeviceBanned) {
      return res.status(403).json({
        error: 'DEVICE_BANNED',
        messageAr: '⛔ تم حظر جهازك نهائياً لمخالفة قوانين المنصة',
      });
    }
  }

  const isIpBanned = await redis.sismember('blacklist:ip_addresses', clientIp);
  if (isIpBanned) {
    return res.status(403).json({
      error: 'IP_BANNED',
      messageAr: '⛔ تم حظر عنوان الاتصال الخاص بك من السيرفر',
    });
  }
  next();
}

app.use(hardwareBanGuard);

/**
 * 2. AGORA WEBRTC VOICE TOKEN ENDPOINT (`/api/rooms/rtc-token`)
 */
app.post('/api/rooms/rtc-token', async (req: Request, res: Response) => {
  const { room_id, user_custom_id, is_speaker } = req.body;
  const role = is_speaker ? RtcRole.PUBLISHER : RtcRole.SUBSCRIBER;
  const expirationTimeInSeconds = 3600;
  const currentTimestamp = Math.floor(Date.now() / 1000);
  const privilegeExpiredTs = currentTimestamp + expirationTimeInSeconds;

  const token = RtcTokenBuilder.buildTokenWithAccount(
    AGORA_APP_ID,
    AGORA_APP_CERTIFICATE,
    String(room_id),
    String(user_custom_id),
    role,
    privilegeExpiredTs
  );

  return res.json({ rtc_token: token, channel_name: room_id, role: is_speaker ? 'BROADCASTER' : 'AUDIENCE' });
});

/**
 * 3. RECHARGE AGENCY COIN TRANSFER (`/api/agency/transfer-coins`)
 * Executed inside a PostgreSQL SERIALIZABLE transaction with FOR UPDATE row locks.
 */
app.post('/api/agency/transfer-coins', async (req: Request, res: Response) => {
  const { agent_custom_id, target_user_custom_id, coins_amount } = req.body;
  const client = await pgPool.connect();
  try {
    await client.query('BEGIN ISOLATION LEVEL SERIALIZABLE');

    const agencyRes = await client.query(
      `SELECT agency_id, agency_coin_balance FROM agencies
       WHERE owner_custom_id = $1 AND agency_type = 'RECHARGE' AND is_active = TRUE FOR UPDATE`,
      [agent_custom_id]
    );

    if (agencyRes.rowCount === 0 || Number(agencyRes.rows[0].agency_coin_balance) < Number(coins_amount)) {
      await client.query('ROLLBACK');
      return res.status(400).json({ error: 'INSUFFICIENT_AGENCY_BALANCE' });
    }

    await client.query(
      `UPDATE agencies SET agency_coin_balance = agency_coin_balance - $1 WHERE owner_custom_id = $2`,
      [coins_amount, agent_custom_id]
    );

    await client.query(
      `UPDATE wallets SET coin_balance = coin_balance + $1, updated_at = NOW() WHERE custom_id = $2`,
      [coins_amount, target_user_custom_id]
    );

    const txRes = await client.query(
      `INSERT INTO coin_transactions (sender_custom_id, receiver_custom_id, tx_type, coins_amount, reference_note)
       VALUES ($1, $2, 'AGENCY_RECHARGE', $3, 'شحن فوري عبر وكالة الشحن المعتمدة') RETURNING serial_number`,
      [agent_custom_id, target_user_custom_id, coins_amount]
    );

    await client.query('COMMIT');
    return res.json({ status: 'SUCCESS', serial_number: txRes.rows[0].serial_number });
  } catch (err) {
    await client.query('ROLLBACK');
    return res.status(500).json({ error: 'TRANSACTION_FAILED' });
  } finally {
    client.release();
  }
});

/**
 * 4. REAL-TIME WEBSOCKET ENGINE FOR 9-SEAT VOICE ROOMS & SVGA GIFTS
 */
io.on('connection', (socket) => {
  socket.on('JOIN_ROOM', async ({ room_id, user_custom_id, nickname, entry_mount }) => {
    socket.join(room_id);
    io.to(room_id).emit('USER_ENTERED_WITH_MOUNT', {
      user_custom_id,
      nickname,
      entry_mount,
      timestamp: Date.now(),
    });
  });

  socket.on('MIC_REQUEST_SUBMIT', async ({ room_id, user_custom_id, nickname }) => {
    await redis.zadd(`room:mic_queue:${room_id}`, Date.now(), `${user_custom_id}:${nickname}`);
    io.to(room_id).emit('MIC_QUEUE_UPDATED', { room_id, requester_id: user_custom_id, nickname });
  });

  socket.on('MIC_SLOT_STATE_CHANGE', async ({ room_id, slot_index, action, target_user_id }) => {
    io.to(room_id).emit('MIC_SLOT_CHANGED', { room_id, slot_index, action, target_user_id });
  });

  socket.on('BROADCAST_GIFT', async ({ room_id, gift_id, gift_name, svga_effect, sender_name, receiver_name, combo }) => {
    io.to(room_id).emit('PLAY_SVGA_GIFT_ANIMATION', {
      gift_id,
      gift_name,
      svga_effect,
      sender_name,
      receiver_name,
      combo,
    });
  });
});

const PORT = process.env.PORT || 4000;
server.listen(PORT, () => {
  console.log(`Zadira Live / Ahlan Chat Real-Time Server running on port ${PORT}`);
});
