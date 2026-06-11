package com.stephan.mobil.ui.screens

import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.stephan.mobil.data.model.CoinMarketData
import com.stephan.mobil.data.model.CryptoWallet
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel
import com.stephan.mobil.ui.viewmodel.CryptoUiState
import com.stephan.mobil.ui.viewmodel.CryptoViewModel
import com.stephan.mobil.ui.theme.*

private val DetailBg    = Color(0xFF0D0E12)
private val DetailCard  = Color(0xFF1A1C23)
private val DetailMuted = Color(0xFF8B8F98)
private val DetailLine  = Color(0xFF2A2D35)
private val Green       = Color(0xFF10B981)
private val Red         = Color(0xFFEF4444)
private val Accent      = BrandPrimary

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
    cryptoState: CryptoUiState,
    bankState: BankUiState,
    cryptoVm: CryptoViewModel,
    vm: BankViewModel,
    onDismiss: () -> Unit
) {
    val darkMode = LocalDarkMode.current
    var activeAction by remember { mutableStateOf<String?>(null) }
    var chartRangeIdx by remember { mutableIntStateOf(1) }
    var chartType by remember { mutableStateOf("line") }

    LaunchedEffect(coin.id, chartRangeIdx, chartType) {
        if (chartType == "line") {
            cryptoVm.loadCryptoChart(coin.id, CHART_RANGES[chartRangeIdx].second)
        } else {
            cryptoVm.loadCryptoCandles(coin.id, CHART_RANGES[chartRangeIdx].second)
        }
    }

    val change    = coin.change24h ?: 0.0
    val isUp      = change >= 0
    val changeColor = if (isUp) Green else Red
    val coinColor = COIN_COLORS[coin.id] ?: DetailMuted
    val balance   = wallet?.balance ?: 0.0
    val address   = wallet?.address ?: ""
    val mga       = cryptoState.mgaPerUsd

    val bg         = if (darkMode) Color(0xFF0D0E12) else Color(0xFFF8F9FB)
    val hdrBg      = if (darkMode) Color(0xFF0D0E12) else Color.White
    val cardBg     = if (darkMode) Color(0xFF1A1C23) else Color.White
    val cardBorder = if (darkMode) Color.Transparent else Color(0xFFE8EAF0)
    val ink        = if (darkMode) Color.White else Color(0xFF0F172A)
    val muted      = if (darkMode) Color(0xFF8B8F98) else Color(0xFF64748B)
    val divLine    = if (darkMode) Color(0xFF2A2D35) else Color(0xFFE5E7EB)
    val toggleBg   = if (darkMode) Color(0xFF252830) else Color(0xFFE8EAED)
    val tabActiveBg = if (darkMode) Color(0xFF2A2D35) else Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
    ) {
        // ── Header ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(hdrBg)
                .padding(horizontal = 4.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ink)
            }
            Column(Modifier.weight(1f)) {
                Text(coin.name, color = ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "%+.2f%% (24h)".format(change),
                    color = changeColor, fontSize = 12.sp, fontWeight = FontWeight.Medium
                )
            }
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
            Spacer(Modifier.width(8.dp))
        }
        Divider(color = divLine)

        // ── Scrollable body ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Price
            Text(
                "${"%.2f".format(coin.currentPrice)} USD",
                color = ink, fontSize = 36.sp,
                fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp
            )
            Text(
                "≈ %,.0f MGA".format(coin.currentPrice * mga).replace(",", " "),
                color = muted, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Chart type toggle – pill style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(toggleBg)
                    .padding(4.dp)
            ) {
                ChartTypeTab("Ligne",   "line",   chartType, tabActiveBg, ink, muted, Modifier.weight(1f)) { chartType = it }
                ChartTypeTab("Bougies", "candle", chartType, tabActiveBg, ink, muted, Modifier.weight(1f)) { chartType = it }
            }

            Spacer(Modifier.height(16.dp))

            // Chart
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                if (chartType == "line") {
                    val pts = cryptoState.cryptoChart
                    if (pts.size >= 4) {
                        SmoothLineChart(pts, coinColor, Modifier.fillMaxSize())
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = coinColor, modifier = Modifier.size(24.dp))
                        }
                    }
                } else {
                    val candles = cryptoState.cryptoCandleData
                    if (candles.size >= 2) {
                        CandleStickChart(candles, Modifier.fillMaxSize())
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = coinColor, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Time range – circular buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CHART_RANGES.forEachIndexed { i, (label, _) ->
                    val selected = i == chartRangeIdx
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (selected) coinColor.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { chartRangeIdx = i },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (selected) coinColor else muted,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Balance card – white card with soft border
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "SOLDE ${coin.symbol.uppercase()}",
                        color = muted, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(formatCryptoQty(balance), color = ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "≈ ${"%.2f".format(balance * coin.currentPrice)} USD",
                        color = muted, fontSize = 13.sp, fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "ADRESSE",
                        color = muted.copy(alpha = 0.7f), fontSize = 9.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (address.length > 10) address.take(6) + "…" + address.takeLast(4)
                        else address.ifEmpty { "—" },
                        color = muted, fontSize = 11.sp,
                        modifier = Modifier
                            .background(
                                if (darkMode) Color(0xFF2A2D35) else Color(0xFFF3F4F6),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Quick-action grid – 5 columns with pastel icon squares
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton("Acheter",  Icons.Default.ShoppingCart, DetailCard, Color(0xFFE11D48), Modifier.weight(1f)) { activeAction = "buy" }
                ActionButton("Vendre",   Icons.Default.Sell,         DetailCard, SemanticDanger, Modifier.weight(1f)) { activeAction = "sell" }
                ActionButton("Swap",     Icons.Default.SwapHoriz,    DetailCard, BrandPrimary, Modifier.weight(1f)) { activeAction = "swap" }
                ActionButton("Envoyer",  Icons.Default.Send,         DetailCard, Color(0xFF2563EB), Modifier.weight(1f)) { activeAction = "send" }
                ActionButton("Recevoir", Icons.Default.QrCode,       DetailCard, BrandPrimary,      Modifier.weight(1f)) { activeAction = "receive" }
            }

            // Feedback messages
            cryptoState.message?.let { msg ->
                Spacer(Modifier.height(16.dp))
                Text(msg, color = Green, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            cryptoState.error?.let { err ->
                Spacer(Modifier.height(16.dp))
                Text(err, color = Red, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    when (activeAction) {
        "buy"     -> BuyModal(coin, cryptoState, bankState, cryptoVm) { activeAction = null }
        "sell"    -> SellModal(coin, wallet, cryptoState, bankState, cryptoVm) { activeAction = null }
        "swap"    -> SwapModal(coin, cryptoState, cryptoVm) { activeAction = null }
        "send"    -> SendModal(coin, wallet, cryptoState, cryptoVm) { activeAction = null }
        "receive" -> ReceiveModal(coin, wallet) { activeAction = null }
    }
}

@Composable
private fun ChartTypeTab(
    label: String, type: String, currentType: String,
    activeBg: Color, ink: Color, muted: Color,
    modifier: Modifier, onClick: (String) -> Unit
) {
    val selected = type == currentType
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) activeBg else Color.Transparent)
            .clickable { onClick(type) }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) ink else muted,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun SmoothLineChart(
    points: List<Pair<Long, Double>>,
    coinColor: Color,
    modifier: Modifier
) {
    val sampled = remember(points) {
        if (points.size > 120) {
            val step = points.size / 90
            points.filterIndexed { i, _ -> i % step == 0 }
        } else points
    }

    var scale by remember(points) { mutableFloatStateOf(1f) }
    var translateX by remember(points) { mutableFloatStateOf(0f) }

    Canvas(
        modifier = modifier.pointerInput(sampled) {
            coroutineScope {
                launch {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val w = size.width.toFloat()
                        val newScale = (scale * zoom).coerceIn(1f, 8f)
                        // Zoom toward centroid
                        val virtualX = (centroid.x - translateX) / scale
                        var newTx = centroid.x - virtualX * newScale + pan.x
                        // Clamp: data must fill the canvas width
                        newTx = newTx.coerceIn(w * (1f - newScale), 0f)
                        scale = newScale
                        translateX = newTx
                    }
                }
                launch {
                    detectTapGestures(onDoubleTap = {
                        scale = 1f
                        translateX = 0f
                    })
                }
            }
        }
    ) {
        val vals = sampled.map { it.second }
        if (vals.size < 2) return@Canvas
        val minV = vals.min()
        val maxV = vals.max()
        val range = (maxV - minV).coerceAtLeast(0.0001)
        val w = size.width
        val h = size.height
        val padV = h * 0.08f

        // Clamp translateX inside drawing scope too (covers initial render)
        val tx = translateX.coerceIn(w * (1f - scale), 0f)

        fun xOf(i: Int) = i / (vals.size - 1).toFloat() * w * scale + tx
        fun yOf(v: Double) = h - padV - ((v - minV) / range).toFloat() * (h - 2 * padV)

        // Dashed grid lines
        val gridDash = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
        repeat(3) { row ->
            val y = padV + (h - 2 * padV) * row / 2f
            drawLine(
                color = coinColor.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f,
                pathEffect = gridDash
            )
        }

        // Only iterate visible range for performance
        val startIdx = (((-tx) / (w * scale)) * (vals.size - 1)).toInt()
            .minus(2).coerceIn(0, vals.size - 2)
        val endIdx = (((w - tx) / (w * scale)) * (vals.size - 1)).toInt()
            .plus(2).coerceIn(1, vals.size - 1)

        // Smooth cubic bezier path
        val linePath = Path()
        val fillPath = Path()
        for (i in startIdx..endIdx) {
            val x = xOf(i)
            val y = yOf(vals[i])
            if (i == startIdx) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                val px = xOf(i - 1)
                val py = yOf(vals[i - 1])
                val cx1 = px + (x - px) / 3f
                val cx2 = x - (x - px) / 3f
                linePath.cubicTo(cx1, py, cx2, y, x, y)
                fillPath.cubicTo(cx1, py, cx2, y, x, y)
            }
        }
        val lastX = xOf(endIdx)
        fillPath.lineTo(lastX, h)
        fillPath.close()

        // Gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(coinColor.copy(alpha = 0.28f), coinColor.copy(alpha = 0.0f)),
                startY = 0f, endY = h
            )
        )

        // Line
        drawPath(linePath, color = coinColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

        // Glowing dot at last visible point
        val lx = xOf(endIdx)
        val ly = yOf(vals[endIdx])
        drawCircle(color = coinColor.copy(alpha = 0.22f), radius = 9f, center = Offset(lx, ly))
        drawCircle(color = coinColor, radius = 4f, center = Offset(lx, ly))
    }
}

@Composable
private fun CandleStickChart(
    candles: List<List<Double>>,
    modifier: Modifier
) {
    val display = remember(candles) {
        if (candles.size > 70) {
            val step = candles.size / 55
            candles.filterIndexed { i, _ -> i % step == 0 }
        } else candles
    }

    var scale by remember(candles) { mutableFloatStateOf(1f) }
    var translateX by remember(candles) { mutableFloatStateOf(0f) }

    Canvas(
        modifier = modifier.pointerInput(display) {
            coroutineScope {
                launch {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val w = size.width.toFloat()
                        val newScale = (scale * zoom).coerceIn(1f, 8f)
                        val virtualX = (centroid.x - translateX) / scale
                        var newTx = centroid.x - virtualX * newScale + pan.x
                        newTx = newTx.coerceIn(w * (1f - newScale), 0f)
                        scale = newScale
                        translateX = newTx
                    }
                }
                launch {
                    detectTapGestures(onDoubleTap = {
                        scale = 1f
                        translateX = 0f
                    })
                }
            }
        }
    ) {
        if (display.size < 2) return@Canvas
        val minPrice = display.minOf { it[3] }
        val maxPrice = display.maxOf { it[2] }
        val range = (maxPrice - minPrice).coerceAtLeast(0.0001)
        val w = size.width
        val h = size.height
        val padV = h * 0.06f

        val tx = translateX.coerceIn(w * (1f - scale), 0f)

        fun yOf(v: Double) = h - padV - ((v - minPrice) / range).toFloat() * (h - 2 * padV)

        val n = display.size
        // Slot width scales with zoom
        val slotW = w * scale / n
        val bodyW = (slotW * 0.55f).coerceAtLeast(2f)

        // Grid
        repeat(4) { row ->
            val y = padV + (h - 2 * padV) * row / 3f
            drawLine(
                color = Color.White.copy(alpha = 0.06f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 0.8f
            )
        }

        // Only draw visible candles
        val startIdx = ((-tx / (w * scale)) * n).toInt().minus(1).coerceIn(0, n - 1)
        val endIdx = (((w - tx) / (w * scale)) * n).toInt().plus(1).coerceIn(0, n - 1)

        for (i in startIdx..endIdx) {
            val c = display[i]
            val open = c[1]; val high = c[2]; val low = c[3]; val close = c[4]
            val isGreen = close >= open
            val color = if (isGreen) Color(0xFF10B981) else Color(0xFFEF4444)
            val cx = slotW * i + slotW / 2f + tx

            // Wick
            drawLine(
                color = color.copy(alpha = 0.7f),
                start = Offset(cx, yOf(high)),
                end   = Offset(cx, yOf(low)),
                strokeWidth = 1.2f
            )

            // Body
            val top    = yOf(maxOf(open, close))
            val bottom = yOf(minOf(open, close))
            val bodyH  = (bottom - top).coerceAtLeast(1.5f)
            drawRect(
                color = color,
                topLeft = Offset(cx - bodyW / 2f, top),
                size = Size(bodyW, bodyH)
            )
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    iconColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label.uppercase(),
            color = iconColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

// ── Swap Modal ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwapModal(
    currentCoin: CoinMarketData,
    cryptoState: CryptoUiState,
    cryptoVm: CryptoViewModel,
    onClose: () -> Unit
) {
    val darkMode = LocalDarkMode.current
    var fromSymbol   by remember { mutableStateOf(currentCoin.symbol.uppercase()) }
    var toSymbol     by remember { mutableStateOf("") }
    var fromAmount   by remember { mutableStateOf("") }
    var showPicker   by remember { mutableStateOf(false) }

    val cardBg      = if (darkMode) Color(0xFF1A1C23) else Color(0xFFF4F5F7)
    val ink         = if (darkMode) Color.White else Color(0xFF17181C)
    val line        = if (darkMode) Color(0xFF2A2D35) else Color(0xFFE5E7EB)
    val secondary   = if (darkMode) Color(0xFF0D0E12) else Color(0xFFECEEF2)

    val fromCoin  = cryptoState.cryptoMarkets.find { it.symbol.uppercase() == fromSymbol }
    val toCoin    = cryptoState.cryptoMarkets.find { it.symbol.uppercase() == toSymbol }
    val fromWallet = cryptoState.cryptoWallets.find { it.symbol == fromSymbol }
    val fromBal   = fromWallet?.balance ?: 0.0
    val fromAmt   = fromAmount.toDoubleOrNull() ?: 0.0
    val toAmt     = if (fromCoin != null && toCoin != null && fromAmt > 0)
        fromAmt * fromCoin.currentPrice / toCoin.currentPrice else 0.0

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = cardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DetailMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Swap", color = ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text("Échangez une crypto contre une autre", color = DetailMuted, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            // FROM row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(secondary)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (fromCoin?.image?.isNotBlank() == true) {
                    AsyncImage(model = fromCoin.image, contentDescription = fromSymbol,
                        modifier = Modifier.size(30.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(8.dp))
                }
                Text(fromSymbol, color = ink, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                OutlinedTextField(
                    value = fromAmount,
                    onValueChange = { v ->
                        val f = v.filter { it.isDigit() || it == '.' }
                        if (f.count { it == '.' } <= 1) fromAmount = f
                    },
                    placeholder = { Text("0.00", color = DetailMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ink, unfocusedTextColor = ink,
                        focusedBorderColor = BrandPrimary, unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier.width(130.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Disponible: ${formatCryptoQty(fromBal)} $fromSymbol",
                    color = DetailMuted, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                TextButton(onClick = { fromAmount = formatCryptoQty(fromBal) }) {
                    Text("Max", color = BrandPrimary, fontSize = 12.sp)
                }
            }

            // Swap arrow
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(BrandPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SwapVert, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            // TO row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(secondary)
                    .clickable { showPicker = true }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (toCoin?.image?.isNotBlank() == true) {
                    AsyncImage(model = toCoin.image, contentDescription = toSymbol,
                        modifier = Modifier.size(30.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.Add, null, tint = DetailMuted, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                }
                if (toSymbol.isEmpty()) {
                    Text("Choisir la crypto cible", color = DetailMuted, fontSize = 14.sp, modifier = Modifier.weight(1f))
                } else {
                    Text(toSymbol, color = ink, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                }
                if (toAmt > 0) {
                    Text(formatCryptoQty(toAmt), color = Color(0xFF10B981), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = DetailMuted)
                }
            }

            // Rate info
            if (toAmt > 0 && fromCoin != null && toCoin != null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .background(secondary).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Taux", color = DetailMuted, fontSize = 13.sp)
                    Text(
                        "1 $fromSymbol = ${formatCryptoQty(fromCoin.currentPrice / toCoin.currentPrice)} $toSymbol",
                        color = ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (fromAmt > 0 && toSymbol.isNotEmpty() && fromCoin != null && toCoin != null) {
                        cryptoVm.swapCrypto(fromSymbol, fromAmt, fromCoin.currentPrice, toSymbol, toCoin.currentPrice)
                        onClose()
                    }
                },
                enabled = fromAmt > 0 && fromAmt <= fromBal && toSymbol.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Confirmer le swap", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Coin picker sheet
    if (showPicker) {
        ModalBottomSheet(
            onDismissRequest = { showPicker = false },
            containerColor = cardBg,
            dragHandle = { BottomSheetDefaults.DragHandle(color = DetailMuted) }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text("Choisir la crypto cible", color = ink, fontSize = 16.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    items(cryptoState.cryptoMarkets.filter { it.symbol.uppercase() != fromSymbol }) { c ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { toSymbol = c.symbol.uppercase(); showPicker = false }
                                .padding(horizontal = 4.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (c.image.isNotBlank()) {
                                AsyncImage(model = c.image, contentDescription = c.name,
                                    modifier = Modifier.size(36.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(c.symbol.uppercase(), color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(c.name, color = DetailMuted, fontSize = 12.sp)
                            }
                            Text("${"%.4f".format(c.currentPrice).trimEnd('0').trimEnd('.')} USD",
                                color = DetailMuted, fontSize = 12.sp)
                        }
                        Divider(color = line, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ── Buy Modal ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuyModal(coin: CoinMarketData, cryptoState: CryptoUiState, bankState: BankUiState, cryptoVm: CryptoViewModel, onClose: () -> Unit) {
    val darkMode = LocalDarkMode.current
    var amountMga by remember { mutableStateOf("") }
    val price = coin.currentPrice
    val mga = cryptoState.mgaPerUsd
    val cryptoQty = if (amountMga.toDoubleOrNull() != null && amountMga.toDouble() > 0)
        amountMga.toDouble() / (price * mga) else 0.0
    val totalBalance = bankState.balance.totalBalance

    val bg = if (darkMode) Color(0xFF0D0E12) else Color.White
    val cardBg = if (darkMode) Color(0xFF1A1C23) else Color(0xFFF4F5F7)
    val ink = if (darkMode) Color.White else Color(0xFF17181C)
    val line = if (darkMode) Color(0xFF2A2D35) else Color(0xFFE5E7EB)
    val secondaryBg = if (darkMode) Color(0xFF0D0E12) else Color(0xFFECEEF2)

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = cardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DetailMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Acheter ${coin.symbol.uppercase()}", color = ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                    focusedTextColor = ink, unfocusedTextColor = ink,
                    focusedBorderColor = Accent, unfocusedBorderColor = line
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (cryptoQty > 0) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(secondaryBg).padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Vous recevrez", color = DetailMuted, fontSize = 13.sp)
                    Text(formatCryptoQty(cryptoQty) + " ${coin.symbol.uppercase()}", color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    amountMga.toDoubleOrNull()?.let { cryptoVm.buyCrypto(coin.symbol.uppercase(), it, price) }
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
private fun SellModal(coin: CoinMarketData, wallet: CryptoWallet?, cryptoState: CryptoUiState, bankState: BankUiState, cryptoVm: CryptoViewModel, onClose: () -> Unit) {
    val darkMode = LocalDarkMode.current
    var amountCrypto by remember { mutableStateOf("") }
    val price = coin.currentPrice
    val mga = cryptoState.mgaPerUsd
    val balance = wallet?.balance ?: 0.0
    val totalMga = if (amountCrypto.toDoubleOrNull() != null) amountCrypto.toDouble() * price * mga else 0.0

    val bg = if (darkMode) Color(0xFF0D0E12) else Color.White
    val cardBg = if (darkMode) Color(0xFF1A1C23) else Color(0xFFF4F5F7)
    val ink = if (darkMode) Color.White else Color(0xFF17181C)
    val line = if (darkMode) Color(0xFF2A2D35) else Color(0xFFE5E7EB)
    val secondaryBg = if (darkMode) Color(0xFF0D0E12) else Color(0xFFECEEF2)

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = cardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DetailMuted) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp).verticalScroll(rememberScrollState())
        ) {
            Text("Vendre ${coin.symbol.uppercase()}", color = ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                    focusedTextColor = ink, unfocusedTextColor = ink,
                    focusedBorderColor = Green, unfocusedBorderColor = line
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
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(secondaryBg).padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Vous recevrez", color = DetailMuted, fontSize = 13.sp)
                    Text("%,.0f MGA".format(totalMga).replace(",", " "), color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    amountCrypto.toDoubleOrNull()?.let { cryptoVm.sellCrypto(coin.symbol.uppercase(), it, price) }
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
private fun SendModal(coin: CoinMarketData, wallet: CryptoWallet?, cryptoState: CryptoUiState, cryptoVm: CryptoViewModel, onClose: () -> Unit) {
    val darkMode = LocalDarkMode.current
    var amountCrypto by remember { mutableStateOf("") }
    var toAddress by remember { mutableStateOf("") }
    var showQrScanner by remember { mutableStateOf(false) }
    val price = coin.currentPrice
    val balance = wallet?.balance ?: 0.0

    val bg = if (darkMode) Color(0xFF0D0E12) else Color.White
    val cardBg = if (darkMode) Color(0xFF1A1C23) else Color(0xFFF4F5F7)
    val ink = if (darkMode) Color.White else Color(0xFF17181C)
    val line = if (darkMode) Color(0xFF2A2D35) else Color(0xFFE5E7EB)
    val secondaryBg = if (darkMode) Color(0xFF0D0E12) else Color(0xFFECEEF2)

    if (showQrScanner) {
        CryptoQrScanDialog(
            coinSymbol = coin.symbol.uppercase(),
            onAddressScanned = { address ->
                toAddress = address
                showQrScanner = false
            },
            onDismiss = { showQrScanner = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = cardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DetailMuted) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp).verticalScroll(rememberScrollState())
        ) {
            Text("Envoyer ${coin.symbol.uppercase()}", color = ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Disponible : ${formatCryptoQty(balance)} ${coin.symbol.uppercase()}", color = DetailMuted, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = toAddress,
                onValueChange = { toAddress = it },
                label = { Text("Adresse de destination", color = DetailMuted) },
                trailingIcon = {
                    IconButton(onClick = { showQrScanner = true }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner QR", tint = BrandPrimary)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ink, unfocusedTextColor = ink,
                    focusedBorderColor = BrandPrimary, unfocusedBorderColor = line
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
                    focusedTextColor = ink, unfocusedTextColor = ink,
                    focusedBorderColor = BrandPrimary, unfocusedBorderColor = line
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if ((amountCrypto.toDoubleOrNull() ?: 0.0) > 0) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(secondaryBg).padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Valeur estimée", color = DetailMuted, fontSize = 13.sp)
                    Text("${"%.2f".format((amountCrypto.toDoubleOrNull() ?: 0.0) * price)} USD", color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val qty = amountCrypto.toDoubleOrNull() ?: 0.0
                    if (qty > 0 && toAddress.isNotBlank()) {
                        cryptoVm.sendCrypto(coin.symbol.uppercase(), qty, toAddress, price)
                    }
                    onClose()
                },
                enabled = (amountCrypto.toDoubleOrNull() ?: 0.0) > 0
                        && (amountCrypto.toDoubleOrNull() ?: 0.0) <= balance
                        && toAddress.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
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
    val darkMode = LocalDarkMode.current
    val address = wallet?.address ?: "Adresse non disponible"
    val qrBitmap = remember(address) { generateQrBitmap(address, 400) }

    val bg = if (darkMode) Color(0xFF0D0E12) else Color.White
    val cardBg = if (darkMode) Color(0xFF1A1C23) else Color(0xFFF4F5F7)
    val ink = if (darkMode) Color.White else Color(0xFF17181C)
    val line = if (darkMode) Color(0xFF2A2D35) else Color(0xFFE5E7EB)
    val secondaryBg = if (darkMode) Color(0xFF0D0E12) else Color(0xFFECEEF2)

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = cardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DetailMuted) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Recevoir ${coin.symbol.uppercase()}", color = ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                    .background(secondaryBg)
                    .padding(14.dp)
            ) {
                Text(address, color = ink, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(8.dp))
            Text("Réseau : ${coin.name}", color = DetailMuted, fontSize = 12.sp)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CryptoQrScanDialog(
    coinSymbol: String,
    onAddressScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "qr_scan")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 320f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "scanLine"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0E12))
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("Scanner une adresse $coinSymbol", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.size(48.dp))
                }

                Spacer(Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (hasCameraPermission) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx).apply {
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                }
                                val executor = ContextCompat.getMainExecutor(ctx)
                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }
                                        val imageAnalysis = ImageAnalysis.Builder()
                                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                            .build()
                                        imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                            val buffer = imageProxy.planes[0].buffer
                                            val data = ByteArray(buffer.remaining()).also { buffer.get(it) }
                                            try {
                                                val source = PlanarYUVLuminanceSource(
                                                    data, imageProxy.width, imageProxy.height,
                                                    0, 0, imageProxy.width, imageProxy.height, false
                                                )
                                                val result = MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source)))
                                                val text = result.text
                                                if (text.isNotBlank()) {
                                                    val address = parseCryptoAddress(text)
                                                    previewView.post { onAddressScanned(address) }
                                                }
                                            } catch (_: Exception) {
                                            } finally {
                                                imageProxy.close()
                                            }
                                        }
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview, imageAnalysis
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }, executor)
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Autorisez la caméra pour scanner", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .offset(y = scanLineY.dp)
                            .background(Accent)
                    )
                }

                Spacer(Modifier.height(32.dp))
                Text(
                    "Pointez la caméra vers le QR code de l'adresse $coinSymbol",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun parseCryptoAddress(raw: String): String {
    val noScheme = if (raw.contains(":")) raw.substringAfter(":") else raw
    return noScheme.substringBefore("?").trim()
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
