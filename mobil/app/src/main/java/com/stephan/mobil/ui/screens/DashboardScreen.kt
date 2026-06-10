package com.stephan.mobil.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.stephan.mobil.data.model.Transaction
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel

private enum class HomeOverlay { None, Qr, Notifications, Support }
private enum class QrMode { Choice, Scan, Receive, Pay }

private val PageBg = Color(0xFFFFFFFF)
private val Ink = Color(0xFF17181C)
private val Muted = Color(0xFF8B8F98)
private val Soft = Color(0xFFF4F5F7)
private val Line = Color(0xFFECEEF2)
private val Danger = Color(0xFFCB5961)
private val Success = Color(0xFF5DBB82)

@Composable
fun DashboardScreen(
    state: BankUiState,
    vm: BankViewModel,
    openTransfer: () -> Unit,
    openCards: () -> Unit,
    darkMode: Boolean = false,
    onFullScreenChanged: (Boolean) -> Unit = {}
) {
    var overlay by remember { mutableStateOf(HomeOverlay.None) }

    androidx.compose.runtime.LaunchedEffect(overlay) {
        onFullScreenChanged(overlay != HomeOverlay.None)
    }

    when (overlay) {
        HomeOverlay.Qr -> QrCollectionScreen(
            state = state,
            vm = vm,
            onBack = { overlay = HomeOverlay.None }
        )
        HomeOverlay.Notifications -> NotificationsScreen(
            state = state,
            vm = vm,
            onBack = { overlay = HomeOverlay.None }
        )
        HomeOverlay.Support -> SupportChatScreen(onBack = { overlay = HomeOverlay.None })
        HomeOverlay.None -> HomeContent(
            state = state,
            onScan = { overlay = HomeOverlay.Qr },
            onNotifications = { overlay = HomeOverlay.Notifications },
            onSupport = { overlay = HomeOverlay.Support },
            openTransfer = openTransfer,
            openCards = openCards,
            vm = vm,
            darkMode = darkMode
        )
    }
}

@Composable
private fun HomeContent(
    state: BankUiState,
    onScan: () -> Unit,
    onNotifications: () -> Unit,
    onSupport: () -> Unit,
    openTransfer: () -> Unit,
    openCards: () -> Unit,
    vm: BankViewModel,
    darkMode: Boolean
) {
    var showBalance by remember { mutableStateOf(true) }
    val mga = state.mgaPerUsd
    val totalMga = state.balance.totalBalance
    val totalUsd = totalMga / mga
    val cryptoTotalUsd = state.cryptoWallets.sumOf { wallet ->
        val price = state.cryptoMarkets.find { it.id == wallet.coinId }?.currentPrice ?: 0.0
        wallet.balance * price
    }
    val grandTotalUsd = totalUsd + cryptoTotalUsd

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkMode) Color(0xFF101114) else PageBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Soft),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SC", color = Muted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Text(
                    text = "SCpay",
                    modifier = Modifier.weight(1f),
                    color = if (darkMode) Color.White else Ink,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                HeaderIcon(Icons.Default.QrCodeScanner, onScan)
                HeaderIcon(Icons.Default.NotificationsNone, onNotifications)
                HeaderIcon(Icons.Default.HeadsetMic, onSupport)
            }
        }

        item {
            val ink = if (darkMode) Color.White else Ink
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Valeur Totale Estimée", color = Muted, fontSize = 14.sp)
                    Spacer(Modifier.width(10.dp))
                    IconButton(
                        onClick = { showBalance = !showBalance },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            if (showBalance) Icons.Default.RemoveRedEye else Icons.Default.VisibilityOff,
                            null, tint = Muted, modifier = Modifier.size(17.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (showBalance) "%.2f".format(grandTotalUsd) else "••••",
                        color = ink, fontSize = 44.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("USD", color = ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                }
                if (showBalance) {
                    Text(
                        "≈ %,.0f MGA".format(totalMga).replace(",", " ") +
                                if (cryptoTotalUsd > 0) " + %.2f USD crypto".format(cryptoTotalUsd) else "",
                        color = Muted, fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HomeAction("Dépôt", Icons.Default.Add, true) { vm.notify("Depot: choisissez un compte dans Actifs") }
                HomeAction("Acheter Crypto", Icons.Default.Bolt) { vm.notify("Achat crypto pret: selectionnez un actif") }
                HomeAction("Convertir", Icons.Default.SwapHoriz) { openTransfer() }
                HomeAction("Plus", Icons.Default.MoreHoriz) { openCards() }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDDEBF2))
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Inviter des amis", color = Muted, fontSize = 15.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Gagnez jusqu'à 40 % de commissions",
                        color = Ink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$", color = Color(0xFFD83256), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, Line, RoundedCornerShape(10.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Crédit", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.KeyboardArrowRight, null, tint = Muted)
                }
                Spacer(Modifier.height(22.dp))
                Text("Limite jusqu'à (USD)", color = Muted, fontSize = 14.sp)
                Text("****", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = openCards,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Ink),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp)
                ) {
                    Text("Obtenez votre limite", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Transactions", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.Default.MoreHoriz, null, tint = Muted)
            }
        }

        if (state.transactions.isEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    TransactionLine("CLAUDE.AI SUBSCRIPTION", "-20,00 USD", "Refusé", false, "2026-04-15 11:56:31")
                    TransactionLine("Achat par carte", "-10,00 USD", "Succès", true, "•• 1000")
                    TransactionLine("Déposez des pièces", "+50,00 USDT", "Succès", true, "Wallet SCpay")
                }
            }
        } else {
            items(state.transactions.take(5)) { txn ->
                TransactionLine(txn)
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun HeaderIcon(icon: ImageVector, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(38.dp)) {
        Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(25.dp))
    }
}

@Composable
private fun HomeAction(label: String, icon: ImageVector, primary: Boolean = false, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(78.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (primary) Color(0xFF2F3035) else Soft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (primary) Color.White else Ink, modifier = Modifier.size(27.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Ink, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TransactionLine(txn: Transaction) {
    val amount = (if (txn.isCredit) "+" else "-") + "${txn.amount.toLong()} MGA"
    TransactionLine(
        title = txn.description ?: "Opération SCpay",
        amount = amount,
        status = if (txn.isCredit) "Succès" else "Refusé",
        success = txn.isCredit,
        subtitle = txn.createdAt ?: txn.category ?: "•• 1000"
    )
}

@Composable
fun PremiumTransactionRow(txn: Transaction) {
    TransactionLine(txn)
}

@Composable
fun TransactionLine(title: String, amount: String, status: String, success: Boolean, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Soft),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CreditCard, null, tint = Ink, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = Muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(amount, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(status, color = if (success) Success else Danger, fontSize = 13.sp)
        }
    }
}

@Composable
private fun QrCollectionScreen(
    state: BankUiState,
    vm: BankViewModel,
    onBack: () -> Unit
) {
    var mode by remember { mutableStateOf(QrMode.Choice) }
    var scannedPayload by remember { mutableStateOf("") }

    // Navigate to Pay automatically when scan result arrives
    LaunchedEffect(state.qrScanResult) {
        if (state.qrScanResult != null) mode = QrMode.Pay
    }

    when (mode) {
        QrMode.Choice -> QrChoiceScreen(
            onScan = { mode = QrMode.Scan },
            onReceive = {
                state.balance.accounts.firstOrNull()?.let { vm.generateQr(it.id, 0.0) }
                mode = QrMode.Receive
            },
            onBack = onBack
        )
        QrMode.Scan -> ScanToPayScreen(
            payloadToScan = scannedPayload,
            onPayloadChange = { scannedPayload = it },
            onBack = { mode = QrMode.Choice },
            onMyCode = { mode = QrMode.Receive },
            onPhotos = { /* No-op for now */ },
            onScan = { if (scannedPayload.isNotBlank()) vm.scanQr(scannedPayload) }
        )
        QrMode.Receive -> QrReceiveScreen(
            state = state,
            onBack = { mode = QrMode.Choice },
            onGenerate = { amt ->
                state.balance.accounts.firstOrNull()?.let { vm.generateQr(it.id, amt) }
            }
        )
        QrMode.Pay -> PayQrScreen(
            state = state,
            onConfirm = { amount ->
                val payload = state.qrScanResult?.payload ?: scannedPayload
                state.balance.accounts.firstOrNull()?.let { account ->
                    vm.payQr(account.id, payload, amount)
                }
                vm.clearQrScan()
                onBack()
            },
            onBack = {
                vm.clearQrScan()
                mode = QrMode.Scan
            }
        )
    }
}

@Composable
private fun QrChoiceScreen(onScan: () -> Unit, onReceive: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(Soft),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.QrCode2, null, tint = Ink, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Paiement QR", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(12.dp))
        Text("Envoyez ou recevez de l'argent instantanément", color = Muted, fontSize = 16.sp)
        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onScan,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink)
        ) {
            Icon(Icons.Default.QrCodeScanner, null)
            Spacer(Modifier.width(12.dp))
            Text("Scanner pour payer", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onReceive,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Soft, contentColor = Ink)
        ) {
            Icon(Icons.Default.QrCode2, null)
            Spacer(Modifier.width(12.dp))
            Text("Mon code QR", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(60.dp))
        TextButton(onClick = onBack) {
            Text("Retour à l'accueil", color = Muted, fontSize = 16.sp)
        }
    }
}

@Composable
private fun QrReceiveScreen(state: BankUiState, onBack: () -> Unit, onGenerate: (Double) -> Unit) {
    var amountText by remember { mutableStateOf("20") }
    // Use the backend‑generated payload (base64 JSON). If not yet generated, create a demo payload that matches the backend schema.
    val payload = state.qrPayload ?: run {
        val demoMap = mapOf(
            "type" to "bank_payment",
            "account_id" to (state.user?.id ?: 1000),
            "amount" to (amountText.toDoubleOrNull() ?: 0.0)
        )
        val json = Gson().toJson(demoMap)
        Base64.encodeToString(json.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ink) }
                Text("Mon Code", modifier = Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Column {
                Text("Recevoir", color = Ink, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                Text("Montrez ce code pour recevoir un paiement", color = Muted, fontSize = 18.sp)
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Line, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RealQrCode(payload)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Montant à recevoir") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { onGenerate(amountText.toDoubleOrNull() ?: 0.0) },
                        colors = ButtonDefaults.buttonColors(containerColor = Ink),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Mettre à jour")
                    }
                    Button(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, payload)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Partager le QR SCpay"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Soft, contentColor = Ink),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Partager")
                    }
                }
            }
        }
    }
}

@Composable
private fun PayQrScreen(state: BankUiState, onConfirm: (Double) -> Unit, onBack: () -> Unit) {
    val scanResult = state.qrScanResult
    var amount by remember(scanResult?.suggestedAmount) {
        mutableStateOf(scanResult?.suggestedAmount?.let {
            if (it > 0) it.toLong().toString() else ""
        } ?: "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ink) }

        Spacer(Modifier.height(24.dp))
        Text("Payer par QR", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(24.dp))

        // Recipient card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF7F8FA))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD92C55).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (scanResult?.recipientName?.take(2) ?: "??").uppercase(),
                        color = Color(0xFFD92C55),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = scanResult?.recipientName ?: "Destinataire inconnu",
                        color = Ink,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = scanResult?.accountMasked ?: "—",
                        color = Muted,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Person, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(22.dp))
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("Montant à envoyer", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("0") },
            suffix = { Text("MGA") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD92C55),
                unfocusedBorderColor = Color(0xFFE6E8EC)
            )
        )

        Spacer(Modifier.height(8.dp))
        state.balance.accounts.firstOrNull()?.let {
            Text("Solde disponible : ${it.formattedBalance}", color = Muted, fontSize = 13.sp)
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { amount.toDoubleOrNull()?.let { onConfirm(it) } },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            enabled = amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) > 0,
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD92C55))
        ) {
            Text("Confirmer le paiement", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ScanToPayScreen(
    payloadToScan: String,
    onPayloadChange: (String) -> Unit,
    onBack: () -> Unit,
    onMyCode: () -> Unit,
    onPhotos: () -> Unit,
    onScan: () -> Unit
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
        if (!hasCameraPermission) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val translateY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "linePosition"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111312))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.FlashOn, null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(86.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
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
                                        val data = ByteArray(buffer.remaining())
                                        buffer.get(data)

                                        val width = imageProxy.width
                                        val height = imageProxy.height

                                        try {
                                            val source = PlanarYUVLuminanceSource(
                                                data, width, height, 0, 0, width, height, false
                                            )
                                            val bitmapResult = BinaryBitmap(HybridBinarizer(source))
                                            val reader = MultiFormatReader()
                                            val result = reader.decode(bitmapResult)
                                            val text = result.text
                                            if (text.isNotBlank()) {
                                                onPayloadChange(text)
                                                previewView.post {
                                                    onScan()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Ignored
                                        } finally {
                                            imageProxy.close()
                                        }
                                    }

                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
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
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Autorisez la caméra pour scanner",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }

                // Moving scanning line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .offset(y = translateY.dp)
                        .background(Color(0xFFD92C55))
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        Text(
                            "Placez le QR SCpay dans le cadre",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(34.dp))
            Text("Scannez pour payer", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE90038)), contentAlignment = Alignment.Center) {
                    Text("P", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                    Text("SC", color = Color(0xFF2D5BFF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                    Text("V", color = Color(0xFFD92C55), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.weight(1f))
            OutlinedTextField(
                value = payloadToScan,
                onValueChange = onPayloadChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Coller un payload QR ici pour simuler", color = Color.Gray) },
                shape = RoundedCornerShape(14.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                )
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onScan,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Ink),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, null)
                Spacer(Modifier.width(8.dp))
                Text("Valider le scan")
            }
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                ScanBottomAction("Mon code", Icons.Default.QrCode2, onMyCode)
                ScanBottomAction("Album", Icons.Default.Image, onPhotos)
            }
        }
    }
}

@Composable
private fun ScanBottomAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(Modifier.size(58.dp).clip(RoundedCornerShape(14.dp)).background(Color.Black.copy(alpha = 0.72f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 16.sp)
    }
}

@Composable
private fun RealQrCode(payload: String) {
    val bitmap = remember(payload) { createQrBitmap(payload, 240) }
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "QR SCpay",
        modifier = Modifier
            .size(240.dp)
            .background(Color.White)
            .padding(8.dp)
    )
}

private fun createQrBitmap(payload: String, size: Int): Bitmap {
    val hints = mapOf(
        EncodeHintType.MARGIN to 1,
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
    )
    val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size, hints)
    return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
        for (x in 0 until size) {
            for (y in 0 until size) {
                setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
    }
}

@Composable
private fun NotificationsScreen(
    state: BankUiState,
    vm: BankViewModel,
    onBack: () -> Unit
) {
    androidx.compose.runtime.LaunchedEffect(Unit) { vm.loadNotifications() }

    val unreadCount = state.notifications.count { !it.read }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ink) }
                Text("Notifications", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (unreadCount > 0) {
                    TextButton(onClick = { vm.markAllNotificationsRead() }) {
                        Text("Tout lire", color = Color(0xFFD92C55), fontSize = 13.sp)
                    }
                }
            }
        }

        if (state.notifications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Mail, null, tint = Muted, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Aucune notification", color = Muted, fontSize = 15.sp)
                    }
                }
            }
        } else {
            items(state.notifications) { notif ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (!notif.read) vm.markNotificationRead(notif.id) }
                        .background(if (!notif.read) Color(0xFFFFF5F7) else Color.Transparent)
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (!notif.read) Color(0xFFD92C55).copy(alpha = 0.12f) else Soft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Mail, null,
                            tint = if (!notif.read) Color(0xFFD92C55) else Muted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                notif.title,
                                color = Ink,
                                fontSize = 15.sp,
                                fontWeight = if (!notif.read) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (!notif.read) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFD92C55)))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(notif.body, color = Muted, fontSize = 13.sp, lineHeight = 18.sp)
                        notif.createdAt?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, color = Muted, fontSize = 11.sp)
                        }
                        Spacer(Modifier.height(14.dp))
                        Divider(color = Line)
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportChatScreen(onBack: () -> Unit) {
    var message by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("") }
    var showNewTicket by remember { mutableStateOf(false) }
    var showSubjectPicker by remember { mutableStateOf(false) }

    // Simulated ticket history
    val tickets = remember {
        listOf(
            Triple("Virement bloqué", "resolved", "Mon virement de 500 000 MGA n'est pas arrivé…"),
            Triple("Carte virtuelle refusée", "open", "Ma carte est refusée en ligne alors qu'elle est active"),
        )
    }

    val subjects = listOf("Virement / Paiement", "Carte virtuelle", "QR Code", "Compte bloqué", "Fraude suspectée", "Autre")
    var messages by remember {
        mutableStateOf(
            listOf(
                "Bienvenue chez SCpay ! Je suis votre assistant, ici pour vous aider.\n\nDécrivez votre problème avec le plus de détails possible ou choisissez un sujet ci-dessous."
            )
        )
    }

    if (showNewTicket) {
        NewTicketScreen(
            onBack = { showNewTicket = false },
            onSend = { subject, msg ->
                messages = messages + "Nouveau ticket ouvert: [$subject]\n$msg"
                messages = messages + "Ticket #${(100..999).random()} créé. Un agent SCpay vous répondra sous 24h. Vous recevrez une notification."
                showNewTicket = false
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ink) }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD92C55)),
                contentAlignment = Alignment.Center
            ) {
                Text("SC", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Support SCpay", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF5DBB82)))
                    Spacer(Modifier.width(4.dp))
                    Text("En ligne · Répond en < 10 min", color = Muted, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { showNewTicket = true }) {
                Icon(Icons.Default.Add, null, tint = Color(0xFFD92C55))
            }
        }
        Divider(color = Line)

        // Quick subjects
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(subjects) { subject ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selectedSubject == subject) Ink else Soft)
                        .clickable {
                            selectedSubject = subject
                            messages = messages + subject
                            messages = messages + "Vous avez sélectionné: **$subject**\nUn agent va vous aider avec ce sujet. Décrivez votre problème en détail ci-dessous."
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        subject,
                        color = if (selectedSubject == subject) Color.White else Ink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Messages list
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages.size) { index ->
                val isAgent = index % 2 == 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAgent) Arrangement.Start else Arrangement.End
                ) {
                    if (isAgent) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD92C55))
                                .align(Alignment.Bottom),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("SC", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.78f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = if (isAgent) 4.dp else 16.dp,
                                    topEnd = 16.dp,
                                    bottomEnd = if (isAgent) 16.dp else 4.dp,
                                    bottomStart = 16.dp
                                )
                            )
                            .background(if (isAgent) Soft else Ink)
                            .padding(14.dp)
                    ) {
                        if (isAgent) {
                            Text("Agent SCpay", color = Color(0xFFD92C55), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(
                            messages[index],
                            color = if (isAgent) Ink else Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showNewTicket = true }) {
                Icon(Icons.Default.Add, null, tint = Muted)
            }
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                placeholder = { Text("Message…", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD92C55),
                    unfocusedBorderColor = Line
                )
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (message.isNotBlank()) Color(0xFFD92C55) else Soft)
                    .clickable {
                        if (message.isNotBlank()) {
                            val userMsg = message
                            messages = messages + userMsg + "Merci pour votre message. Un agent SCpay va traiter votre demande. Temps de réponse estimé: moins de 10 minutes."
                            message = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    null,
                    tint = if (message.isNotBlank()) Color.White else Muted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun NewTicketScreen(
    onBack: () -> Unit,
    onSend: (subject: String, message: String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("general") }
    var priority by remember { mutableStateOf("medium") }
    var hasPhoto by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) hasPhoto = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ink) }
            Text("Nouveau ticket", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Sujet") },
            placeholder = { Text("Ex: Virement non reçu") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD92C55))
        )
        Spacer(Modifier.height(12.dp))

        // Category chips
        Text("Catégorie", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val cats = listOf("general", "payment", "card", "account", "fraud", "other")
            val labels = listOf("Général", "Paiement", "Carte", "Compte", "Fraude", "Autre")
            items(cats.size) { i ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (category == cats[i]) Ink else Soft)
                        .clickable { category = cats[i] }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(labels[i], color = if (category == cats[i]) Color.White else Ink, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Description du problème") },
            placeholder = { Text("Décrivez votre problème en détail…") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 6,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD92C55))
        )
        Spacer(Modifier.height(12.dp))

        // Photo attachment
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Line, RoundedCornerShape(12.dp))
                .clickable { imagePicker.launch("image/*") }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Image,
                null,
                tint = if (hasPhoto) Color(0xFF5DBB82) else Muted,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                if (hasPhoto) "Photo ajoutée ✓" else "Ajouter une capture d'écran (optionnel)",
                color = if (hasPhoto) Color(0xFF5DBB82) else Muted,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                if (subject.isNotBlank() && message.isNotBlank()) {
                    onSend(subject, message)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = subject.isNotBlank() && message.isNotBlank(),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD92C55))
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, null)
            Spacer(Modifier.width(8.dp))
            Text("Envoyer le ticket", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
    }
}
