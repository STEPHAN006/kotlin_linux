package com.stephan.mobil.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import coil.compose.AsyncImage
import com.stephan.mobil.data.model.Account
import com.stephan.mobil.data.model.CoinMarketData
import com.stephan.mobil.data.model.CryptoWallet
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel

private val White = Color.White
private val AssetInk = Color(0xFF17181C)
private val AssetMuted = Color(0xFF8B8F98)
private val AssetLine = Color(0xFFF0F1F3)
private val AssetSoft = Color(0xFFF4F5F7)

private val COIN_COLORS = mapOf(
    "bitcoin"      to Color(0xFFF7931A),
    "ethereum"     to Color(0xFF627EEA),
    "solana"       to Color(0xFF9945FF),
    "binancecoin"  to Color(0xFFF3BA2F),
    "tether"       to Color(0xFF26A17B),
    "usd-coin"     to Color(0xFF2775CA),
    "toncoin"      to Color(0xFF0088CC),
    "sonic-3"      to Color(0xFF1A1A1A),
)

private val COIN_LABELS = mapOf(
    "bitcoin"      to "₿",
    "ethereum"     to "Ξ",
    "solana"       to "◎",
    "binancecoin"  to "B",
    "tether"       to "T",
    "usd-coin"     to "\$",
    "toncoin"      to "T",
    "sonic-3"      to "S",
)

@Composable
fun AssetsScreen(
    state: BankUiState,
    vm: BankViewModel,
    darkMode: Boolean = false
) {
    var selectedTab by remember { mutableStateOf(0) }
    var balanceVisible by remember { mutableStateOf(true) }
    var selectedCoin by remember { mutableStateOf<CoinMarketData?>(null) }

    LaunchedEffect(Unit) { vm.loadCrypto() }

    val totalMga = state.balance.totalBalance
    val totalUsd = totalMga / state.mgaPerUsd
    val cryptoTotalUsd = state.cryptoWallets.sumOf { wallet ->
        val price = state.cryptoMarkets.find { it.id == wallet.coinId }?.currentPrice ?: 0.0
        wallet.balance * price
    }

    val bg = if (darkMode) Color(0xFF101114) else White
    val ink = if (darkMode) White else AssetInk
    val line = if (darkMode) Color(0xFF2A2D3A) else AssetLine

    if (selectedCoin != null) {
        val wallet = state.cryptoWallets.find { it.coinId == selectedCoin!!.id }
        CryptoDetailSheet(
            coin = selectedCoin!!,
            wallet = wallet,
            state = state,
            vm = vm,
            onDismiss = { selectedCoin = null }
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Valeur Totale Estimée", color = AssetMuted, fontSize = 14.sp)
                    Spacer(Modifier.width(10.dp))
                    IconButton(
                        onClick = { balanceVisible = !balanceVisible },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            if (balanceVisible) Icons.Default.RemoveRedEye else Icons.Default.VisibilityOff,
                            null, tint = AssetMuted, modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (balanceVisible) {
                            if (selectedTab == 0)
                                "%.2f".format(cryptoTotalUsd)
                            else
                                "%,.0f".format(totalMga).replace(",", " ")
                        } else "••••",
                        color = ink, fontSize = 44.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = (-1).sp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (selectedTab == 0) "USD" else "MGA",
                        color = ink, fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (selectedTab == 0 && balanceVisible && state.cryptoMarkets.isNotEmpty()) {
                    Text(
                        "≈ %,.0f MGA".format(cryptoTotalUsd * state.mgaPerUsd).replace(",", " "),
                        color = AssetMuted, fontSize = 14.sp
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AssetActionBtn("Acheter", true, Modifier.weight(1f)) {
                    state.cryptoMarkets.firstOrNull()?.let { selectedCoin = it }
                }
                AssetActionBtn("Vendre", false, Modifier.weight(1f)) {
                    state.cryptoMarkets.firstOrNull()?.let { selectedCoin = it }
                }
                AssetActionBtn("Envoyer", false, Modifier.weight(1f)) {
                    state.cryptoMarkets.firstOrNull()?.let { selectedCoin = it }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                listOf("Crypto", "Fiat").forEachIndexed { index, label ->
                    Column(
                        modifier = Modifier.clickable { selectedTab = index },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            label,
                            color = if (selectedTab == index) ink else AssetMuted,
                            fontSize = 15.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(if (selectedTab == index) AssetInk else Color.Transparent)
                        )
                    }
                }
            }
            Divider(color = line, thickness = 1.dp)
        }

        if (selectedTab == 0) {
            if (state.cryptoLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFD92C55))
                    }
                }
            } else if (state.cryptoMarkets.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Impossible de charger les prix", color = AssetMuted, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = { vm.loadCrypto() }) {
                                Text("Réessayer", color = Color(0xFFD92C55))
                            }
                        }
                    }
                }
            } else {
                items(state.cryptoMarkets) { market ->
                    val wallet = state.cryptoWallets.find { it.coinId == market.id }
                    CryptoMarketRow(
                        market = market,
                        wallet = wallet,
                        visible = balanceVisible,
                        line = line,
                        ink = ink,
                        onClick = { selectedCoin = market }
                    )
                }
            }
        } else {
            if (state.balance.accounts.isEmpty()) {
                item {
                    FiatRow("MGA", "Ariary Malgache",
                        if (balanceVisible) "%,.0f".format(totalMga).replace(",", " ") else "••••",
                        if (balanceVisible) "≈ %.2f USD".format(totalUsd) else "••••",
                        Color(0xFFD92C55), "Ar", line, ink)
                }
            } else {
                items(state.balance.accounts) { account ->
                    AccountFiatRow(account, balanceVisible, line, ink)
                }
            }

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
                        "+ Nouvelle", color = Color(0xFFD92C55), fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            state.balance.accounts.firstOrNull()?.let { vm.createCard(it.id, 5_000_000.0) }
                        }
                    )
                }
                Divider(color = line)
            }

            if (state.cards.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("Aucune carte. Appuyez sur + Nouvelle.", color = AssetMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(state.cards) { card ->
                    CardRow(card, vm, line, ink)
                }
            }
        }
    }
}

@Composable
private fun CryptoMarketRow(
    market: CoinMarketData,
    wallet: CryptoWallet?,
    visible: Boolean,
    line: Color,
    ink: Color,
    onClick: () -> Unit
) {
    val change = market.change24h ?: 0.0
    val isUp = change >= 0
    val changeColor = if (isUp) Color(0xFF10B981) else Color(0xFFEF4444)
    val coinColor = COIN_COLORS[market.id] ?: Color(0xFF8B8F98)
    val coinLabel = COIN_LABELS[market.id] ?: market.symbol.uppercase().take(2)
    val sparkPrices = market.sparkline?.price ?: emptyList()
    val balance = wallet?.balance ?: 0.0
    val balanceUsd = balance * market.currentPrice

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coin logo
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(coinColor),
            contentAlignment = Alignment.Center
        ) {
            if (market.image.isNotBlank()) {
                AsyncImage(
                    model = market.image, contentDescription = market.name,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(coinLabel, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(market.symbol.uppercase(), color = ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(market.name, color = AssetMuted, fontSize = 12.sp)
        }

        // Mini sparkline
        if (sparkPrices.size >= 4) {
            SparklineChart(
                prices = sparkPrices,
                isUp = isUp,
                modifier = Modifier.size(width = 60.dp, height = 30.dp)
            )
            Spacer(Modifier.width(12.dp))
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                if (visible) formatCryptoBalance(balance) else "••••",
                color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold
            )
            Text(
                if (visible) "≈ ${"%.2f".format(market.currentPrice)} USD" else "••••",
                color = AssetMuted, fontSize = 11.sp
            )
            Text(
                "%+.2f%%".format(change),
                color = changeColor, fontSize = 11.sp, fontWeight = FontWeight.Medium
            )
        }
    }
    Divider(modifier = Modifier.padding(horizontal = 20.dp), color = line, thickness = 0.5.dp)
}

@Composable
private fun SparklineChart(prices: List<Double>, isUp: Boolean, modifier: Modifier) {
    val lineColor = if (isUp) Color(0xFF10B981) else Color(0xFFEF4444)
    Canvas(modifier = modifier) {
        if (prices.size < 2) return@Canvas
        val min = prices.min()
        val max = prices.max()
        val range = (max - min).coerceAtLeast(0.0001)
        val w = size.width
        val h = size.height
        val path = Path()
        prices.forEachIndexed { i, price ->
            val x = i / (prices.size - 1).toFloat() * w
            val y = h - ((price - min) / range).toFloat() * h
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = lineColor, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
    }
}

private fun formatCryptoBalance(balance: Double): String {
    if (balance == 0.0) return "0"
    return when {
        balance >= 1.0    -> "%.4f".format(balance).trimEnd('0').trimEnd('.')
        balance >= 0.0001 -> "%.6f".format(balance).trimEnd('0').trimEnd('.')
        else              -> "%.8f".format(balance).trimEnd('0').trimEnd('.')
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
        Text(label, color = if (primary) Color.White else AssetInk, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FiatRow(symbol: String, name: String, amount: String, usdValue: String, color: Color, label: String, line: Color, ink: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { }.padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
            Text(label, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
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
    val label = when (account.type) { "savings" -> "Ep"; "business" -> "Biz"; else -> "Ar" }
    FiatRow(
        symbol = account.currency,
        name = "${account.type.replaceFirstChar { it.uppercase() }} · ****${account.accountNumber.takeLast(4)}",
        amount = if (visible) account.formattedBalance else "••••",
        usdValue = if (visible) "≈ ${"%.2f".format(account.balance / 4500.0)} USD" else "••••",
        color = Color(0xFFD92C55), label = label, line = line, ink = ink
    )
}

@Composable
private fun CardRow(card: com.stephan.mobil.data.model.Card, vm: BankViewModel, line: Color, ink: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1A1A2E), Color(0xFFD92C55)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CreditCard, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(card.cardNumberMasked, color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Expire ${card.expiryDate} · Limite ${"%,.0f".format(card.dailyLimit).replace(",", " ")} MGA",
                color = AssetMuted, fontSize = 12.sp
            )
        }
        Switch(
            checked = !card.isBlocked,
            onCheckedChange = { vm.toggleCard(card.id) },
            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFD92C55), uncheckedTrackColor = AssetSoft),
            modifier = Modifier.size(width = 44.dp, height = 24.dp)
        )
    }
    Divider(modifier = Modifier.padding(horizontal = 20.dp), color = line, thickness = 0.5.dp)
}
