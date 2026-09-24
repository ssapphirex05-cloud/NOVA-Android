package com.nova.messenger.native2.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.messenger.native2.R

@Composable
fun AuthScreen(
    busy: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit
) {
    var register by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF06131E),
                        Color(0xFF071827),
                        Color(0xFF06111B)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.CenterEnd)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x223390EC), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 27.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.nova_logo),
                    contentDescription = "NOVA",
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Column(Modifier.padding(start = 15.dp)) {
                    Text(
                        "N O V A",
                        color = NovaPalette.Text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.2.sp
                    )
                    Text(
                        "private messenger",
                        color = Color(0xFF7E90A6),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(34.dp))
            Text(
                if (register) "Регистрация в NOVA" else "Вход в NOVA",
                color = NovaPalette.Text,
                fontSize = 31.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                if (register) "Создай аккаунт и продолжи общение."
                else "Продолжи переписку или создай новый аккаунт.",
                color = Color(0xFF8798AD),
                fontSize = 13.5.sp,
                modifier = Modifier.padding(top = 9.dp, bottom = 21.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0B1C29))
                    .border(1.dp, Color(0xFF17384E), RoundedCornerShape(18.dp))
                    .padding(4.dp)
            ) {
                AuthTab("Вход", !register, Modifier.weight(1f)) { register = false }
                AuthTab("Регистрация", register, Modifier.weight(1f)) { register = true }
            }

            Spacer(Modifier.height(19.dp))
            AuthLabel("Username")
            Spacer(Modifier.height(7.dp))
            PremiumInput(
                value = username,
                onChange = { username = it },
                placeholder = "Username",
                icon = Icons.Rounded.PersonOutline,
                password = false,
                keyboardType = KeyboardType.Ascii
            )

            if (register) {
                Spacer(Modifier.height(13.dp))
                AuthLabel("Имя")
                Spacer(Modifier.height(7.dp))
                PremiumInput(
                    value = displayName,
                    onChange = { displayName = it },
                    placeholder = "Как тебя называть",
                    icon = Icons.Rounded.PersonOutline,
                    password = false,
                    keyboardType = KeyboardType.Text
                )
            }

            Spacer(Modifier.height(13.dp))
            AuthLabel("Пароль")
            Spacer(Modifier.height(7.dp))
            PremiumInput(
                value = password,
                onChange = { password = it },
                placeholder = "Пароль",
                icon = Icons.Rounded.Lock,
                password = true,
                keyboardType = KeyboardType.Password
            )

            Spacer(Modifier.height(19.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF0BC6FA),
                                Color(0xFF109CF2),
                                Color(0xFF1774FF)
                            )
                        )
                    )
                    .clickable(enabled = !busy) {
                        if (register) onRegister(username, displayName, password)
                        else onLogin(username, password)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (register) "Создать аккаунт" else "Войти",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.width(18.dp))
                        Text("→", color = Color.White, fontSize = 19.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthLabel(text: String) {
    Text(
        text,
        color = Color(0xFF9AABBE),
        fontSize = 11.5.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(start = 2.dp)
    )
}

@Composable
private fun AuthTab(
    text: String,
    active: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (active) {
                    Modifier.background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF127CD2), Color(0xFF0B5EA8))
                        )
                    )
                } else {
                    Modifier.background(Color.Transparent)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (active) Color.White else Color(0xFF8698AD),
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun PremiumInput(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    password: Boolean,
    keyboardType: KeyboardType
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(Color(0xFF0B1F2D))
            .border(1.dp, Color(0xFF1B4C69), RoundedCornerShape(17.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color(0xFF7693AD), modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(13.dp))
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(
                color = NovaPalette.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = Color(0xFF6F8398), fontSize = 14.sp)
                    }
                    inner()
                }
            }
        )
        if (password) {
            Icon(
                Icons.Rounded.VisibilityOff,
                contentDescription = null,
                tint = Color(0xFF7693AD),
                modifier = Modifier.size(21.dp)
            )
        }
    }
}
