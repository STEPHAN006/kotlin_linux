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
            .background(if (darkMode) Color(0xFF101114) else Color.White)
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
                            Brush.linearGradient(colors = listOf(Color(0xFFD92C55), Color(0xFF7C4DFF))),
                            shape = CircleShape
                        )
                        .padding(3.dp)
                        .background(Color(0xFF17181C), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFFD92C55),
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = state.user?.name ?: "Client SCpay",
                    color = if (darkMode) Color.White else Color(0xFF17181C),
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Sécurité & Paramètres",
                        color = Color(0xFF17181C),
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
                                Text(text = "Mode Démo / Mock", color = Color(0xFF17181C), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "Utiliser des fausses données hors réseau", color = LightSlate, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = state.mockMode,
                            onCheckedChange = { vm.setMockMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF17181C),
                                checkedTrackColor = Color(0xFFD92C55),
                                uncheckedThumbColor = LightSlate,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    Divider(color = Color(0xFF17181C).copy(alpha = 0.05f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = LightSlate, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Theme sombre", color = Color(0xFF17181C), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "Basculer toute l'application en dark mode", color = LightSlate, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { onToggleTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF17181C),
                                checkedTrackColor = Color(0xFFD92C55)
                            )
                        )
                    }

                    Divider(color = Color(0xFF17181C).copy(alpha = 0.05f))

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
                                Text(text = "Déverrouillage Biométrique", color = Color(0xFF17181C), fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                                checkedThumbColor = Color(0xFF17181C),
                                checkedTrackColor = Color(0xFFD92C55),
                                uncheckedThumbColor = LightSlate,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Ajouter un Bénéficiaire",
                        color = Color(0xFF17181C),
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
                                        if (selected) Color(0xFFD92C55).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) Color(0xFFD92C55).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.06f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { benefChannel = channel.first }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = channel.second,
                                    color = if (selected) Color(0xFFD92C55) else PremiumWhite,
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD92C55))
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color(0xFF17181C))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Enregistrer le bénéficiaire", color = Color(0xFF17181C), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (state.beneficiaries.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.border(1.dp, Color(0xFFE6E8EC), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Bénéficiaires", color = Color(0xFF17181C), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        state.beneficiaries.forEach { beneficiary ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(beneficiary.name, color = Color(0xFF17181C), fontWeight = FontWeight.SemiBold)
                                    Text("${beneficiary.bankName} · ${beneficiary.accountNumberMasked}", color = LightSlate, fontSize = 12.sp)
                                }
                                IconButton(onClick = { vm.deleteBeneficiary(beneficiary.id) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer", tint = Color(0xFFD92C55))
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Payload du Code QR Généré", color = Color(0xFF17181C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = state.qrPayload, color = Color(0xFFD92C55), fontSize = 11.sp, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
