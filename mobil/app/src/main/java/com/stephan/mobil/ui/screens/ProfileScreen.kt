package com.stephan.mobil.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.security.SecurityUtil
import com.stephan.mobil.ui.theme.*
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel

@Composable
fun ProfileScreen(
    state: BankUiState,
    vm: BankViewModel,
    darkMode: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    var biometricEnabled by remember { mutableStateOf(SecurityUtil.isBiometricEnabled(context)) }

    // Beneficiary form inputs
    var benefName by remember { mutableStateOf("") }
    var benefAccount by remember { mutableStateOf("") }
    var benefPhone by remember { mutableStateOf("") }
    var benefChannel by remember { mutableStateOf("bank") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
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
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            Brush.linearGradient(colors = listOf(BrandPrimary, Color(0xFF7C4DFF))),
                            shape = CircleShape
                        )
                        .padding(3.dp)
                        .background(BgSurface, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = state.user?.name ?: "Client SCpay",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = state.user?.email ?: "client@scpay.mg",
                    color = LightSlate,
                    fontSize = 13.sp
                )
            }
        }

        // Section Security Switches
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BgSurfaceElevated),
                modifier = Modifier.border(1.dp, BgSurfaceTop, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Sécurité & Paramètres",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Mock Mode Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Dns, contentDescription = null, tint = LightSlate, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Mode Démo / Mock", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "Utiliser des fausses données hors réseau", color = LightSlate, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = state.mockMode,
                            onCheckedChange = { vm.setMockMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandPrimary,
                                uncheckedThumbColor = LightSlate,
                                uncheckedTrackColor = BgSurfaceTop
                            )
                        )
                    }

                    Divider(color = BgSurfaceTop)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Thème sombre", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "Mode nuit activé en permanence", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = true,
                            onCheckedChange = { },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandPrimary
                            )
                        )
                    }

                    Divider(color = BgSurfaceTop)

                    // Biometrics Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = LightSlate, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Déverrouillage Biométrique", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "Empreinte digitale ou reconnaissance faciale", color = LightSlate, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                SecurityUtil.setBiometricEnabled(context, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandPrimary,
                                uncheckedThumbColor = LightSlate,
                                uncheckedTrackColor = BgSurfaceTop
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
                colors = CardDefaults.cardColors(containerColor = BgSurfaceElevated),
                modifier = Modifier.border(1.dp, BgSurfaceTop, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Ajouter un Bénéficiaire",
                        color = TextPrimary,
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
                                        if (selected) BrandPrimary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) BrandPrimary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.06f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { benefChannel = channel.first }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = channel.second,
                                    color = if (selected) BrandPrimary else PremiumWhite,
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
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = BgSurface)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Enregistrer le bénéficiaire", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (state.beneficiaries.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSurfaceElevated),
                    modifier = Modifier.border(1.dp, LineColor, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Bénéficiaires", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        state.beneficiaries.forEach { beneficiary ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(beneficiary.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text("${beneficiary.bankName} · ${beneficiary.accountNumberMasked}", color = LightSlate, fontSize = 12.sp)
                                }
                                IconButton(onClick = { vm.deleteBeneficiary(beneficiary.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer", tint = BrandPrimary)
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
                    colors = CardDefaults.cardColors(containerColor = BgSurfaceElevated),
                    modifier = Modifier.border(1.dp, BgSurfaceTop, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Payload du Code QR Généré", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = state.qrPayload, color = BrandPrimary, fontSize = 11.sp, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        item {
            var showConfirm by remember { mutableStateOf(false) }

            if (showConfirm) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showConfirm = false },
                    title = { Text("Se déconnecter ?", fontWeight = FontWeight.Bold) },
                    text = { Text("Vous devrez vous reconnecter avec votre email et mot de passe.") },
                    confirmButton = {
                        Button(
                            onClick = { vm.logout(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                        ) { Text("Déconnecter", color = Color.White) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirm = false }) { Text("Annuler") }
                    }
                )
            }

            Button(
                onClick = { showConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary.copy(alpha = 0.08f),
                    contentColor = BrandPrimary
                )
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Se déconnecter", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
