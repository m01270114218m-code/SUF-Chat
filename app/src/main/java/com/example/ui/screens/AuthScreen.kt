package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Ultra-Polished Royal Voice Login & Registration Screen:
 * - Updated App Logo (`R.drawable.img_royal_voice_logo`) inside a glowing 3D animated gold-and-violet royal ring.
 * - Updated App Brand Name ("ROYAL VOICE | رويال فويس") + luxury subtitle.
 * - Refined high-contrast input fields, glowing 3D buttons, and crystal-clear Arabic typography.
 */
@Composable
fun AuthScreen(
    authErrorMessage: String? = null,
    onLoginSuccess: (email: String, password: String, nickname: String?, avatarType: String) -> Unit
) {
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(true) }
    var localErrorText by remember { mutableStateOf<String?>(null) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var registerNickname by remember { mutableStateOf("") }
    var showOtpDialog by remember { mutableStateOf(false) }
    var showPhoneLoginDialog by remember { mutableStateOf(false) }
    var phoneInput by remember { mutableStateOf("") }

    val goldPrimary = Color(0xFFFFC72C)
    val goldText = Color(0xFFF5B82E)
    val fieldBorder = Color(0xFF3A3222)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0E091C),
                        Color(0xFF090710),
                        Color(0xFF060508)
                    )
                )
            )
    ) {
        // Luxury radial ambient glow circles in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF58287F).copy(alpha = 0.45f),
                        Color(0xFF3D2E08).copy(alpha = 0.32f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.50f, size.height * 0.16f),
                    radius = size.width * 0.72f
                ),
                center = Offset(size.width * 0.50f, size.height * 0.16f),
                radius = size.width * 0.72f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF432C08).copy(alpha = 0.42f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.88f),
                    radius = size.width * 0.60f
                ),
                center = Offset(size.width * 0.85f, size.height * 0.88f),
                radius = size.width * 0.60f
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. New Royal Voice App Emblem with Generated 3D Logo & Animated Crown Ring
            RoyalVoiceAppLogoEmblem()

            Spacer(modifier = Modifier.height(18.dp))

            // 2. New App Name in Login Screen (Bilingual Royal Title + Badge)
            Text(
                text = "R O Y A L   V O I C E",
                color = goldPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "رويال فويس بلس 👑",
                color = Color(0xFFFFF3C4),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Enhanced Subtitle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1F1735).copy(alpha = 0.85f))
                    .border(0.8.dp, goldPrimary.copy(alpha = 0.45f), RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isRegisterMode) "✨ إنشاء حساب ملكي جديد في عالم الفخامة" else "✨ عالم الدردشة الصوتية الملكية والغرف الفاخرة",
                    color = Color(0xFFE5E0D5),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Optional Nickname field when creating a new account
            if (isRegisterMode) {
                SeefoDarkInputField(
                    value = registerNickname,
                    onValueChange = {
                        registerNickname = it
                        localErrorText = null
                    },
                    placeholder = "الاسم المستعار في التطبيق (مثال: محمد فرعون)",
                    isPassword = false,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = goldText,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    modifier = Modifier.testTag("nickname_input")
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 4. Input Field 1: "اسم المستخدم أو البريد الإلكتروني"
            SeefoDarkInputField(
                value = usernameOrEmail,
                onValueChange = {
                    usernameOrEmail = it
                    localErrorText = null
                },
                placeholder = "اسم المستخدم أو البريد الإلكتروني",
                isPassword = false,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "البريد الإلكتروني",
                        tint = goldText,
                        modifier = Modifier.size(22.dp)
                    )
                },
                modifier = Modifier.testTag("email_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Input Field 2: "كلمة المرور"
            SeefoDarkInputField(
                value = password,
                onValueChange = {
                    password = it
                    localErrorText = null
                },
                placeholder = "كلمة المرور",
                isPassword = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "كلمة المرور",
                        tint = goldText,
                        modifier = Modifier.size(22.dp)
                    )
                },
                modifier = Modifier.testTag("password_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 6. "نسيت كلمة المرور؟ استعادة بـ OTP"
            Text(
                text = "نسيت كلمة المرور؟ استعادة بـ OTP",
                color = goldText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clickable { showOtpDialog = true }
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 7. Privacy Policy & Terms of Use Checkbox Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { agreedToTerms = !agreedToTerms }
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFFE0DDD5), fontWeight = FontWeight.Medium)) {
                            append("قرأت ")
                        }
                        withStyle(
                            SpanStyle(
                                color = goldPrimary,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("سياسة الخصوصية")
                        }
                        withStyle(SpanStyle(color = Color(0xFFE0DDD5), fontWeight = FontWeight.Medium)) {
                            append(" و ")
                        }
                        withStyle(
                            SpanStyle(
                                color = goldPrimary,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("اتفاقية الاستخدام")
                        }
                    },
                    fontSize = 13.sp,
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Custom Square Checkbox
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (agreedToTerms) Color(0xFF281F08) else Color.Transparent)
                        .border(
                            width = 1.4.dp,
                            color = if (agreedToTerms) goldPrimary else Color(0xFF757268),
                            shape = RoundedCornerShape(6.dp)
                        )
                ) {
                    if (agreedToTerms) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = goldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            val shownError = localErrorText ?: authErrorMessage
            if (shownError != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = shownError,
                    color = Color(0xFFFF5252),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 8. Bright Golden Primary Button: "تسجيل الدخول"
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFFFB300))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFDF73),
                                Color(0xFFFFB800),
                                Color(0xFFE69500)
                            )
                        )
                    )
                    .border(1.2.dp, Color(0xFFFFF6D6), RoundedCornerShape(18.dp))
                    .clickable {
                        if (!agreedToTerms) {
                            localErrorText = "يرجى الموافقة على سياسة الخصوصية واتفاقية الاستخدام أولاً"
                            return@clickable
                        }
                        val rawInput = usernameOrEmail.trim().ifBlank { "محمد فرعون" }
                        val normalizedEmail = if (rawInput.contains("@")) {
                            rawInput
                        } else {
                            "${rawInput.replace(" ", "_")}@royalvoice.live"
                        }
                        val effectivePass = password.ifBlank { "123456" }
                        val chosenName = when {
                            registerNickname.isNotBlank() -> registerNickname.trim()
                            !rawInput.contains("@") && rawInput.isNotBlank() -> rawInput
                            else -> "محمد فرعون"
                        }
                        onLoginSuccess(
                            normalizedEmail,
                            effectivePass,
                            chosenName,
                            "PRINCE"
                        )
                    }
                    .testTag("login_submit_button")
            ) {
                Text(
                    text = if (isRegisterMode) "إنشاء الحساب وتسجيل الدخول 👑" else "تسجيل الدخول الملكي 👑",
                    color = Color(0xFF140D02),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 9. Phone Login Button: "تسجيل الدخول برقم الهاتف"
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0C1D13), Color(0xFF122B1C))
                        )
                    )
                    .border(1.3.dp, Color(0xFF34D399), RoundedCornerShape(18.dp))
                    .clickable { showPhoneLoginDialog = true }
                    .testTag("phone_login_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "تسجيل الدخول برقم الهاتف",
                        color = Color(0xFF6EE7B7),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Outlined.PhoneAndroid,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 10. Google Login Button: "المتابعة باستخدام جوجل"
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1A162B), Color(0xFF231D38))
                        )
                    )
                    .border(1.3.dp, Color(0xFF7C69A8), RoundedCornerShape(18.dp))
                    .clickable {
                        val rawName = usernameOrEmail.trim().ifBlank { "محمد فرعون" }
                        onLoginSuccess(
                            "mohamed.pharaoh@gmail.com",
                            "google_auth_pass",
                            rawName,
                            "PRINCE"
                        )
                    }
                    .testTag("google_login_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "المتابعة باستخدام جوجل",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "G",
                        color = Color(0xFFFFD54F),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 11. Bottom Link: "ليس لديك حساب؟ إنشاء حساب جديد"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1A1426))
                    .border(1.dp, goldPrimary.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .clickable { isRegisterMode = !isRegisterMode }
                    .padding(horizontal = 22.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (isRegisterMode) "لديك حساب بالفعل؟ تسجيل الدخول" else "ليس لديك حساب؟ إنشاء حساب جديد ✨",
                    color = goldPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Phone Login Dialog
        if (showPhoneLoginDialog) {
            AlertDialog(
                onDismissRequest = { showPhoneLoginDialog = false },
                containerColor = Color(0xFF141122),
                title = {
                    Text(
                        text = "📱 تسجيل الدخول برقم الهاتف",
                        color = goldPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                text = {
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        placeholder = { Text("+201000000000", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF34D399),
                            unfocusedBorderColor = fieldBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPhoneLoginDialog = false
                            val phoneClean = phoneInput.trim().ifBlank { "01000000000" }
                            onLoginSuccess(
                                "$phoneClean@phone.royalvoice.live",
                                "123456",
                                "محمد فرعون",
                                "PRINCE"
                            )
                        }
                    ) {
                        Text("دخول الآن", color = Color(0xFF34D399), fontWeight = FontWeight.ExtraBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPhoneLoginDialog = false }) {
                        Text("إلغاء", color = Color.White)
                    }
                }
            )
        }

        // OTP Recovery Dialog
        if (showOtpDialog) {
            AlertDialog(
                onDismissRequest = { showOtpDialog = false },
                containerColor = Color(0xFF141122),
                title = {
                    Text(
                        text = "🔐 استعادة كلمة المرور بـ OTP",
                        color = goldPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                text = {
                    Text(
                        text = "أدخل بريدك الإلكتروني أو رقم هاتفك في حقل المستخدم ثم اضغط إرسال رمز التحقق الفوري OTP.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showOtpDialog = false }) {
                        Text("إرسال الرمز", color = goldPrimary, fontWeight = FontWeight.ExtraBold)
                    }
                }
            )
        }
    }
}

@Composable
private fun SeefoDarkInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean,
    trailingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF171326), Color(0xFF1D1830))
                )
            )
            .border(1.2.dp, Color(0xFF473B22), RoundedCornerShape(18.dp))
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 14.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color(0xFF9E988E),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    ),
                    cursorBrush = SolidColor(Color(0xFFFFC72C)),
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            trailingIcon()
        }
    }
}

@Composable
private fun RoyalVoiceAppLogoEmblem() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_ring_anim")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "logo_ring_rotate"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_glow_pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(144.dp)
    ) {
        // Outer animated royal sweep-gradient ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = (size.minDimension / 2f) * 0.94f
            drawCircle(
                color = Color(0xFFFFC72C).copy(alpha = 0.20f),
                radius = r * glowPulse,
                style = Stroke(width = 6.dp.toPx())
            )
            rotate(ringRotation) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFFFFD700),
                            Color(0xFFA855F7),
                            Color(0xFF38BDF8),
                            Color(0xFFFF8F00),
                            Color(0xFFFFD700)
                        )
                    ),
                    radius = r,
                    style = Stroke(width = 3.5.dp.toPx())
                )
            }
        }

        // Main circular app logo image (`img_royal_voice_logo`)
        Image(
            painter = painterResource(id = R.drawable.img_royal_voice_logo),
            contentDescription = "شعار تطبيق رويال فويس",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(122.dp)
                .clip(CircleShape)
                .border(2.5.dp, Color(0xFFFFD700), CircleShape)
        )
    }
}

