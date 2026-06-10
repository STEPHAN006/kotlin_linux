package com.stephan.mobil.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.stephan.mobil.data.model.CoinMarketData
import com.stephan.mobil.data.model.CryptoWallet
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel

private val DetailBg    = Color(0xFF0D0E12)
private val DetailCard  = Color(0xFF1A1C23)
private val DetailMuted = Color(0xFF8B8F98)
private val DetailLine  = Color(0xFF2A2D35)
private val Green       = Color(0xFF10B981)
private val Red         = Color(0xFFEF4444)
private val Accent      = Color(0xFFD92C55)

private val COIN_COLORS = mapOf(
    "bitcoin"     to Color(0xFFF7931A),
    "ethereum"    to Color(0xFF627EEA),
    "solana"      to Color(0xFF9945FF),
    "binancecoin" to Color(0xFFF3BA2F),
    "tether"      to Color(0xFF26A17B),
    "usd-coin"    to Color(0xFF2775CA),
    "toncoin"     to Color(0xFF0088CC),
    "sonic-3"     to Color(0xFF1A1A1A),
)

private val CHART_RANGES = listOf("1H" to "1", "1J" to "1", "1S" to "7", "1M" to "30", "3M" to "90")

@Composable
fun CryptoDetailSheet(
    coin: CoinMarketData,
    wallet: CryptoWallet?,
    state: BankUiState,
    vm: BankViewModel,
    onDismiss: () -> Unit
) {
    var activeAction by remember { mutableStateOf<String?>(null) }
    var chartRangeIdx by remember { mutableIntStateOf(1) }

    LaunchedEffect(coin.id, chartRangeIdx) {
        vm.loadCryptoChart(coin.id, CHART_RANGES[chartRangeIdx].second)
    }

    val change = coin.change24h ?: 0.0
    val isUp = change >= 0
    val changeColor = if (isUp) Green else Red
    val coinColor = COIN_COLORS[coin.id] ?: DetailMuted
    val balance = wallet?.balance ?: 0.0
    val address = wallet?.address ?: ""
    val mga = state.mgaPerUsd

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DetailBg)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(coin.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "%+.2f%% (24h)".format(change),
                    color = changeColor, fontSize = 13.sp
                )
            }
            // Coin icon
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(coinColor),
                contentAlignment = Alignment.Center
            ) {
                if (coin.image.isNotBlank()) {
                    AsyncImage(
                        model = coin.image, contentDescription = coin.name,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Price
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Text(
                "${"%.2f".format(coin.currentPrice)} USD",
                color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold
            )
            Text(
                "≈ %,.0f MGA".format(coin.currentPrice * mga).replace(",", " "),
                color = DetailMuted, fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // Chart
        val chartPoints = state.cryptoChart
        if (chartPoints.size >= 4) {
            PriceChart(
                points = chartPoints,
                isUp = isUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 12.dp)
            )
        } else {
            Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent, modifier = Modifier.size(24.dp))
            }
        }

        // Chart range tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CHART_RANGES.forEachIndexed { i, (label, _) ->
                val selected = i == chartRangeIdx
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selected) DetailCard else Color.Transparent)
                        .clickable { chartRangeIdx = i }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = if (selected) Color.White else DetailMuted, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Wallet balance card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DetailCard)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Solde ${coin.symbol.uppercase()}", color = DetailMuted, fontSize = 12.sp)
                Text(formatCryptoQty(balance), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "≈ ${"%.2f".format(balance * coin.currentPrice)} USD",
                    color = DetailMuted, fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Adresse", color = DetailMuted, fontSize = 11.sp)
                Text(
                    address.take(8) + "…" + address.takeLast(4),
                    color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionButton("Acheter", Icons.Default.ShoppingCart, Accent, Modifier.weight(1f)) { activeAction = "buy" }
            ActionButton("Vendre", Icons.Default.Sell, Color(0xFF1A7F37), Modifier.weight(1f)) { activeAction = "sell" }
            ActionButton("Envoyer", Icons.Default.Send, Color(0xFF2775CA), Modifier.weight(1f)) { activeAction = "send" }
            ActionButton("Recevoir", Icons.Default.QrCode, Color(0xFF9945FF), Modifier.weight(1f)) { activeAction = "receive" }
        }

        // Dismiss messages
        state.message?.let { msg ->
            Spacer(Modifier.height(12.dp))
            Text(
                msg, color = Green, fontSize = 13.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        }
        state.error?.let { err ->
            Spacer(Modifier.height(12.dp))
            Text(
                err, color = Red, fontSize = 13.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        }
    }

    when (activeAction) {
        "buy"     -> BuyModal(coin, state, vm) { activeAction = null }
        "sell"    -> SellModal(coin, wallet, state, vm) { activeAction = null }
        "send"    -> SendModal(coin, wallet, state, vm) { activeAction = null }
        "receive" -> ReceiveModal(coin, wallet) { activeAction = null }
    }
}

@Composable
private fun PriceChart(points: List<Pair<Long, Double>>, isUp: Boolean, modifier: Modifier) {
    val lineColor = if (isUp) Green else Red
    val fillColor = if (isUp) Green.copy(alpha = 0.15f) else Red.copy(alpha = 0.15f)

    Canvas(modifier = modifier) {
        val prices = points.map { it.second }
        if (prices.size < 2) return@Canvas
        val min = prices.min()
        val max = prices.max()
        val range = (max - min).coerceAtLeast(0.0001)
        val w = size.width
        val h = size.height
        val path = Path()
        val fillPath = Path()
        prices.forEachIndexed { i, price ->
            val x = i / (prices.size - 1).toFloat() * w
            val y = h - ((price - min) / range).toFloat() * h * 0.9f
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(w, h)
        fillPath.close()
        drawPath(fillPath, color = fillColor)
        drawPath(path, color = lineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun ActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Buy Modal ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuyModal(coin: CoinMarketData, state: BankUiState, vm: BankViewModel, onClose: () -> Unit) {
    var amountMga by remember { mutableStateOf("") }
    val price = coin.currentPrice
    val mga = state.mgaPerUsd
    val cryptoQty = if (amountMga.toDoubleOrNull() != null && amountMga.toDouble() > 0)
        amountMga.toDouble() / (price * mga) else 0.0
    val totalBalance = state.balance.totalBalance

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = DetailCard,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DetailMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Acheter ${coin.symbol.uppercase()}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Prix : ${"%.2f".format(price)} USD · Solde : %,.0f MGA".format(totalBalance).replace(",", " "), color = DetailMuted, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = amountMga,
                onValueChange = { amountMga = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Montant en MGA", color = DetailMuted) },
                suffix = { Text("MGA", color = DetailMuted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Accent, unfocusedBorderColor = DetailLine
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (cryptoQty > 0) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(DetailBg).padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Vous recevrez", color = DetailMuted, fontSize = 13.sp)
                    Text(formatCryptoQty(cryptoQty) + " ${coin.symbol.uppercase()}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    amountMga.toDoubleOrNull()?.let { vm.buyCrypto(coin.symbol.uppercase(), it, price) }
                    onClose()
                },
                enabled = (amountMga.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Confirmer l'achat", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Sell Modal ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellModal(coin: CoinMarketData, wallet: CryptoWallet?, state: BankUiState, vm: BankViewModel, onClose: () -> Unit) {
    var amountCrypto by remember { mutableStateOf("") }
    val price = coin.currentPrice
    val mga = state.mgaPerUsd
    val balance = wallet?.balance ?: 0.0
    val totalMga = if (amountCrypto.toDoubleOrNull() != null) amountCrypto.toDouble() * price * mga else 0.0

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = DetailCard,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DetailMuted) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp).verticalScroll(rememberScrollState())
        ) {
            Text("Vendre ${coin.symbol.uppercase()}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Disponible : ${formatCryptoQty(balance)} ${coin.symbol.uppercase()}", color = DetailMuted, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = amountCrypto,
                onValueChange = { amountCrypto = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Quantité ${coin.symbol.uppercase()}", color = DetailMuted) },
                suffix = { Text(coin.symbol.uppercase(), color = DetailMuted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Green, unfocusedBorderColor = DetailLine
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { amountCrypto = formatCryptoQty(balance) }) {
                    Text("Max", color = Green, fontSize = 12.sp)
                }
            }

            if (totalMga > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(DetailBg).padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Vous recevrez", color = DetailMuted, fontSize = 13.sp)
                    Text("%,.0f MGA".format(totalMga).replace(",", " "), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    amountCrypto.toDoubleOrNull()?.let { vm.sellCrypto(coin.symbol.uppercase(), it, price) }
                    onClose()
                },
                enabled = (amountCrypto.toDoubleOrNull() ?: 0.0) > 0 && (amountCrypto.toDoubleOrNull() ?: 0.0) <= balance,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A7F37)),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Confirmer la vente", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Send Modal ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SendModal(coin: CoinMarketData, wallet: CryptoWallet?, state: BankUiState, vm: BankViewModel, onClose: () -> Unit) {
    var amountCrypto by remember { mutableStateOf("") }
    var toAddress by remember { mutableStateOf("") }
    val price = coin.currentPrice
    val balance = wallet?.balance ?: 0.0

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = DetailCard,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DetailMuted) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp).verticalScroll(rememberScrollState())
        ) {
            Text("Envoyer ${coin.symbol.uppercase()}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Disponible : ${formatCryptoQty(balance)} ${coin.symbol.uppercase()}", color = DetailMuted, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = toAddress,
                onValueChange = { toAddress = it },
                label = { Text("Adresse de destination", color = DetailMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF2775CA), unfocusedBorderColor = DetailLine
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = amountCrypto,
                onValueChange = { amountCrypto = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Quantité ${coin.symbol.uppercase()}", color = DetailMuted) },
                suffix = { Text(coin.symbol.uppercase(), color = DetailMuted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF2775CA), unfocusedBorderColor = DetailLine
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if ((amountCrypto.toDoubleOrNull() ?: 0.0) > 0) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(DetailBg).padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Valeur estimée", color = DetailMuted, fontSize = 13.sp)
                    Text("${"%.2f".format((amountCrypto.toDoubleOrNull() ?: 0.0) * price)} USD", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val qty = amountCrypto.toDoubleOrNull() ?: 0.0
                    if (qty > 0 && toAddress.isNotBlank()) {
                        vm.sendCrypto(coin.symbol.uppercase(), qty, toAddress, price)
                    }
                    onClose()
                },
                enabled = (amountCrypto.toDoubleOrNull() ?: 0.0) > 0
                        && (amountCrypto.toDoubleOrNull() ?: 0.0) <= balance
                        && toAddress.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2775CA)),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Confirmer l'envoi", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Receive Modal ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiveModal(coin: CoinMarketData, wallet: CryptoWallet?, onClose: () -> Unit) {
    val address = wallet?.address ?: "Adresse non disponible"
    val qrBitmap = remember(address) { generateQrBitmap(address, 400) }

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = DetailCard,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DetailMuted) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Recevoir ${coin.symbol.uppercase()}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Scannez ce QR code pour envoyer ${coin.symbol.uppercase()} à cette adresse", color = DetailMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))

            if (qrBitmap != null) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DetailBg)
                    .padding(14.dp)
            ) {
                Text(address, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(8.dp))
            Text("Réseau : ${coin.name}", color = DetailMuted, fontSize = 12.sp)
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun generateQrBitmap(payload: String, size: Int): Bitmap? = runCatching {
    val hints = mapOf(
        EncodeHintType.MARGIN to 1,
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
    )
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size, hints)
    Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
        for (x in 0 until size) {
            for (y in 0 until size) {
                setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
    }
}.getOrNull()

private fun formatCryptoQty(qty: Double): String {
    if (qty == 0.0) return "0"
    return when {
        qty >= 1.0    -> "%.4f".format(qty).trimEnd('0').trimEnd('.')
        qty >= 0.0001 -> "%.6f".format(qty).trimEnd('0').trimEnd('.')
        else          -> "%.8f".format(qty).trimEnd('0').trimEnd('.')
    }
}
