package com.stephan.mobil.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.stephan.mobil.data.api.ApiClient
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import com.stephan.mobil.R
import com.stephan.mobil.ui.theme.DarkSlate
import com.stephan.mobil.ui.theme.LightSlate
import com.stephan.mobil.ui.theme.BrandPrimary
import com.stephan.mobil.ui.theme.ObsidianBlack
import com.stephan.mobil.ui.theme.PremiumWhite

@Composable
fun WelcomeScreen(
    onLoginSubmit: (String, String) -> Unit,
    onRegisterSubmit: (String, String, String, String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    var showLoginOverlay by remember { mutableStateOf(false) }
    var showRegisterOverlay by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    val view = LocalView.current

    DisposableEffect(view) {
        val window = (view.context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        onDispose { }
    }

    // ExoPlayer ping-pong — forward then reverse, no cuts
    val context = LocalContext.current
    val stepMs = 33L // ~30 fps reverse
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse("android.resource://${context.packageName}/${R.raw.bg_video}")
            setMediaItem(MediaItem.fromUri(uri))
            volume = 0f
            repeatMode = Player.REPEAT_MODE_OFF
            prepare()
        }
    }
    var isReversing by remember { mutableStateOf(false) }

    // ExoPlayer lifecycle — release on leave
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    isReversing = true
                }
            }
        }
        exoPlayer.addListener(listener)
        exoPlayer.play()
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Reverse loop: tick every stepMs, seek back, restart forward at position 0
    LaunchedEffect(isReversing) {
        if (isReversing) {
            while (true) {
                kotlinx.coroutines.delay(stepMs)
                val next = exoPlayer.currentPosition - stepMs
                if (next > 0) {
                    exoPlayer.seekTo(next)
                } else {
                    exoPlayer.seekTo(0)
                    exoPlayer.play()
                    isReversing = false
                    break
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // PlayerView — keeps last frame visible during seeks (no black flash)
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Semi-transparent gradient overlay to blend colors and ensure text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.08f),
                            Color.Black.copy(alpha = 0.18f),
                            Color.Black.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BrandPrimary.copy(alpha = 0.12f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.42f)
                        ),
                        radius = 920f
                    )
                )
        )

        // Foreground Onboarding content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // App Title "SCpay"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 22.dp)
            ) {
                Text(
                    text = "SCpay",
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "BANQUE PRIVEE MOBILE",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Votre argent, plus fluide.",
                    color = PremiumWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 32.sp
                )
                Text(
                    text = "Paiements, cartes et virements dans une experience rapide et elegante.",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
            }

            // Bottom Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 26.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Créer un compte Button
                Button(
                    onClick = { showRegisterOverlay = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PremiumWhite,
                        contentColor = ObsidianBlack
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = "Créer un compte",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Me connecter Button
                OutlinedButton(
                    onClick = { showLoginOverlay = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.42f))
                ) {
                    Text(
                        text = "Me connecter",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Stephan & Cassie",
                    color = Color.White.copy(alpha = 0.38f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp
                )
            }
        }

        // Bouton serveur — coin haut droit
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 12.dp, top = 4.dp)
        ) {
            IconButton(onClick = { showServerDialog = true }) {
                Icon(
                    Icons.Default.WifiFind,
                    contentDescription = "Serveur",
                    tint = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Dialog changement URL serveur
        if (showServerDialog) {
            var urlField by remember { mutableStateOf(ApiClient.getBaseUrl(context)) }
            Dialog(
                onDismissRequest = { showServerDialog = false },
                properties = DialogProperties(dismissOnClickOutside = true)
            ) {
                Column(
                    modifier = Modifier
                        .background(Color(0xFF1A1C23), androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wifi, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Serveur Laravel", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(
                        value = urlField,
                        onValueChange = { urlField = it },
                        label = { Text("URL du serveur", color = Color(0xFF8B8F98), fontSize = 12.sp) },
                        placeholder = { Text("http://192.168.x.x:8000/api/", color = Color(0xFF8B8F98), fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0xFF3A3D45)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )
                    Button(
                        onClick = {
                            val normalized = urlField.trim().let {
                                if (!it.startsWith("http")) "http://$it" else it
                            }.trimEnd('/') + "/"
                            ApiClient.saveCustomUrl(context, normalized)
                            showServerDialog = false
                            (context as? Activity)?.recreate()
                        },
                        enabled = urlField.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Appliquer et redémarrer", color = Color(0xFF17181C), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    TextButton(
                        onClick = { showServerDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Annuler", color = Color(0xFF8B8F98), fontSize = 13.sp)
                    }
                }
            }
        }

        // Gorgeous slide-up bottom modal sheets simulated via Compose animations
        AnimatedVisibility(
            visible = showLoginOverlay,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            LoginOverlaySheet(
                onClose = { showLoginOverlay = false },
                onSubmit = onLoginSubmit,
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }

        AnimatedVisibility(
            visible = showRegisterOverlay,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            RegisterOverlaySheet(
                onClose = { showRegisterOverlay = false },
                onSubmit = onRegisterSubmit,
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }
    }
}

@Composable
fun LoginOverlaySheet(
    onClose: () -> Unit,
    onSubmit: (String, String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlate),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {} // Disable click through
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle bar
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Connexion",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                    }
                }

                if (errorMessage != null) {
                    Text(text = errorMessage, color = Color.Red, fontSize = 13.sp)
                }

                // Email field
                PremiumInputField(
                    label = "Adresse email",
                    value = email,
                    onValueChange = { email = it },
                    keyboardType = KeyboardType.Email,
                    icon = Icons.Default.Email
                )

                // Password field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Mot de passe",
                        color = LightSlate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LightSlate, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = LightSlate
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(14.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandPrimary.copy(alpha = 0.8f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                            cursorColor = BrandPrimary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onSubmit(email, password) },
                    enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = ObsidianBlack, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Se connecter",
                            color = ObsidianBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterOverlaySheet(
    onClose: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlate),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Inscription",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                    }
                }

                if (errorMessage != null) {
                    Text(text = errorMessage, color = Color.Red, fontSize = 13.sp)
                }

                PremiumInputField(
                    label = "Nom Complet",
                    value = name,
                    onValueChange = { name = it },
                    keyboardType = KeyboardType.Text,
                    icon = Icons.Default.Badge
                )

                PremiumInputField(
                    label = "Adresse email",
                    value = email,
                    onValueChange = { email = it },
                    keyboardType = KeyboardType.Email,
                    icon = Icons.Default.Email
                )

                PremiumInputField(
                    label = "Téléphone mobile",
                    value = phone,
                    onValueChange = { phone = it },
                    keyboardType = KeyboardType.Phone,
                    icon = Icons.Default.Phone
                )

                // Password field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Mot de passe",
                        color = LightSlate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = LightSlate, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = LightSlate
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(14.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandPrimary.copy(alpha = 0.8f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                            cursorColor = BrandPrimary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onSubmit(name, email, phone, password) },
                    enabled = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && password.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = ObsidianBlack, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Créer mon compte",
                            color = ObsidianBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumCardVisual(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00E676).copy(alpha = 0.2f),
                        Color(0xFF333537).copy(alpha = 0.3f),
                        Color(0xFFFF5252).copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.05f),
                        Color(0xFF00E676).copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCpay",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                // Card Chip Simulation
                Box(
                    modifier = Modifier
                        .size(36.dp, 26.dp)
                        .background(Color(0xFFD4AF37).copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                )
            }

            Column {
                Text(
                    text = "**** **** **** 8888",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.8.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "MEMBRE PRIVILÈGE",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "12/29",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
