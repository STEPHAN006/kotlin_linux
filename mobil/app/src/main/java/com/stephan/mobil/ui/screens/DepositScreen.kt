package com.stephan.mobil.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.ui.theme.*
import com.stephan.mobil.ui.theme.LocalDarkMode
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel

private data class DepositMethod(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val requiresPhone: Boolean
)

private val depositMethods = listOf(
    DepositMethod("mvola",        "MVola",        Icons.Default.PhoneAndroid, Color(0xFFD32F2F), true),
    DepositMethod("orange_money", "Orange Money", Icons.Default.PhoneAndroid, Color(0xFFFF6F00), true),
    DepositMethod("airtel_money", "Airtel Money", Icons.Default.PhoneAndroid, Color(0xFFE53935), true),
    DepositMethod("cash",         "Espèces",      Icons.Default.AccountBalance, Color(0xFF388E3C), false),
)

private const val SCPAY_MVOLA_NUMBER = "0385339501"

private fun buildUssdCode(amount: Double, reference: String): String {
    // #111*1*2*MARCHAND*MONTANT*2*REFERENCE#  — MVola paiement marchand Telma
    val amountInt = amount.toLong()
    return "#111*1*2*$SCPAY_MVOLA_NUMBER*$amountInt*2*$reference#"
}

private fun launchUssd(context: android.content.Context, ussdCode: String) {
    val encoded = ussdCode.replace("#", "%23")
    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$encoded"))
    context.startActivity(intent)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DepositScreen(
    state: BankUiState,
    vm: BankViewModel,
    onBack: () -> Unit
) {
    val darkMode = LocalDarkMode.current
    val pageBg = if (darkMode) BgBase else Color.White
    val ink    = if (darkMode) TextPrimary else Color(0xFF17181C)

    // Écran succès
    val successDeposit = state.depositSuccess
    if (successDeposit != null) {
        DepositSuccessScreen(
            result = successDeposit,
            onDone = {
                vm.clearDepositResult()
                onBack()
            }
        )
        return
    }

    // Écran en attente (pending)
    val pendingDeposit = state.depositPending
    if (pendingDeposit != null) {
        DepositPendingScreen(
            result = pendingDeposit,
            isConfirming = state.isConfirmingDeposit,
            onConfirm = { vm.confirmDeposit() },
            onCancel = {
                vm.cancelDeposit()
                onBack()
            }
        )
        return
    }

    // Formulaire
    DepositFormScreen(state = state, vm = vm, ink = ink, pageBg = pageBg, onBack = onBack)
}

@Composable
private fun DepositFormScreen(
    state: BankUiState,
    vm: BankViewModel,
    ink: Color,
    pageBg: Color,
    onBack: () -> Unit
) {
    val darkMode = LocalDarkMode.current
    val brand    = LocalBrandColor.current
    val brandSoftBg = if (darkMode) Color(0x1AFFFFFF) else Color(0xFFF0F0F2)
    val cardBg = if (darkMode) BgSurfaceElevated else Color(0xFFF8F9FC)
    val muted  = if (darkMode) TextSecondary else Color(0xFF8B8F98)
    val border = if (darkMode) BgSurfaceTop else Color(0xFFECEEF2)

    val accounts = state.balance.accounts
    var selectedAccountId by remember(accounts) { mutableIntStateOf(accounts.firstOrNull()?.id ?: 0) }
    var selectedMethod    by remember { mutableStateOf(depositMethods.first()) }
    var amountText        by remember { mutableStateOf("") }
    var phoneText         by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ink)
            }
            Text("Dépôt", color = ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Compte
            item {
                SectionLabel("Compte à créditer", ink)
                Spacer(Modifier.height(8.dp))
                if (accounts.isEmpty()) {
                    Text("Aucun compte disponible", color = muted, fontSize = 14.sp)
                } else {
                    accounts.forEach { account ->
                        val sel = account.id == selectedAccountId
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (sel) brandSoftBg else cardBg)
                                .border(if (sel) 1.5.dp else 1.dp, if (sel) brand else border, RoundedCornerShape(12.dp))
                                .clickable { selectedAccountId = account.id }
                                .padding(12.dp)
                        ) {
                            RadioButton(
                                selected = sel,
                                onClick = { selectedAccountId = account.id },
                                colors = RadioButtonDefaults.colors(selectedColor = brand)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(account.accountNumber, color = ink, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("%,.0f MGA".format(account.balance).replace(",", " "), color = muted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Méthode
            item {
                SectionLabel("Méthode de dépôt", ink)
                Spacer(Modifier.height(8.dp))
                depositMethods.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { method ->
                            val isSel = method.id == selectedMethod.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) brandSoftBg else cardBg)
                                    .border(if (isSel) 1.5.dp else 1.dp, if (isSel) brand else border, RoundedCornerShape(12.dp))
                                    .clickable { selectedMethod = method }
                                    .padding(vertical = 14.dp)
                            ) {
                                Icon(method.icon, null, tint = if (isSel) brand else method.color, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.height(6.dp))
                                Text(method.label, color = if (isSel) brand else ink, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal)
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            // Téléphone
            if (selectedMethod.requiresPhone) {
                item {
                    SectionLabel("Numéro ${selectedMethod.label}", ink)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it },
                        placeholder = { Text("034 XX XXX XX", color = muted) },
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = muted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = brand, unfocusedBorderColor = border, focusedTextColor = ink, unfocusedTextColor = ink, cursorColor = brand),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Montant
            item {
                SectionLabel("Montant (MGA)", ink)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("Ex: 50 000", color = muted) },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = muted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = brand, unfocusedBorderColor = border, focusedTextColor = ink, unfocusedTextColor = ink, cursorColor = brand),
                    modifier = Modifier.fillMaxWidth()
                )
                amountText.toDoubleOrNull()?.takeIf { it > 0 }?.let {
                    Spacer(Modifier.height(6.dp))
                    Text("%,.0f MGA".format(it).replace(",", " "), color = SemanticSuccess, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10_000, 20_000, 50_000, 100_000).forEach { quick ->
                        FilterChip(
                            selected = amountText == quick.toString(),
                            onClick = { amountText = quick.toString() },
                            label = { Text("%,d".format(quick).replace(",", " "), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = brand, selectedLabelColor = Color.White)
                        )
                    }
                }
            }

            // Erreur
            if (state.error != null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().background(SemanticDanger.copy(alpha = 0.1f), RoundedCornerShape(10.dp)).padding(12.dp)) {
                        Text(state.error, color = SemanticDanger, fontSize = 13.sp)
                    }
                }
            }

            // Bouton
            item {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                val phoneValid = !selectedMethod.requiresPhone || phoneText.isNotBlank()
                val canSubmit = amount >= 1000 && selectedAccountId != 0 && phoneValid && !state.isDepositing

                Button(
                    onClick = { vm.deposit(selectedAccountId, amount, selectedMethod.id, phoneText.takeIf { selectedMethod.requiresPhone }) },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brand),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    if (state.isDepositing) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    } else {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Déposer", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DepositPendingScreen(
    result: com.stephan.mobil.data.model.DepositResult,
    isConfirming: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val darkMode = LocalDarkMode.current
    val brand    = LocalBrandColor.current
    val ink    = if (darkMode) TextPrimary else Color(0xFF17181C)
    val bg     = if (darkMode) BgBase else Color.White
    val cardBg = if (darkMode) BgSurfaceElevated else Color(0xFFF8F9FC)
    val context = LocalContext.current

    val isMobileMoney = result.method in listOf("mvola", "orange_money", "airtel_money")
    val ussdCode      = if (isMobileMoney) buildUssdCode(result.amount, result.reference) else null

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && ussdCode != null) launchUssd(context, ussdCode)
    }

    // Lancer le USSD automatiquement à l'affichage
    LaunchedEffect(Unit) {
        if (ussdCode != null) permissionLauncher.launch(Manifest.permission.CALL_PHONE)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.weight(1f))

        // Spinner animé
        CircularProgressIndicator(
            color = brand,
            strokeWidth = 4.dp,
            modifier = Modifier.size(72.dp)
        )

        Spacer(Modifier.height(28.dp))
        Text(
            "En attente du dépôt",
            color = ink,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "%,.0f MGA".format(result.amount).replace(",", " "),
            color = brand,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(6.dp))
        Text("Réf : ${result.reference}", color = TextSecondary, fontSize = 12.sp)

        if (ussdCode != null) {
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardBg)
                    .padding(16.dp)
            ) {
                Text("Paiement USSD lancé", color = ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Complétez le paiement sur votre téléphone, puis appuyez sur « J'ai payé ».",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(ussdCode, color = brand, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.CALL_PHONE) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = brand),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhoneAndroid, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Relancer le USSD", fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Bouton confirmer
        Button(
            onClick = onConfirm,
            enabled = !isConfirming,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SemanticSuccess),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            if (isConfirming) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("J'ai payé — Confirmer", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Bouton annuler
        OutlinedButton(
            onClick = onCancel,
            enabled = !isConfirming,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SemanticDanger),
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Annuler le dépôt", fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun DepositSuccessScreen(
    result: com.stephan.mobil.data.model.DepositResult,
    onDone: () -> Unit
) {
    val darkMode = LocalDarkMode.current
    val brand    = LocalBrandColor.current
    val ink = if (darkMode) TextPrimary else Color(0xFF17181C)
    val bg  = if (darkMode) BgBase else Color.White

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(32.dp)
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(SemanticSuccess.copy(alpha = 0.15f))
        ) {
            Icon(Icons.Default.Check, null, tint = SemanticSuccess, modifier = Modifier.size(44.dp))
        }

        Spacer(Modifier.height(24.dp))
        Text("Dépôt confirmé !", color = ink, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "%,.0f MGA".format(result.amount).replace(",", " "),
            color = SemanticSuccess,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(16.dp))
        Text("Réf : ${result.reference}", color = TextSecondary, fontSize = 13.sp)
        result.newBalance?.let {
            Spacer(Modifier.height(4.dp))
            Text("Nouveau solde : %,.0f MGA".format(it).replace(",", " "), color = ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onDone,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = brand),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text("Retour au tableau de bord", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(text, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
}
