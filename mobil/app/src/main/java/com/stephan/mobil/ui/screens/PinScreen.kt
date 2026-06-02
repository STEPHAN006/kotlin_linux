package com.stephan.mobil.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.security.SecurityUtil
import com.stephan.mobil.ui.theme.NeonEmerald
import com.stephan.mobil.ui.theme.ObsidianBlack
import kotlinx.coroutines.delay

@Composable
fun PinScreen(
    onUnlock: () -> Unit,
    onBiometric: () -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var cooldownSeconds by remember { mutableIntStateOf(0) }
    val setupMode = remember { !SecurityUtil.hasPinCode(context) }

    LaunchedEffect(cooldownSeconds) {
        if (cooldownSeconds > 0) {
            delay(1000)
            cooldownSeconds -= 1
        }
    }

    // Trigger input verify when PIN reaches length 4
    LaunchedEffect(pin) {
        if (pin.length == 4) {
            delay(150)
            if (setupMode) {
                SecurityUtil.savePinCode(context, pin)
                onUnlock()
            } else if (SecurityUtil.verifyPinCode(context, pin)) {
                onUnlock()
            } else {
                failedAttempts += 1
                pin = ""
                if (failedAttempts >= 3) {
                    cooldownSeconds = 30
                    failedAttempts = 0
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ObsidianBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = NeonEmerald,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (setupMode) "Définir votre code PIN" else "Saisir votre code PIN",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (setupMode) "Créez un code à 4 chiffres pour sécuriser l'accès" else "Entrez votre code secret SCpay",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            // Input Dots Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                if (cooldownSeconds > 0) {
                    Text(
                        text = "Trop de tentatives !",
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Veuillez réessayer dans ${cooldownSeconds}s",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 4) {
                            val isFilled = i < pin.length
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) NeonEmerald else Color.DarkGray.copy(alpha = 0.5f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isFilled) NeonEmerald else Color.Gray.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }

            // Custom Keypad
            CustomNumericKeypad(
                enabled = cooldownSeconds == 0,
                showBiometrics = !setupMode && SecurityUtil.isBiometricEnabled(context),
                onKeyPress = { digit ->
                    if (pin.length < 4) {
                        pin += digit
                    }
                },
                onBackspace = {
                    if (pin.isNotEmpty()) {
                        pin = pin.dropLast(1)
                    }
                },
                onBiometricClick = onBiometric
            )
        }
    }
}

@Composable
fun CustomNumericKeypad(
    enabled: Boolean,
    showBiometrics: Boolean,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onBiometricClick: () -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("biometric", "0", "backspace")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                if (key != "biometric" && key != "backspace") {
                                    Color.White.copy(alpha = 0.05f)
                                } else Color.Transparent
                            )
                            .clickable(enabled = enabled) {
                                when (key) {
                                    "biometric" -> if (showBiometrics) onBiometricClick()
                                    "backspace" -> onBackspace()
                                    else -> onKeyPress(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (key) {
                            "biometric" -> {
                                if (showBiometrics) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Biométrie",
                                        tint = NeonEmerald,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            "backspace" -> {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Supprimer",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            else -> {
                                Text(
                                    text = key,
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
