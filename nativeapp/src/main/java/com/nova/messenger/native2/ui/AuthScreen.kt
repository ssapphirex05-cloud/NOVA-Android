package com.nova.messenger.native2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    Column(
        modifier = Modifier.fillMaxSize().background(NovaPalette.Bg2).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(17.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF5BB8FF), NovaPalette.Accent, Color(0xFF3867E8)))),
                contentAlignment = Alignment.Center
            ) {
                Text("N", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
            }
            Column(Modifier.padding(start = 12.dp)) {
                Text("NOVA", color = NovaPalette.Text, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Text("Messenger", color = NovaPalette.Muted, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(40.dp))
        Text(if (register) "Создать аккаунт" else "Войти в NOVA", color = NovaPalette.Text, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
        Text("Один аккаунт для веба и Android.", color = NovaPalette.Muted, fontSize = 13.5.sp, modifier = Modifier.padding(top = 8.dp, bottom = 22.dp))

        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(NovaPalette.Panel2).border(1.dp, NovaPalette.Line, RoundedCornerShape(16.dp)).padding(4.dp)
        ) {
            AuthTab("Вход", !register, Modifier.weight(1f)) { register = false }
            AuthTab("Регистрация", register, Modifier.weight(1f)) { register = true }
        }

        Spacer(Modifier.height(14.dp))
        NovaInput(username, { username = it }, "Username", false, KeyboardType.Ascii)
        if (register) {
            Spacer(Modifier.height(12.dp))
            NovaInput(displayName, { displayName = it }, "Имя", false, KeyboardType.Text)
        }
        Spacer(Modifier.height(12.dp))
        NovaInput(password, { password = it }, "Пароль", true, KeyboardType.Password)

        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF45A3F4), Color(0xFF2F82CF))))
                .clickable(enabled = !busy) {
                    if (register) onRegister(username, displayName, password) else onLogin(username, password)
                },
            contentAlignment = Alignment.Center
        ) {
            if (busy) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            else Text(if (register) "Создать аккаунт" else "Войти", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun AuthTab(text: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(42.dp).clip(RoundedCornerShape(12.dp))
            .background(if (active) NovaPalette.Panel3 else Color.Transparent).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (active) NovaPalette.Text else NovaPalette.Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NovaInput(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    password: Boolean,
    keyboardType: KeyboardType
) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = TextStyle(color = NovaPalette.Text, fontSize = 16.sp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(15.dp))
            .background(NovaPalette.Panel2).border(1.dp, NovaPalette.Line, RoundedCornerShape(15.dp)).padding(horizontal = 15.dp),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) Text(placeholder, color = NovaPalette.Muted2, fontSize = 14.sp)
                inner()
            }
        }
    )
}
