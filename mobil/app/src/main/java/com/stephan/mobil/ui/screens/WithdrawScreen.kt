package com.stephan.mobil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.data.model.Beneficiary
import com.stephan.mobil.data.model.ScheduledWithdrawal
import com.stephan.mobil.ui.theme.*
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel

private val LightPageBg   = Color(0xFFF5F6FA)
private val LightCardBg   = Color.White
private val LightInk      = Color(0xFF17181C)
private val SharedMuted   = Color(0xFF8B8F98)
private val LightBorder   = Color(0xFFECEEF2)
private val WarnAmber     = Color(0xFFF57C00)

@Composable
fun WithdrawScreen(
    state: BankUiState,
    vm: BankViewModel,
    onBack: () -> Unit
) {
    val transferred = state.message?.contains("virement", ignoreCase = true) == true ||
                      state.message?.contains("envoy", ignoreCase = true) == true ||
                      state.message?.contains("transf", ignoreCase = true) == true
    val scheduledCreated = state.message?.contains("programmé", ignoreCase = true) == true

    if ((transferred && state.pendingTransfer == null) || scheduledCreated) {
        WithdrawSuccessScreen(
            message = if (scheduledCreated) state.message ?: "Retrait automatique programmé !" else null,
            onDone = { vm.consumeMessages(); onBack() }
        )
        return
    }

    WithdrawFormScreen(state = state, vm = vm, onBack = onBack)
}

private enum class WithdrawMode { SIMPLE, AUTO }

@Composable
private fun WithdrawFormScreen(
    state: BankUiState,
    vm: BankViewModel,
    onBack: () -> Unit
) {
    val darkMode   = LocalDarkMode.current
    val brand      = LocalBrandColor.current
    val pageBg     = if (darkMode) BgBase       else LightPageBg
    val cardBg     = if (darkMode) BgSurface    else LightCardBg
    val ink        = if (darkMode) TextPrimary  else LightInk
    val muted      = SharedMuted
    val border     = if (darkMode) BgSurfaceTop else LightBorder
    val warnBg     = if (darkMode) Color(0xFF2A1F00) else Color(0xFFFFF8E1)
    val accounts      = state.balance.accounts
    val beneficiaries = state.beneficiaries
    var mode          by remember { mutableStateOf(WithdrawMode.SIMPLE) }
    var selectedBenef by remember { mutableStateOf<Beneficiary?>(null) }
    var amountText    by remember { mutableStateOf("") }
    var noteText      by remember { mutableStateOf("") }
    var freqDays      by remember { mutableIntStateOf(7) }

    val senderId   = accounts.firstOrNull()?.id ?: 0
    val amount     = amountText.toDoubleOrNull() ?: 0.0
    val canSubmit  = selectedBenef != null && amount >= 1_000 && senderId != 0 && !state.loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ink)
            }
            Column {
                Text("Retrait", color = ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Vers un bénéficiaire", color = muted, fontSize = 12.sp)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp)
        ) {
            // Solde dispo
            if (accounts.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardBg)
                            .border(1.dp, border, RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(brand.copy(alpha = 0.12f))
                            ) {
                                Icon(Icons.Default.AccountBalance, null, tint = brand, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Solde disponible", color = muted, fontSize = 12.sp)
                                Text(
                                    "%,.0f MGA".format(accounts.first().balance).replace(",", " "),
                                    color = ink,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SemanticSuccess.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Actif", color = SemanticSuccess, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Toggle Simple / Automatique
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(border)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WithdrawMode.entries.forEach { m ->
                        val active = mode == m
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) cardBg else Color.Transparent)
                                .clickable { mode = m }
                                .padding(vertical = 10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (m == WithdrawMode.SIMPLE) Icons.Default.ArrowDownward else Icons.Default.Repeat,
                                    contentDescription = null,
                                    tint = if (active) brand else muted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (m == WithdrawMode.SIMPLE) "Retrait simple" else "Automatique",
                                    color = if (active) ink else muted,
                                    fontSize = 13.sp,
                                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                if (mode == WithdrawMode.AUTO) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(warnBg)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, null, tint = WarnAmber, modifier = Modifier.size(16.dp))
                        Text(
                            "Le retrait sera effectué automatiquement tous les $freqDays jour(s).",
                            color = WarnAmber,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Fréquence (mode AUTO seulement)
            if (mode == WithdrawMode.AUTO) {
                item {
                    Text("Fréquence", color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1 to "Chaque jour", 7 to "Chaque semaine", 14 to "2 semaines", 30 to "Chaque mois").forEach { (days, label) ->
                            FilterChip(
                                selected = freqDays == days,
                                onClick  = { freqDays = days },
                                label    = { Text(label, fontSize = 12.sp) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = brand,
                                    selectedLabelColor     = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Bénéficiaires
            item {
                Text("Bénéficiaire", color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                if (beneficiaries.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardBg)
                            .border(1.dp, border, RoundedCornerShape(14.dp))
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.PersonOff, null, tint = muted, modifier = Modifier.size(32.dp))
                            Text("Aucun bénéficiaire enregistré", color = muted, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        beneficiaries.forEach { benef ->
                            val selected = selectedBenef?.id == benef.id
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (selected) brand.copy(alpha = 0.06f) else cardBg)
                                    .border(
                                        if (selected) 1.5.dp else 1.dp,
                                        if (selected) brand else border,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { selectedBenef = benef }
                                    .padding(14.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(channelColor(benef.channel).copy(alpha = 0.15f))
                                ) {
                                    Text(
                                        benef.name.take(1).uppercase(),
                                        color = channelColor(benef.channel),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(benef.name, color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(benef.bankName, color = muted, fontSize = 12.sp)
                                    Text(benef.accountNumberMasked, color = muted, fontSize = 11.sp)
                                }
                                if (selected) {
                                    Icon(Icons.Default.CheckCircle, null, tint = brand, modifier = Modifier.size(22.dp))
                                } else {
                                    Icon(Icons.Default.RadioButtonUnchecked, null, tint = border, modifier = Modifier.size(22.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Montant
            item {
                Text("Montant (MGA)", color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("Ex : 50 000", color = muted) },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = muted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = brand,
                        unfocusedBorderColor = border,
                        focusedTextColor     = ink,
                        unfocusedTextColor   = ink,
                        cursorColor          = brand,
                        focusedContainerColor   = cardBg,
                        unfocusedContainerColor = cardBg
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (amount > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "%,.0f MGA".format(amount).replace(",", " "),
                        color = brand,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10_000, 20_000, 50_000, 100_000).forEach { quick ->
                        FilterChip(
                            selected = amountText == quick.toString(),
                            onClick  = { amountText = quick.toString() },
                            label    = { Text("%,d".format(quick).replace(",", " "), fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = brand,
                                selectedLabelColor     = Color.White
                            )
                        )
                    }
                }
            }

            // Note optionnelle
            item {
                Text("Note (optionnelle)", color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Ex : remboursement, loyer…", color = muted) },
                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = muted) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = brand,
                        unfocusedBorderColor = border,
                        focusedTextColor     = ink,
                        unfocusedTextColor   = ink,
                        cursorColor          = brand,
                        focusedContainerColor   = cardBg,
                        unfocusedContainerColor = cardBg
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Erreur
            if (state.error != null) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SemanticDanger.copy(alpha = 0.08f))
                            .padding(12.dp)
                    ) {
                        Icon(Icons.Default.Error, null, tint = SemanticDanger, modifier = Modifier.size(18.dp))
                        Text(state.error, color = SemanticDanger, fontSize = 13.sp)
                    }
                }
            }

            // Récapitulatif + bouton
            item {
                if (selectedBenef != null && amount >= 1_000) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardBg)
                            .border(1.dp, border, RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Récapitulatif", color = ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        HorizontalDivider(color = border)
                        SummaryRow("Bénéficiaire", selectedBenef!!.name)
                        SummaryRow("Banque", selectedBenef!!.bankName)
                        SummaryRow("Compte", selectedBenef!!.accountNumberMasked)
                        SummaryRow("Montant", "%,.0f MGA".format(amount).replace(",", " "))
                        if (mode == WithdrawMode.AUTO) {
                            SummaryRow("Fréquence", freqLabel(freqDays))
                        }
                        if (mode == WithdrawMode.SIMPLE && amount > 500_000) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(warnBg)
                                    .padding(10.dp)
                            ) {
                                Icon(Icons.Default.Warning, null, tint = WarnAmber, modifier = Modifier.size(16.dp))
                                Text("Un OTP sera envoyé par email", color = WarnAmber, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        val benef = selectedBenef ?: return@Button
                        if (mode == WithdrawMode.SIMPLE) {
                            vm.createTransfer(
                                senderId   = senderId,
                                receiverId = benef.id,
                                amount     = amount,
                                note       = noteText.ifBlank { "Retrait vers ${benef.name}" }
                            )
                        } else {
                            vm.createScheduledWithdrawal(
                                senderAccountId = senderId,
                                beneficiaryId   = benef.id,
                                amount          = amount,
                                note            = noteText,
                                frequencyDays   = freqDays
                            )
                        }
                    },
                    enabled  = canSubmit,
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = brand),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    } else {
                        Icon(
                            imageVector = if (mode == WithdrawMode.SIMPLE) Icons.Default.KeyboardArrowDown else Icons.Default.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (mode == WithdrawMode.SIMPLE) "Confirmer le retrait" else "Programmer le retrait",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Liste des retraits automatiques existants
            if (state.scheduledWithdrawals.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Retraits programmés", color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("${state.scheduledWithdrawals.size}", color = muted, fontSize = 12.sp)
                    }
                }
                items(state.scheduledWithdrawals) { sw ->
                    ScheduledWithdrawalCard(sw = sw, vm = vm)
                }
            }
        }
    }
}

@Composable
private fun ScheduledWithdrawalCard(sw: ScheduledWithdrawal, vm: BankViewModel) {
    val darkMode = LocalDarkMode.current
    val brand    = LocalBrandColor.current
    val cardBg   = if (darkMode) BgSurface    else LightCardBg
    val ink      = if (darkMode) TextPrimary  else LightInk
    val muted    = SharedMuted
    val border   = if (darkMode) BgSurfaceTop else LightBorder
    var showDelete by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, if (sw.isActive) border else border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background((if (sw.isActive) brand else muted).copy(alpha = 0.12f))
        ) {
            Icon(
                Icons.Default.Repeat,
                null,
                tint = if (sw.isActive) brand else muted,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                sw.beneficiary?.name ?: "—",
                color = if (sw.isActive) ink else muted,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "%,.0f MGA · %s".format(sw.amount, freqLabel(sw.frequencyDays)).replace(",", " "),
                color = muted,
                fontSize = 12.sp
            )
            if (sw.nextRunAt != null) {
                Text(
                    "Prochain : ${sw.nextRunAt.take(10)}",
                    color = muted,
                    fontSize = 11.sp
                )
            }
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Switch(
                checked = sw.isActive,
                onCheckedChange = { vm.toggleScheduledWithdrawal(sw.id) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor  = Color.White,
                    checkedTrackColor  = brand,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = muted.copy(alpha = 0.4f)
                ),
                modifier = Modifier.size(width = 46.dp, height = 26.dp)
            )
            IconButton(
                onClick = { showDelete = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, null, tint = muted, modifier = Modifier.size(18.dp))
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Supprimer ?") },
            text = { Text("Le retrait automatique vers ${sw.beneficiary?.name} sera supprimé.") },
            confirmButton = {
                TextButton(onClick = { vm.deleteScheduledWithdrawal(sw.id); showDelete = false }) {
                    Text("Supprimer", color = brand)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun WithdrawSuccessScreen(message: String?, onDone: () -> Unit) {
    val darkMode = LocalDarkMode.current
    val brand    = LocalBrandColor.current
    val pageBg   = if (darkMode) BgBase      else LightPageBg
    val ink      = if (darkMode) TextPrimary else LightInk
    val muted    = SharedMuted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .padding(32.dp)
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(SemanticSuccess.copy(alpha = 0.12f))
        ) {
            Icon(Icons.Default.Check, null, tint = SemanticSuccess, modifier = Modifier.size(48.dp))
        }

        Spacer(Modifier.height(28.dp))
        Text(
            if (message != null) "Retrait programmé !" else "Retrait effectué !",
            color = ink,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message ?: "Votre retrait a été traité avec succès.",
            color = muted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick   = onDone,
            shape     = RoundedCornerShape(14.dp),
            colors    = ButtonDefaults.buttonColors(containerColor = brand),
            modifier  = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text("Retour au tableau de bord", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val darkMode = LocalDarkMode.current
    val ink      = if (darkMode) TextPrimary else LightInk
    val muted    = SharedMuted
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = muted, fontSize = 13.sp)
        Text(value, color = ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private fun freqLabel(days: Int): String = when (days) {
    1    -> "Chaque jour"
    7    -> "Chaque semaine"
    14   -> "Toutes les 2 semaines"
    30   -> "Chaque mois"
    else -> "Tous les $days jours"
}

private fun channelColor(channel: String): Color = when (channel.lowercase()) {
    "mvola"        -> Color(0xFFD32F2F)
    "orange_money" -> Color(0xFFFF6F00)
    "airtel_money" -> Color(0xFFE53935)
    else           -> Color(0xFF1976D2)
}
