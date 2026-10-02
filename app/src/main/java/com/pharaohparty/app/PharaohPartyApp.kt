package com.pharaohparty.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Purple = Color(0xFF6E2BD9)
private val Deep = Color(0xFF0A0317)
private val CardColor = Color(0xFF24104A)
private val Gold = Color(0xFFFFD66B)

@Composable
fun PharaohPartyApp(vm: PartyViewModel) {
    val screen by vm.screen.collectAsState()
    val error by vm.error.collectAsState()
    MaterialTheme(colorScheme = darkColorScheme(primary = Purple, background = Deep, surface = CardColor)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF30105C), Deep)))) {
            when (val current = screen) {
                Screen.Login -> LoginScreen(vm)
                Screen.Home -> HomeScreen(vm)
                is Screen.RoomScreen -> RoomScreen(vm, current.room)
                Screen.Store -> StoreScreen(vm)
                Screen.Profile -> ProfileScreen(vm)
            }
            error?.let { message ->
                AlertDialog(
                    onDismissRequest = vm::clearError,
                    confirmButton = { TextButton(onClick = vm::clearError) { Text("حسناً") } },
                    title = { Text("تنبيه") },
                    text = { Text(message) }
                )
            }
        }
    }
}

@Composable
private fun LoginScreen(vm: PartyViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var create by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("♛", fontSize = 72.sp, color = Gold)
        Text("فرعون بارتي", fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text("غرف صوتية • هدايا • VIP • وكالات", color = Color.LightGray)
        Spacer(Modifier.height(30.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("اسم المستخدم") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("كلمة المرور") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { vm.login(username, password, create) },
            enabled = username.length >= 3 && password.length >= 6,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (create) "إنشاء الحساب" else "دخول")
        }
        TextButton(onClick = { create = !create }) {
            Text(if (create) "لدي حساب بالفعل" else "إنشاء حساب جديد")
        }
        OutlinedButton(onClick = vm::quickLogin, modifier = Modifier.fillMaxWidth()) {
            Text("دخول سريع — حساب واحد لهذا الجهاز")
        }
    }
}

@Composable
private fun HomeScreen(vm: PartyViewModel) {
    val profile by vm.profile.collectAsState()
    val rooms by vm.rooms.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(profile?.display_name ?: "مستخدم", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("ID ${profile?.display_id ?: "—"} • 🪙 ${profile?.coins ?: 0}", color = Gold)
            }
            IconButton(onClick = vm::profile) { Icon(Icons.Default.Person, null) }
        }

        Text("الغرف الساخنة", fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))

        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rooms) { room -> RoomCard(room) { vm.open(room) } }
        }

        NavigationBar(containerColor = Color(0xFF16072D)) {
            NavigationBarItem(true, vm::home, icon = { Icon(Icons.Default.Home, null) }, label = { Text("المنزل") })
            NavigationBarItem(false, vm::store, icon = { Icon(Icons.Default.ShoppingCart, null) }, label = { Text("المتجر") })
            NavigationBarItem(false, vm::profile, icon = { Icon(Icons.Default.Person, null) }, label = { Text("أنا") })
        }
    }
}

@Composable
private fun RoomCard(room: Room, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(22.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(74.dp).background(
                    Brush.radialGradient(listOf(Purple, Deep)),
                    RoundedCornerShape(18.dp)
                ),
                contentAlignment = Alignment.Center
            ) { Text("🎙️", fontSize = 36.sp) }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(room.title ?: room.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("غرفة صوتية • ${room.max_seats} مقاعد", color = Color.LightGray)
            }
            Text("دخول", color = Gold, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RoomScreen(vm: PartyViewModel, room: Room) {
    val profile by vm.profile.collectAsState()
    val seats by vm.seats.collectAsState()
    val messages by vm.messages.collectAsState()
    val gifts by vm.gifts.collectAsState()
    val mic by vm.micEnabled.collectAsState()
    var chat by remember { mutableStateOf(false) }
    var giftsOpen by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = vm::home) { Icon(Icons.Default.ArrowBack, null) }
            Column(Modifier.weight(1f)) {
                Text(room.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("مباشر • §{seats.count { it.user_id != null }} متصل", color = Gold, fontSize = 12.sp)
            }
            Text("🪙 §{profile?.coins ?: 0}", color = Gold)
        }
        Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Surface2)) {
            Column(Modifier.padding(14.dp)) {
                Text(room.title ?: "مجلس فرعون بارتي", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("الصوت مباشر عبر LiveKit • بيانات الغرفة عبر Supabase", color = TextSoft, fontSize = 12.sp)
            }
        }
        LazyVerticalGrid(
            GridCells.Fixed(4),
            Modifier.weight(1f).padding(14.dp),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items((1..room.max_seats).toList()) { number ->
                val seat = seats.firstOrNull { it.seat_no == number }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(enabled = seat?.user_id == null && seat?.locked != true) {
                        vm.claimSeat(room.id, number)
                    }
                ) {
                    Box(Modifier.size(68.dp).background(if (seat?.user_id != null) Primary else Color(0x552D1849), CircleShape), contentAlignment = Alignment.Center) {
                        Text(if (seat?.user_id != null) "🎙️" else if (seat?.locked == true) "🔒" else "+", fontSize = 28.sp)
                    }
                    Text(if (seat?.user_id != null) "متحدث" else "مقعد $number", fontSize = 11.sp, color = TextSoft)
                    if (seat?.is_muted == true) Text("🔇", fontSize = 10.sp)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            FilledTonalIconButton(onClick = { chat = true }) { Icon(Icons.Default.Chat, null) }
            FilledTonalIconButton(onClick = { giftsOpen = true }) { Icon(Icons.Default.CardGiftcard, null) }
            FilledIconButton(onClick = vm::toggleMic) { Icon(if (mic) Icons.Default.Mic else Icons.Default.MicOff, null) }
            FilledTonalIconButton(onClick = vm::home) { Icon(Icons.Default.ExitToApp, null) }
        }
        Text("اضغط مقعداً فارغاً للصعود إلى المايك", color = TextSoft, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp))
    }

    if (chat) {
        ModalBottomSheet(onDismissRequest = { chat = false }) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("دردشة الغرفة", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                LazyColumn(Modifier.heightIn(max = 340.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(messages) { m -> Text("§{m.user_id.take(8)}: §{m.message}", Modifier.background(Surface2, RoundedCornerShape(12.dp)).padding(10.dp)) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(message, { message = it }, Modifier.weight(1f), placeholder = { Text("اكتب رسالتك") })
                    IconButton(onClick = { vm.sendMessage(room.id, message); message = "" }) { Icon(Icons.Default.Send, null) }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }

    if (giftsOpen) {
        ModalBottomSheet(onDismissRequest = { giftsOpen = false }) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("إرسال هدية", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("اختر هدية لإرسالها إلى أحد الموجودين", color = TextSoft)
                val receiver = seats.firstOrNull { it.user_id != null }?.user_id
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    items(gifts) { gift ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), colors = CardDefaults.cardColors(containerColor = Surface2)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(gift.icon, fontSize = 38.sp)
                                Column(Modifier.weight(1f)) {
                                    Text(gift.name, fontWeight = FontWeight.Bold)
                                    Text("§{gift.price_coins} 🪙", color = Gold)
                                }
                                Button(enabled = receiver != null, onClick = {
                                    receiver?.let { vm.sendGift(room.id, it, gift.id) }
                                    giftsOpen = false
                                }) { Text("إرسال") }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}
@Composable
private fun StoreScreen(vm: PartyViewModel) {
    val storeItems by vm.store.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = vm::home) { Icon(Icons.Default.ArrowBack, null) }
            Text("المتجر", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }

        LazyVerticalGrid(
            GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(storeItems) { item ->
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(
                        Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(item.icon ?: "✨", fontSize = 50.sp)
                        Text(item.name, fontWeight = FontWeight.Bold)
                        Text("${item.price_coins} 🪙", color = Gold)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { vm.buy(item.id) }) { Text("شراء") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(vm: PartyViewModel) {
    val profile by vm.profile.collectAsState()

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        IconButton(onClick = vm::home) { Icon(Icons.Default.ArrowBack, null) }
        Spacer(Modifier.height(20.dp))
        Text("الملف الشخصي", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        Card(shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp)) {
                Text(profile?.display_name ?: "مستخدم", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("ID ${profile?.display_id ?: "—"}")
                Spacer(Modifier.height(12.dp))
                Text("🪙 ${profile?.coins ?: 0}   💎 ${profile?.diamonds ?: 0}")
                Text("VIP ${profile?.vip_level ?: 0}")
                Text("الدور: ${profile?.role_code ?: "USER"}")
            }
        }
    }
}
