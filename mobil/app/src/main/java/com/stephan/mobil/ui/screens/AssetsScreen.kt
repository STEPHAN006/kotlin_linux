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
import com.stephan.mobil.data.model.CryptoTxn
import com.stephan.mobil.data.model.CryptoWallet
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel
import com.stephan.mobil.ui.viewmodel.CryptoUiState
import com.stephan.mobil.ui.viewmodel.CryptoViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.stephan.mobil.ui.theme.*

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    state: BankUiState,
    cryptoState: CryptoUiState,
    vm: BankViewModel,
    cryptoVm: CryptoViewModel
) {
    val darkMode = LocalDarkMode.current
    var selectedTab by remember { mutableStateOf(0) }
    var balanceVisible by remember { mutableStateOf(true) }
    var selectedCoin by remember { mutableStateOf<CoinMarketData?>(null) }
    var selectedCurrency by remember { mutableStateOf("MGA") }

    LaunchedEffect(Unit) {
        cryptoVm.loadCrypto()
        cryptoVm.loadCryptoTransactions()
    }

    val totalMga = state.balance.totalBalance
    val totalUsd = totalMga / cryptoState.mgaPerUsd
    val cryptoTotalUsd = cryptoState.cryptoWallets.sumOf { wallet ->
        val price = cryptoState.cryptoMarkets.find { it.id == wallet.coinId }?.currentPrice ?: 0.0
        wallet.balance * price
    }
    val cryptoTotalMga = cryptoTotalUsd * cryptoState.mgaPerUsd
    val cryptoTotalEur = cryptoTotalMga / cryptoState.mgaPerEur
    val totalEur = totalMga / cryptoState.mgaPerEur

    val bg = if (darkMode) Color(0xFF101114) else White
    val ink = if (darkMode) White else AssetInk
    val line = if (darkMode) Color(0xFF2A2D3A) else AssetLine

    if (selectedCoin != null) {
        val wallet = cryptoState.cryptoWallets.find { it.coinId == selectedCoin!!.id }
        CryptoDetailSheet(
            coin = selectedCoin!!,
            wallet = wallet,
            cryptoState = cryptoState,
            bankState = state,
            cryptoVm = cryptoVm,
            vm = vm,
            onDismiss = { selectedCoin = null }
        )
        return
    }

    PullToRefreshBox(
        isRefreshing = cryptoState.cryptoLoading,
        onRefresh = { cryptoVm.loadCrypto() },
        modifier = Modifier.fillMaxSize()
    ) {
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
                    Spacer(Modifier.weight(1f))
                    AssetCurrencyToggle(
                        selected = selectedCurrency,
                        onSelect = { selectedCurrency = it }
                    )
                }
                Spacer(Modifier.height(10.dp))
                val displayAmount = if (balanceVisible) {
                    val raw = if (selectedTab == 0) {
                        if (selectedCurrency == "MGA") cryptoTotalMga else cryptoTotalEur
                    } else {
                        if (selectedCurrency == "MGA") totalMga else totalEur
                    }
                    if (selectedCurrency == "MGA")
                        "%,.0f".format(raw).replace(",", " ")
                    else
                        "%.2f".format(raw)
                } else "••••"
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = displayAmount,
                        color = ink, fontSize = 44.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = (-1).sp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        selectedCurrency,
                        color = ink, fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (balanceVisible && (selectedTab == 0 && cryptoState.cryptoMarkets.isNotEmpty() || selectedTab == 1)) {
                    val sub = if (selectedCurrency == "MGA") {
                        val eur = if (selectedTab == 0) cryptoTotalEur else totalEur
                        "≈ %.2f EUR".format(eur)
                    } else {
                        val mga = if (selectedTab == 0) cryptoTotalMga else totalMga
                        "≈ %,.0f MGA".format(mga).replace(",", " ")
                    }
                    Text(sub, color = AssetMuted, fontSize = 14.sp)
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
                    cryptoState.cryptoMarkets.firstOrNull()?.let { selectedCoin = it }
                }
                AssetActionBtn("Vendre", false, Modifier.weight(1f)) {
                    cryptoState.cryptoMarkets.firstOrNull()?.let { selectedCoin = it }
                }
                AssetActionBtn("Envoyer", false, Modifier.weight(1f)) {
                    cryptoState.cryptoMarkets.firstOrNull()?.let { selectedCoin = it }
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
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                            .clickable { selectedTab = index },
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
                                .background(if (selectedTab == index) ink else Color.Transparent)
                        )
                    }
                }
            }
            Divider(color = line, thickness = 1.dp)
        }

        if (selectedTab == 0) {
            if (cryptoState.cryptoLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandPrimary)
                    }
                }
            } else if (cryptoState.cryptoMarkets.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Impossible de charger les prix", color = AssetMuted, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = { cryptoVm.loadCrypto() }) {
                                Text("Réessayer", color = BrandPrimary)
                            }
                        }
                    }
                }
            } else {
                items(cryptoState.cryptoMarkets) { market ->
                    val wallet = cryptoState.cryptoWallets.find { it.coinId == market.id }
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
                        BgSurfaceHigh, "Ar", line, ink)
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
                        "+ Nouvelle", color = BrandPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold,
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

            // Crypto transaction history
            if (cryptoState.cryptoTxns.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Historique crypto", color = AssetMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Divider(color = line)
                }
                items(cryptoState.cryptoTxns) { txn ->
                    CryptoTxnRow(txn, line, ink)
                }
            }
        }
    }
    } // PullToRefreshBox
}

@Composable
private fun CryptoTxnRow(txn: CryptoTxn, line: Color, ink: Color) {
    val (icon, color, label) = when (txn.type) {
        "buy"  -> Triple(Icons.Default.ShoppingCart, Color(0xFF10B981), "Achat")
        "sell" -> Triple(Icons.Default.Sell, SemanticDanger, "Vente")
        else   -> Triple(Icons.Default.Send, Color(0xFF2775CA), "Envoi")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("$label ${txn.symbol}", color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(txn.createdAt?.take(10) ?: "", color = AssetMuted, fontSize = 12.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${if (txn.type == "buy") "-" else "+"}%,.0f MGA".format(txn.totalMga).replace(",", " "),
                color = if (txn.type == "buy") SemanticDanger else Color(0xFF10B981),
                fontSize = 14.sp, fontWeight = FontWeight.Bold
            )
            Text("%.6f ${txn.symbol}".trimEnd('0').trimEnd('.'), color = AssetMuted, fontSize = 11.sp)
        }
    }
    Divider(color = line, modifier = Modifier.padding(horizontal = 20.dp))
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
        color = BgSurfaceHigh, label = label, line = line, ink = ink
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
                .background(Brush.linearGradient(listOf(Color(0xFF1A1A2E), Color(0xFF2A2A2A)))),
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
            colors = SwitchDefaults.colors(checkedTrackColor = BrandPrimary, uncheckedTrackColor = AssetSoft),
            modifier = Modifier.size(width = 44.dp, height = 24.dp)
        )
    }
    Divider(modifier = Modifier.padding(horizontal = 20.dp), color = line, thickness = 0.5.dp)
}

@Composable
private fun AssetCurrencyToggle(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .border(1.dp, AssetMuted.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
    ) {
        listOf("MGA", "EUR").forEach { cur ->
            val isSelected = cur == selected
            Box(
                modifier = Modifier
                    .background(if (isSelected) AssetMuted.copy(alpha = 0.15f) else Color.Transparent)
                    .clickable { onSelect(cur) }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    cur,
                    fontSize = 12.sp,
                    color = if (isSelected) AssetInk else AssetMuted,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
