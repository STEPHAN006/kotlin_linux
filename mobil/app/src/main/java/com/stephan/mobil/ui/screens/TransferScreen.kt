package com.stephan.mobil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.ui.theme.*
import com.stephan.mobil.ui.theme.LocalDarkMode
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransferScreen(state: BankUiState, vm: BankViewModel) {
    val darkMode = LocalDarkMode.current
    val brand = LocalBrandColor.current
    val accounts = state.balance.accounts
    var senderId by remember(accounts) { mutableIntStateOf(accounts.firstOrNull()?.id ?: 1) }
    var receiverId by remember(accounts) { mutableIntStateOf(accounts.drop(1).firstOrNull()?.id ?: accounts.firstOrNull()?.id ?: 2) }
    var amountText by remember { mutableStateOf("600000") }
    var noteText by remember { mutableStateOf("Virement mensuel") }
    var otpCode by remember { mutableStateOf("") }
    val pageBg = if (darkMode) BgBase else Color.White
    val ink = if (darkMode) TextPrimary else Color(0xFF17181C)
    val muted = if (darkMode) TextSecondary else Color(0xFF737780)
    val cardBg = if (darkMode) BgSurfaceElevated else Color.White
    val border = if (darkMode) BgSurfaceTop else LineColor

    PullToRefreshBox(
        isRefreshing = state.loading,
        onRefresh = { vm.refreshAll() },
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Virement",
                    color = ink,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            // OTP Notice Info Box
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (darkMode) BrandPrimarySoft else Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
                        .border(1.dp, brand.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(40.dp)
                                .background(brand, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = brand,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Double validation OTP requise pour tout montant ≥ 500 000 MGA ou dépassant 30% de votre solde.",
                            color = muted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Account Selection Fields
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PremiumInputField(
                        label = "Compte Source (ID)",
                        value = senderId.toString(),
                        onValueChange = { senderId = it.toIntOrNull() ?: senderId },
                        keyboardType = KeyboardType.Number,
                        icon = Icons.Default.AccountBalanceWallet
                    )

                    PremiumInputField(
                        label = "Compte Destinataire (ID ou N°)",
                        value = receiverId.toString(),
                        onValueChange = { receiverId = it.toIntOrNull() ?: receiverId },
                        keyboardType = KeyboardType.Number,
                        icon = Icons.Default.ArrowForward
                    )

                    PremiumInputField(
                        label = "Montant (MGA)",
                        value = amountText,
                        onValueChange = { amountText = it },
                        keyboardType = KeyboardType.Decimal,
                        icon = Icons.Default.Payments
                    )

                    PremiumInputField(
                        label = "Motif du virement (Note)",
                        value = noteText,
                        onValueChange = { noteText = it },
                        keyboardType = KeyboardType.Text,
                        icon = Icons.Default.EditNote
                    )
                }
            }

            // Send Button
            item {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        vm.createTransfer(senderId, receiverId, amount, noteText)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brand)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Exécuter le virement",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Beneficiaries horizontal list
            item {
                Text(
                    text = "Bénéficiaires enregistrés",
                    color = ink,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            if (state.beneficiaries.isEmpty()) {
                item {
                    Text(
                        text = "Aucun bénéficiaire enregistré. Vous pouvez en ajouter depuis l'onglet Profil.",
                        color = muted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }
            } else {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.beneficiaries) { benef ->
                            BeneficiaryItemCard(
                                name = benef.name ?: "Inconnu",
                                channel = benef.channel ?: "bank",
                                accountMasked = benef.accountNumberMasked ?: "",
                                onClick = {
                                    benef.id?.let { receiverId = it }
                                }
                            )
                        }
                    }
                }
            }

            // Spacer bottom
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // OTP Validation Dialog Overlay
        if (state.pendingTransfer != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable(enabled = false) {}, // Lock interaction
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(1.dp, border, RoundedCornerShape(24.dp))
                        .padding(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(brand.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = brand,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "Validation OTP",
                            color = ink,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Pour sécuriser votre transfert de ${state.pendingTransfer.amount.toLong()} MGA, veuillez entrer le code à 6 chiffres envoyé.",
                            color = muted,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Réf: ${state.pendingTransfer.reference}",
                            color = brand,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // OTP Numeric Input Box
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) otpCode = it },
                            placeholder = { Text("Code à 6 chiffres", color = muted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (darkMode) BgSurfaceHigh else LightBackground, RoundedCornerShape(12.dp)),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ink,
                                unfocusedTextColor = ink,
                                focusedBorderColor = brand,
                                unfocusedBorderColor = border,
                                cursorColor = brand,
                                focusedContainerColor = if (darkMode) BgSurfaceHigh else LightBackground,
                                unfocusedContainerColor = if (darkMode) BgSurfaceHigh else LightBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (otpCode.length == 6) {
                                    vm.verifyOtp(otpCode)
                                    otpCode = ""
                                }
                            },
                            enabled = otpCode.length == 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brand)
                        ) {
                            Text(
                                text = "Confirmer le code",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun BeneficiaryItemCard(
    name: String,
    channel: String,
    accountMasked: String,
    onClick: () -> Unit
) {
    val darkMode = LocalDarkMode.current
    val brand = LocalBrandColor.current
    val ink = if (darkMode) TextPrimary else Color(0xFF17181C)
    val muted = if (darkMode) TextSecondary else Color(0xFF737780)
    val cardBg = if (darkMode) BgSurfaceElevated else SoftBackground
    val border = if (darkMode) BgSurfaceTop else LineColor
    val iconBg = if (darkMode) Color.White.copy(alpha = 0.05f) else Color.White

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .width(130.dp)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (channel.lowercase()) {
                        "mvola", "orange_money", "airtel_money" -> Icons.Default.PhoneAndroid
                        else -> Icons.Default.AccountBalance
                    },
                    contentDescription = null,
                    tint = brand,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = name,
                color = ink,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = accountMasked,
                color = muted,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
