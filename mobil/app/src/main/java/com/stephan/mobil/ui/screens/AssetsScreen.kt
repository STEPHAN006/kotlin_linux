package com.stephan.mobil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.data.model.Account
import com.stephan.mobil.data.model.Card
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel

private val White = Color.White
private val AssetInk = Color(0xFF17181C)
private val AssetMuted = Color(0xFF8B8F98)
private val AssetLine = Color(0xFFF0F1F3)
private val AssetSoft = Color(0xFFF4F5F7)

private data class CryptoAsset(
    val symbol: String,
    val name: String,
    val amount: Double,
    val usdValue: Double,
    val color: Color,
    val gradient: List<Color>? = null,
    val label: String
)

private val cryptoAssets = listOf(
    CryptoAsset("USDT", "TetherUS", 4.352, 4.35, Color(0xFF26A17B), null, "T"),
    CryptoAsset("USDC", "USD Coin", 0.0, 0.0, Color(0xFF2775CA), null, "$"),
    CryptoAsset("BTC", "Bitcoin", 0.0, 0.0, Color(0xFFF7931A), null, "₿"),
    CryptoAsset("ETH", "Ethereum", 0.0, 0.0, Color(0xFF627EEA), null, "Ξ"),
    CryptoAsset("SOL", "Solana", 0.0, 0.0, Color(0xFF9945FF), null, "◎"),
    CryptoAsset("BNB", "BNB Chain", 0.0, 0.0, Color(0xFFF3BA2F), null, "B"),
    CryptoAsset("S", "Sonic", 0.0, 0.0, Color(0xFF1A1A1A), null, "S"),
    CryptoAsset("TON", "Toncoin", 0.0, 0.0, Color(0xFF0088CC), null, "T"),
)

@Composable
fun AssetsScreen(
    state: BankUiState,
    vm: BankViewModel,
    darkMode: Boolean = false
) {
    var selectedTab by remember { mutableStateOf(0) }
    var balanceVisible by remember { mutableStateOf(true) }

    val totalMga = state.balance.totalBalance
    val totalUsd = totalMga / 4500.0
    val totalUsdt = cryptoAssets.sumOf { it.usdValue }

    val bg = if (darkMode) Color(0xFF101114) else White
    val ink = if (darkMode) White else AssetInk
    val muted = AssetMuted
    val line = if (darkMode) Color(0xFF2A2D3A) else AssetLine

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Header balance
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Valeur Totale Estimée",
                        color = muted,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        null,
                        tint = muted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    IconButton(
                        onClick = { balanceVisible = !balanceVisible },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            if (balanceVisible) Icons.Default.RemoveRedEye else Icons.Default.VisibilityOff,
                            null,
                            tint = muted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (balanceVisible) {
                            if (selectedTab == 0) "%.2f".format(totalUsdt).replace(".", ",")
                            else "%,.0f".format(totalMga).replace(",", " ")
                        } else "••••",
                        color = ink,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    )
                    Spacer(Modifier.width(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (selectedTab == 0) "USD" else "MGA",
                            color = ink,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            null,
                            tint = muted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Action buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AssetActionBtn(
                    label = "Ajouter des fonds",
                    primary = true,
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
                AssetActionBtn(
                    label = "Earn",
                    primary = false,
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
                AssetActionBtn(
                    label = "Crédit",
                    primary = false,
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        // Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                listOf("Crypto", "Décret").forEachIndexed { index, label ->
                    Column(
                        modifier = Modifier.clickable { selectedTab = index },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            label,
                            color = if (selectedTab == index) ink else muted,
                            fontSize = 15.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(
                                    if (selectedTab == index) AssetInk else Color.Transparent
                                )
                        )
                    }
                }
            }
            Divider(color = line, thickness = 1.dp)
        }

        // Asset list
        if (selectedTab == 0) {
            items(cryptoAssets) { asset ->
                CryptoRow(asset = asset, visible = balanceVisible, line = line, ink = ink)
            }
        } else {
            // Fiat: bank accounts
            if (state.balance.accounts.isEmpty()) {
                item {
                    FiatRow(
                        symbol = "MGA",
                        name = "Ariary Malgache",
                        amount = if (balanceVisible) "%,.0f".format(totalMga).replace(",", " ") else "••••",
                        usdValue = if (balanceVisible) "≈ %.2f USD".format(totalUsd) else "••••",
                        color = Color(0xFFD92C55),
                        label = "Ar",
                        line = line,
                        ink = ink
                    )
                    FiatRow(
                        symbol = "EUR",
                        name = "Euro",
                        amount = if (balanceVisible) "0,00" else "••••",
                        usdValue = if (balanceVisible) "≈ 0,00 USD" else "••••",
                        color = Color(0xFF003399),
                        label = "€",
                        line = line,
                        ink = ink
                    )
                    FiatRow(
                        symbol = "USD",
                        name = "Dollar US",
                        amount = if (balanceVisible) "0,00" else "••••",
                        usdValue = if (balanceVisible) "≈ 0,00 USD" else "••••",
                        color = Color(0xFF1A7F37),
                        label = "$",
                        line = line,
                        ink = ink
                    )
                }
            } else {
                items(state.balance.accounts) { account ->
                    AccountFiatRow(account = account, visible = balanceVisible, line = line, ink = ink)
                }
            }

            // Cards section
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cartes virtuelles", color = AssetMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text(
                        "+ Nouvelle",
                        color = Color(0xFFD92C55),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            state.balance.accounts.firstOrNull()?.let {
                                vm.createCard(it.id, 5_000_000.0)
                            }
                        }
                    )
                }
                Divider(color = line)
            }

            if (state.cards.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aucune carte. Appuyez sur + Nouvelle.", color = AssetMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(state.cards) { card ->
                    CardRow(card = card, vm = vm, line = line, ink = ink)
                }
            }
        }
    }
}

@Composable
private fun AssetActionBtn(label: String, primary: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (primary) AssetInk else AssetSoft)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (primary) White else AssetInk,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun CryptoRow(asset: CryptoAsset, visible: Boolean, line: Color, ink: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(asset.color),
            contentAlignment = Alignment.Center
        ) {
            Text(asset.label, color = White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(asset.symbol, color = ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(asset.name, color = AssetMuted, fontSize = 13.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                if (visible) "%.3f".format(asset.amount).trimEnd('0').trimEnd('.').ifEmpty { "0" } else "••••",
                color = ink,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                if (visible) "≈ %.2f USD".format(asset.usdValue).replace(".", ",") else "••••",
                color = AssetMuted,
                fontSize = 12.sp
            )
        }
    }
    Divider(modifier = Modifier.padding(horizontal = 20.dp), color = line, thickness = 0.5.dp)
}

@Composable
private fun FiatRow(symbol: String, name: String, amount: String, usdValue: String, color: Color, label: String, line: Color, ink: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(symbol, color = ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(name, color = AssetMuted, fontSize = 13.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(amount, color = ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(usdValue, color = AssetMuted, fontSize = 12.sp)
        }
    }
    Divider(modifier = Modifier.padding(horizontal = 20.dp), color = line, thickness = 0.5.dp)
}

@Composable
private fun AccountFiatRow(account: Account, visible: Boolean, line: Color, ink: Color) {
    val label = when (account.type) {
        "savings" -> "Ep"
        "business" -> "Biz"
        else -> "Ar"
    }
    val usdValue = account.balance / 4500.0
    FiatRow(
        symbol = account.currency,
        name = "${account.type.replaceFirstChar { it.uppercase() }} · ****${account.accountNumber.takeLast(4)}",
        amount = if (visible) account.formattedBalance else "••••",
        usdValue = if (visible) "≈ ${"%.2f".format(usdValue)} USD" else "••••",
        color = Color(0xFFD92C55),
        label = label,
        line = line,
        ink = ink
    )
}

@Composable
private fun CardRow(card: com.stephan.mobil.data.model.Card, vm: BankViewModel, line: Color, ink: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1A1A2E), Color(0xFFD92C55))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CreditCard, null, tint = White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(card.cardNumberMasked, color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Expire ${card.expiryDate} · Limite ${"%,.0f".format(card.dailyLimit).replace(",", " ")} MGA",
                color = AssetMuted,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = !card.isBlocked,
            onCheckedChange = { vm.toggleCard(card.id) },
            colors = SwitchDefaults.colors(
                checkedTrackColor = Color(0xFFD92C55),
                uncheckedTrackColor = AssetSoft
            ),
            modifier = Modifier.size(width = 44.dp, height = 24.dp)
        )
    }
    Divider(modifier = Modifier.padding(horizontal = 20.dp), color = line, thickness = 0.5.dp)
}
