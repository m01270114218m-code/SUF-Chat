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
    onLoginSuccess: (email: String, password: String, nickname: String?, avatarType: String, createAccount: Boolean) -> Unit

) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var agreed by remember { mutableStateOf(true) }
    var localError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0E091C), Color(0xFF090710), Color(0xFF060508)))
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFF58287F).copy(alpha = .45f), Color.Transparent)),
                center = Offset(size.width * .5f, size.height * .16f),
                radius = size.width * .72f
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            RoyalVoiceAppLogoEmblem()
            Spacer(Modifier.height(18.dp))
            Text("فــرعــون بــارتــي 👑", color = Color(0xFFFFC72C), fontSize = 28.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(6.dp))
            Text(
                if (isRegisterMode) "إنشاء حساب جديد — الاسم وكلمة السر فقط" else "تسجيل الدخول واستعادة حسابك من قاعدة البيانات",
                color = Color(0xFFFFF3C4), fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(26.dp))

            if (isRegisterMode) {
                SeefoDarkInputField(
                    nickname, { nickname = it; localError = null }, "الاسم الظاهر داخل التطبيق",
                    false, { Icon(Icons.Outlined.Person, null, tint = Color(0xFFF5B82E), modifier = Modifier.size(22.dp)) },
                    Modifier.testTag("nickname_input")
                )
                Spacer(Modifier.height(14.dp))
            }

            SeefoDarkInputField(
                username, { username = it; localError = null }, "اسم الحساب (مثال: pharaoh123)",
                false, { Icon(Icons.Outlined.Person, null, tint = Color(0xFFF5B82E), modifier = Modifier.size(22.dp)) },
                Modifier.testTag("username_input")
            )
            Spacer(Modifier.height(14.dp))
            SeefoDarkInputField(
                password, { password = it; localError = null }, "كلمة المرور",
                true, { Icon(Icons.Outlined.Lock, null, tint = Color(0xFFF5B82E), modifier = Modifier.size(22.dp)) },
                Modifier.testTag("password_input")
            )

            Spacer(Modifier.height(18.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { agreed = !agreed }.padding(4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(22.dp).clip(RoundedCornerShape(6.dp))
                        .background(if (agreed) Color(0xFF281F08) else Color.Transparent)
                        .border(1.4.dp, if (agreed) Color(0xFFFFC72C) else Color.Gray, RoundedCornerShape(6.dp))
                ) { if (agreed) Icon(Icons.Outlined.Check, null, tint = Color(0xFFFFC72C), modifier = Modifier.size(16.dp)) }
                Spacer(Modifier.width(10.dp))
                Text("أوافق على شروط استخدام فرعون بارتي", color = Color(0xFFE0DDD5), fontSize = 13.sp)
            }

            val shownError = localError ?: authErrorMessage
            if (shownError != null) {
                Spacer(Modifier.height(10.dp))
                Text(shownError, color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(20.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().height(58.dp).shadow(12.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFFDF73), Color(0xFFFFB800), Color(0xFFE69500))))
                    .border(1.2.dp, Color(0xFFFFF6D6), RoundedCornerShape(18.dp))
                    .clickable {
                        if (!agreed) {
                            localError = "يرجى الموافقة على الشروط أولاً"
                        } else if (username.trim().length < 3) {
                            localError = "اسم الحساب يجب أن يكون 3 أحرف أو أكثر"
                        } else if (password.length < 6) {
                            localError = "كلمة المرور يجب أن تكون 6 أحرف أو أكثر"
                        } else {
                            onLoginSuccess(username.trim(), password, nickname.trim().ifBlank { username.trim() }, "PRINCE", isRegisterMode)
                        }
                    }
                    .testTag("login_submit_button")
            ) {
                Text(if (isRegisterMode) "إنشاء الحساب وتسجيل الدخول 👑" else "تسجيل الدخول 👑", color = Color(0xFF140D02), fontSize = 18.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(14.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().height(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF241A39))
                    .border(1.2.dp, Color(0xFFFFC72C).copy(alpha = .65f), RoundedCornerShape(18.dp))
                    .clickable { onLoginSuccess("__QUICK_LOGIN__", "", "مستخدم فرعون بارتي", "PRINCE", true) }
                    .testTag("quick_login_button")
            ) {
                Text("⚡ تسجيل دخول سريع", color = Color(0xFFFFD75A), fontSize = 17.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(18.dp))
            Text(
                if (isRegisterMode) "لديك حساب؟ تسجيل الدخول" else "ليس لديك حساب؟ إنشاء حساب جديد",
                color = Color(0xFFFFC72C), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clip(RoundedCornerShape(50)).background(Color(0xFF1A1426))
                    .border(1.dp, Color(0xFFFFC72C).copy(alpha = .4f), RoundedCornerShape(50))
                    .clickable { isRegisterMode = !isRegisterMode }.padding(horizontal = 22.dp, vertical = 12.dp)
            )

            Spacer(Modifier.height(18.dp))
            Text(
                "نفس اسم الحساب + كلمة السر = نفس الحساب والرصيد والمقتنيات والبيانات المحفوظة",
                color = Color.White.copy(alpha = .65f), fontSize = 12.sp, textAlign = TextAlign.Center
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
                        keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Ascii
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

