package com.stephan.mobil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.ui.theme.*
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel
import com.stephan.mobil.ui.viewmodel.CryptoUiState

private val CvAccent = Color(0xFFE2E2E5)
private val CvGreen  = Color(0xFF10B981)

data class CurrencyInfo(val code: String, val name: String, val flag: String)

private val ALL_CURRENCIES = listOf(
    CurrencyInfo("MGA", "Ariary malgache",    "🇲🇬"),
    CurrencyInfo("USD", "Dollar américain",   "🇺🇸"),
    CurrencyInfo("EUR", "Euro",               "🇪🇺"),
    CurrencyInfo("GBP", "Livre sterling",     "🇬🇧"),
    CurrencyInfo("JPY", "Yen japonais",       "🇯🇵"),
    CurrencyInfo("CNY", "Yuan chinois",       "🇨🇳"),
    CurrencyInfo("CHF", "Franc suisse",       "🇨🇭"),
    CurrencyInfo("CAD", "Dollar canadien",    "🇨🇦"),
    CurrencyInfo("AUD", "Dollar australien",  "🇦🇺"),
    CurrencyInfo("ZAR", "Rand sud-africain",  "🇿🇦"),
    CurrencyInfo("INR", "Roupie indienne",    "🇮🇳"),
    CurrencyInfo("KMF", "Franc comorien",     "🇰🇲"),
    CurrencyInfo("BRL", "Réal brésilien",     "🇧🇷"),
    CurrencyInfo("SGD", "Dollar de Singapour","🇸🇬"),
    CurrencyInfo("AED", "Dirham émirati",     "🇦🇪"),
)

private val POPULAR_CODES = listOf("MGA", "USD", "EUR", "GBP", "JPY", "CNY", "CHF", "ZAR")

private val FALLBACK_RATES = mapOf(
    "USD" to 1.0,    "MGA" to 4500.0, "EUR" to 0.92,
    "GBP" to 0.79,  "JPY" to 157.0,  "CNY" to 7.24,
    "CHF" to 0.90,  "CAD" to 1.37,   "AUD" to 1.53,
    "ZAR" to 18.3,  "INR" to 83.5,   "KMF" to 452.0,
    "BRL" to 4.97,  "SGD" to 1.35,   "AED" to 3.67,
)

private fun effectiveRates(cryptoState: CryptoUiState): Map<String, Double> {
    val base = if (cryptoState.exchangeRates.isNotEmpty()) cryptoState.exchangeRates else FALLBACK_RATES
    return if (cryptoState.mgaPerUsd > 0) base + ("MGA" to cryptoState.mgaPerUsd) else base
}

private fun convert(amount: Double, from: String, to: String, rates: Map<String, Double>): Double {
    val fromRate = rates[from] ?: return 0.0
    val toRate   = rates[to]   ?: return 0.0
    if (fromRate == 0.0) return 0.0
    return amount * (toRate / fromRate)
}

private fun fmtAmount(v: Double, code: String): String {
    if (v <= 0.0) return "0"
    return when (code) {
        "JPY", "MGA", "KMF" -> "%,.0f".format(v).replace(",", " ")
        else                 -> "%,.2f".format(v).replace(",", " ")
    }
}

private fun fmtRate(from: String, to: String, rates: Map<String, Double>): String {
    val result = convert(1.0, from, to, rates)
    return "1 $from = ${fmtAmount(result, to)} $to"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    state: BankUiState,
    cryptoState: CryptoUiState,
    vm: BankViewModel,
    onBack: () -> Unit,
    onLoadRates: () -> Unit = {}
) {
    val darkMode = LocalDarkMode.current
    val CvBg   = if (darkMode) Color(0xFF0D0E12) else Color(0xFFF8F9FA)
    val CvCard = if (darkMode) Color(0xFF1A1C23) else Color(0xFFEFF0F3)
    val CvText = if (darkMode) Color.White       else Color(0xFF17181C)
    val CvMuted = if (darkMode) Color(0xFF8B8F98) else Color(0xFF6B7280)
    val CvLine  = if (darkMode) Color(0xFF2A2D35) else Color(0xFFDEE0E6)

    var fromCode  by remember { mutableStateOf("USD") }
    var toCode    by remember { mutableStateOf("MGA") }
    var fromInput by remember { mutableStateOf("100") }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker   by remember { mutableStateOf(false) }
    var pickerSearch   by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { onLoadRates() }

    val rates   = effectiveRates(cryptoState)
    val fromVal = fromInput.toDoubleOrNull() ?: 0.0
    val toVal   = convert(fromVal, fromCode, toCode, rates)
    val isLive  = cryptoState.exchangeRates.isNotEmpty()

    val fromInfo = ALL_CURRENCIES.find { it.code == fromCode }
    val toInfo   = ALL_CURRENCIES.find { it.code == toCode }

    // ── Currency picker sheet ─────────────────────────────────────────────────
    if (showFromPicker || showToPicker) {
        ModalBottomSheet(
            onDismissRequest = { showFromPicker = false; showToPicker = false; pickerSearch = "" },
            containerColor = CvCard,
            dragHandle = { BottomSheetDefaults.DragHandle(color = CvMuted) }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(
                    if (showFromPicker) "Devise source" else "Devise cible",
                    color = CvText, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = pickerSearch,
                    onValueChange = { pickerSearch = it },
                    placeholder = { Text("Rechercher…", color = CvMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = CvMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CvText, unfocusedTextColor = CvText,
                        focusedBorderColor = CvAccent, unfocusedBorderColor = CvLine,
                        cursorColor = CvAccent
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                val filtered = ALL_CURRENCIES.filter {
                    pickerSearch.isBlank() ||
                    it.code.contains(pickerSearch, ignoreCase = true) ||
                    it.name.contains(pickerSearch, ignoreCase = true)
                }
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    items(filtered) { info ->
                        val selected = if (showFromPicker) info.code == fromCode else info.code == toCode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) CvAccent.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable {
                                    if (showFromPicker) fromCode = info.code
                                    else toCode = info.code
                                    showFromPicker = false; showToPicker = false; pickerSearch = ""
                                }
                                .padding(horizontal = 12.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(info.flag, fontSize = 26.sp)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(info.code, color = CvText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(info.name, color = CvMuted, fontSize = 12.sp)
                            }
                            if (selected) Icon(Icons.Default.Check, null, tint = CvAccent, modifier = Modifier.size(18.dp))
                        }
                        if (info != filtered.last()) Divider(color = CvLine, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }

    // ── Main screen ───────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CvBg)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = CvText)
            }
            Text(
                "Convertisseur",
                color = CvText, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isLive) CvGreen.copy(alpha = 0.15f) else CvLine)
                    .clickable { onLoadRates() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Refresh, null,
                    tint = if (isLive) CvGreen else CvMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Spacer(Modifier.height(8.dp))

        // FROM card
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CvCard)
                .padding(16.dp)
        ) {
            Text("DE", color = CvMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Currency selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CvLine)
                        .clickable { showFromPicker = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(fromInfo?.flag ?: "🌐", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(fromCode, color = CvText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = CvMuted, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.weight(1f))
                // Amount input
                BasicTextField(
                    value = fromInput,
                    onValueChange = { new ->
                        val filtered = new.filter { it.isDigit() || it == '.' }
                        val dots = filtered.count { it == '.' }
                        if (dots <= 1 && filtered.length <= 15) fromInput = filtered
                    },
                    textStyle = TextStyle(
                        color = CvText,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    ),
                    cursorBrush = SolidColor(CvAccent),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.widthIn(min = 80.dp, max = 220.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(fromInfo?.name ?: "", color = CvMuted, fontSize = 12.sp)
        }

        // Swap button
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CvAccent)
                    .clickable {
                        val tmp = fromCode; fromCode = toCode; toCode = tmp
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SwapVert, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }

        // TO card
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CvCard)
                .padding(16.dp)
        ) {
            Text("VERS", color = CvMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CvLine)
                        .clickable { showToPicker = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(toInfo?.flag ?: "🌐", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(toCode, color = CvText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = CvMuted, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = fmtAmount(toVal, toCode),
                    color = if (toVal > 0) CvGreen else CvMuted,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.widthIn(max = 220.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(toInfo?.name ?: "", color = CvMuted, fontSize = 12.sp)
        }

        Spacer(Modifier.height(20.dp))

        // Rate info
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CvCard)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isLive) CvGreen else CvMuted)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    fmtRate(fromCode, toCode, rates),
                    color = CvText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (isLive) "Taux en direct · open.er-api.com" else "Taux approximatifs · sans réseau",
                    color = CvMuted, fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Popular currencies
        Text(
            "Devises populaires",
            color = CvMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(POPULAR_CODES) { code ->
                val info = ALL_CURRENCIES.find { it.code == code }
                val active = code == fromCode || code == toCode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (active) CvAccent.copy(alpha = 0.18f) else CvCard
                        )
                        .clickable {
                            when {
                                code == fromCode -> Unit
                                code == toCode   -> Unit
                                else             -> toCode = code
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(info?.flag ?: "🌐", fontSize = 16.sp)
                        Text(
                            code,
                            color = if (active) CvAccent else CvText,
                            fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
