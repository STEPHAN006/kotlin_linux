package com.stephan.mobil.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.stephan.mobil.security.SecurityUtil
import com.stephan.mobil.ui.theme.*
import com.stephan.mobil.ui.theme.LocalDarkMode
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: BankUiState,
    vm: BankViewModel,
    onToggleTheme: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val darkMode = LocalDarkMode.current
    val brand = LocalBrandColor.current
    val context = LocalContext.current
    var biometricEnabled by remember { mutableStateOf(SecurityUtil.isBiometricEnabled(context)) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { vm.uploadAvatar(it) }
    }

    // Beneficiary form inputs
    var benefName by remember { mutableStateOf("") }
    var benefAccount by remember { mutableStateOf("") }
    var benefPhone by remember { mutableStateOf("") }
    var benefChannel by remember { mutableStateOf("bank") }
    var cardToDeleteId by remember { mutableStateOf<Int?>(null) }
    val pageBg = if (darkMode) BgBase else Color.White
    val cardBg = if (darkMode) BgSurface else Color.White
    val sectionBg = if (darkMode) BgSurfaceElevated else SoftBackground
    val ink = if (darkMode) TextPrimary else Color(0xFF17181C)
    val muted = if (darkMode) TextSecondary else Color(0xFF737780)
    val border = if (darkMode) BgSurfaceTop else LineColor

    cardToDeleteId?.let { cardId ->
        val card = state.cards.firstOrNull { it.id == cardId }
        AlertDialog(
            onDismissRequest = { cardToDeleteId = null },
            title = { Text("Supprimer la carte") },
            text = { Text("Voulez-vous vraiment supprimer la carte •• ${card?.lastFour} ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteCard(cardId)
                    cardToDeleteId = null
                }) {
                    Text("Supprimer", color = brand)
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDeleteId = null }) { Text("Annuler") }
            }
        )
    }

    PullToRefreshBox(
        isRefreshing = state.loading,
        onRefresh = { vm.refreshAll() },
        modifier = Modifier.fillMaxSize()
    ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Avatar / Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(94.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(
                                Brush.linearGradient(colors = listOf(Color(0xFF1C1C1C), Color(0xFF0D0D0D))),
                                shape = CircleShape
                            )
                            .padding(3.dp)
                            .clip(CircleShape)
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarUrl = state.user?.avatarUrl
                        if (avatarUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Photo de profil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(if (darkMode) BgSurface else SoftBackground, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (state.user?.name?.take(2) ?: "SC").uppercase(),
                                    color = brand,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    // Camera badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(brand, CircleShape)
                            .border(2.dp, pageBg, CircleShape)
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Changer photo",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = state.user?.name ?: "Client SCpay",
                    color = ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = state.user?.email ?: "client@scpay.mg",
                    color = muted,
                    fontSize = 13.sp
                )
            }
        }

        // Section Security Switches
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.border(1.dp, border, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Sécurité & Paramètres",
                        color = ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = muted, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Theme sombre", color = ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "Basculer toute l'application en dark mode", color = muted, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { onToggleTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (darkMode) BgSurface else Color.White,
                                checkedTrackColor = brand,
                                uncheckedThumbColor = muted,
                                uncheckedTrackColor = if (darkMode) BgSurfaceHigh else Color(0xFFE7E9EE)
                            )
                        )
                    }

                    Divider(color = border)

                    // Biometrics Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = muted, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Déverrouillage Biométrique", color = ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "Empreinte digitale ou reconnaissance faciale", color = muted, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                SecurityUtil.setBiometricEnabled(context, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (darkMode) BgSurface else Color.White,
                                checkedTrackColor = brand,
                                uncheckedThumbColor = muted,
                                uncheckedTrackColor = if (darkMode) BgSurfaceHigh else Color(0xFFE7E9EE)
                            )
                        )
                    }
                }
            }
        }

        // Section Add Beneficiary
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.border(1.dp, border, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Ajouter un Bénéficiaire",
                        color = ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    PremiumInputField(
                        label = "Nom Complet",
                        value = benefName,
                        onValueChange = { benefName = it },
                        keyboardType = KeyboardType.Text,
                        icon = Icons.Default.Badge
                    )

                    PremiumInputField(
                        label = "Numéro de Compte",
                        value = benefAccount,
                        onValueChange = { benefAccount = it },
                        keyboardType = KeyboardType.Text,
                        icon = Icons.Default.AccountBalanceWallet
                    )

                    PremiumInputField(
                        label = "Téléphone Mobile Money (Optionnel)",
                        value = benefPhone,
                        onValueChange = { benefPhone = it },
                        keyboardType = KeyboardType.Phone,
                        icon = Icons.Default.Phone
                    )

                    // Channel Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("bank" to "Banque", "mvola" to "MVola", "orange_money" to "Orange").forEach { channel ->
                            val selected = benefChannel == channel.first
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (selected) brand.copy(alpha = 0.14f) else sectionBg,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) brand.copy(alpha = 0.5f) else border,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { benefChannel = channel.first }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = channel.second,
                                    color = if (selected) brand else ink,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (benefName.isNotBlank() && benefAccount.isNotBlank()) {
                                vm.addBeneficiary(benefName, benefAccount, benefPhone, benefChannel)
                                benefName = ""
                                benefAccount = ""
                                benefPhone = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = brand)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Enregistrer le bénéficiaire", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (state.beneficiaries.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.border(1.dp, border, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Bénéficiaires", color = ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        state.beneficiaries.forEach { beneficiary ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(beneficiary.name, color = ink, fontWeight = FontWeight.SemiBold)
                                    Text("${beneficiary.bankName} · ${beneficiary.accountNumberMasked}", color = muted, fontSize = 12.sp)
                                }
                                IconButton(onClick = { vm.deleteBeneficiary(beneficiary.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer", tint = brand)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cards management section
        if (state.cards.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.border(1.dp, border, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Mes cartes", color = ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        state.cards.forEach { card ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("Carte •• ${card.lastFour}", color = ink, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${card.type} · ${if (card.isBlocked) "Bloquée" else "Active"}",
                                        color = if (card.isBlocked) brand else Color(0xFF16A34A),
                                        fontSize = 12.sp
                                    )
                                }
                                IconButton(onClick = { cardToDeleteId = card.id }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer", tint = brand)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Simulating QR payload block (Debug purpose)
        if (state.qrPayload != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier.border(1.dp, border, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Payload du Code QR Généré", color = ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = state.qrPayload, color = brand, fontSize = 11.sp, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // Settings button
        item {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.padding(horizontal = 0.dp)
            ) {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = sectionBg,
                        contentColor = ink
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.Default.Settings, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Tous les paramètres", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ArrowForward, null, tint = muted, modifier = Modifier.size(18.dp))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
    } // PullToRefreshBox
}
