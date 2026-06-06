package com.stephan.mobil.ui.screens
import com.stephan.mobil.ui.theme.*

import android.content.Intent
import android.graphics.Bitmap
import android.util.Base64
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.google.gson.Gson
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOff
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.stephan.mobil.data.model.AppNotification
import com.stephan.mobil.data.model.SupportTicket
import com.stephan.mobil.data.model.Transaction
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel
import com.stephan.mobil.security.SecurityUtil

private enum class HomeOverlay { None, Qr, Notifications, Support }
private enum class QrMode { Choice, Scan, Receive, Pay }

private val PageBg = BgBase
private val Ink = TextPrimary
private val Muted = TextSecondary
private val Soft = BgSurfaceHigh
private val Line = BgSurfaceTop
private val Danger = SemanticDanger
private val Success = SemanticSuccess

@Composable
fun DashboardScreen(
    state: BankUiState,
    vm: BankViewModel,
    openTransfer: () -> Unit,
    openCards: () -> Unit,
    openTransactions: () -> Unit = {},
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
        HomeOverlay.Notifications -> NotificationsScreen(state = state, vm = vm, onBack = { overlay = HomeOverlay.None })
        HomeOverlay.Support -> SupportChatScreen(state = state, vm = vm, onBack = { overlay = HomeOverlay.None })
        HomeOverlay.None -> HomeContent(
            state = state,
            onScan = { overlay = HomeOverlay.Qr },
            onNotifications = { overlay = HomeOverlay.Notifications },
            onSupport = { overlay = HomeOverlay.Support },
            openTransfer = openTransfer,
            openCards = openCards,
            openTransactions = openTransactions,
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
    openTransactions: () -> Unit,
    vm: BankViewModel,
    darkMode: Boolean
) {
    val context = LocalContext.current
    val currency = state.balance.currency.ifBlank { "MGA" }
    // formattedTotalBalance contient déjà la devise ("1 952 000,00 MGA") — on l'extrait
    val amountOnly = if (state.balance.formattedTotalBalance.isNotBlank())
        state.balance.formattedTotalBalance
            .replace(currency, "").trim().trimEnd(',').trimEnd('.')
    else
        "%,.0f".format(state.balance.totalBalance).replace(",", " ")

    var selectedTxn by remember { mutableStateOf<Transaction?>(null) }

    if (selectedTxn != null) {
        TransactionDetailScreen(txn = selectedTxn!!, onBack = { selectedTxn = null })
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Header ──────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp).clip(CircleShape)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(BrandPrimary, Color(0xFF7C3AED))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SC", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Text(
                    text = "SCpay",
                    modifier = Modifier.weight(1f).padding(start = 10.dp),
                    color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold
                )
                HeaderIcon(Icons.Default.QrCodeScanner, onScan)
                HeaderIcon(Icons.Default.NotificationsNone, onNotifications)
                HeaderIcon(Icons.Default.HeadsetMic, onSupport)
            }
        }

        // ── Balance card ────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(BgSurfaceElevated, BgSurfaceHigh)
                        )
                    )
                    .border(1.dp, BrandPrimarySoft, RoundedCornerShape(20.dp))
                    .padding(22.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Valeur Totale Estimée", color = Muted, fontSize = 13.sp)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Muted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.RemoveRedEye, null, tint = Muted, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = amountOnly,
                            color = Ink,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            currency,
                            color = Muted,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("Disponible", color = Muted, fontSize = 11.sp)
                            Text("Compte principal", color = Ink.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // ── Quick actions ────────────────────────────────────────
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HomeAction("Dépôt", Icons.Default.Add, true) { openTransfer() }
                HomeAction("Historique", Icons.Default.Bolt) { openTransactions() }
                HomeAction("Convertir", Icons.Default.SwapHoriz) { openTransfer() }
                HomeAction("Plus", Icons.Default.MoreHoriz) { openCards() }
            }
        }

        // ── Referral banner ──────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Color(0xFF0d2d3a), Color(0xFF1a3a4a))
                        )
                    )
                    .border(1.dp, SemanticSuccess.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .clickable {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Rejoins SCpay — la banque mobile la plus rapide ! Télécharge l'appli et gère ton argent en toute sécurité.")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Inviter un ami"))
                    }
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Inviter des amis", color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Gagnez jusqu'à 40 % de commissions",
                        color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$", color = BrandPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ── Credit section ───────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgSurfaceElevated)
                    .border(1.dp, BgSurfaceTop, RoundedCornerShape(16.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Crédit", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.KeyboardArrowRight, null, tint = Muted)
                }
                Spacer(Modifier.height(18.dp))
                Text("Limite jusqu'à ($currency)", color = Muted, fontSize = 13.sp)
                Text("****", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = openCards,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Obtenez votre limite", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // ── Transactions header ──────────────────────────────────
        item {
            Row(
                Modifier.fillMaxWidth().clickable { openTransactions() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transactions", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Voir tout", color = BrandPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (state.transactions.isEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    TransactionLine("CLAUDE.AI SUBSCRIPTION", "-20,00 USD", "Refusé", false, "2026-04-15 11:56:31", onClick = {})
                    TransactionLine("Achat par carte", "-10,00 USD", "Succès", true, "•• 1000", onClick = {})
                    TransactionLine("Déposez des pièces", "+50,00 USDT", "Succès", true, "Wallet SCpay", onClick = {})
                }
            }
        } else {
            items(state.transactions.take(5)) { txn ->
                TransactionLine(txn, onClick = { selectedTxn = txn })
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun HeaderIcon(icon: ImageVector, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(38.dp)) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun HomeAction(label: String, icon: ImageVector, primary: Boolean = false, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(78.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp).clip(CircleShape)
                .background(if (primary) BrandPrimary else BgSurfaceHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TransactionLine(txn: Transaction, onClick: () -> Unit = {}) {
    val amount = (if (txn.isCredit) "+" else "-") + "${txn.amount.toLong()} MGA"
    TransactionLine(
        title = txn.description ?: "Opération SCpay",
        amount = amount,
        status = if (txn.isCredit) "Crédit" else "Débit",
        success = txn.isCredit,
        subtitle = txn.createdAt?.take(10) ?: txn.category ?: "—",
        onClick = onClick
    )
}

@Composable
fun PremiumTransactionRow(txn: Transaction, onClick: () -> Unit = {}) {
    TransactionLine(txn, onClick = onClick)
}

@Composable
fun TransactionLine(title: String, amount: String, status: String, success: Boolean, subtitle: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSurfaceElevated)
            .clickable(onClick = onClick)
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
private fun PaymentSuccessDialog(amount: Double, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(BgSurfaceElevated)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", color = SemanticSuccess, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
            Text("Paiement réussi !", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "%,.0f MGA".format(amount).replace(",", " "),
                color = SemanticSuccess, fontSize = 28.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text("Le destinataire a été notifié du virement.", color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SemanticSuccess)
            ) {
                Text("Retour à l'accueil", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TransactionDetailScreen(txn: Transaction, onBack: () -> Unit) {
    val isCredit = txn.isCredit
    val amountColor = if (isCredit) SemanticSuccess else BrandPrimary
    val amountPrefix = if (isCredit) "+" else "-"
    val categoryLabel = when (txn.category) {
        "salary" -> "Salaire"
        "transfer_in" -> "Virement reçu"
        "transfer_out" -> "Virement envoyé"
        "groceries" -> "Alimentation"
        "utilities" -> "Factures"
        "transport" -> "Transport"
        "dining" -> "Restauration"
        "shopping" -> "Shopping"
        "rent" -> "Loyer"
        "education" -> "Éducation"
        "healthcare" -> "Santé"
        "mobile_money" -> "Mobile Money"
        "refund" -> "Remboursement"
        "interest" -> "Intérêt"
        else -> txn.category?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "Autre"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSurfaceElevated)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = BgSurface)
            }
            Text(
                "Détails",
                color = BgSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgSurfaceElevated)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(amountColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (txn.description?.firstOrNull()?.uppercase() ?: "T"),
                            color = amountColor,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        txn.description ?: "Opération SCpay",
                        color = BgSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "$amountPrefix${"%,.0f".format(txn.amount).replace(",", " ")} MGA",
                        color = amountColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isCredit) Color(0xFFDCFCE7) else Color(0xFFFFE4E6))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            if (isCredit) "Crédit" else "Débit",
                            color = if (isCredit) SemanticSuccess else BrandPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF0F0F5))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(categoryLabel, color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BgSurfaceElevated)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Détails de la transaction", color = BgSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Divider(color = LineColor)
                    DetailRow("Date", txn.createdAt?.take(10) ?: "—")
                    DetailRow("Référence", txn.reference ?: "—")
                    DetailRow("Type", if (isCredit) "Crédit entrant" else "Débit sortant")
                    DetailRow("Catégorie", categoryLabel)
                    if (txn.description != null) DetailRow("Description", txn.description)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.weight(0.4f))
        Text(value, color = BgSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.End, maxLines = 2)
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
    var paidAmount by remember { mutableStateOf(0.0) }

    // Success dialog after payment
    if (state.paymentSuccess != null) {
        PaymentSuccessDialog(
            amount = paidAmount.takeIf { it > 0 } ?: (state.paymentSuccess?.amount ?: 0.0),
            onDismiss = { vm.clearPaymentSuccess(); onBack() }
        )
        return
    }

    when (mode) {
        QrMode.Choice -> QrChoiceScreen(
            onScan = { mode = QrMode.Scan },
            onReceive = {
                state.balance.accounts.firstOrNull()?.let { vm.generateQr(it.id, null) }
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
            onScan = { if (scannedPayload.isNotBlank()) mode = QrMode.Pay }
        )
        QrMode.Receive -> QrReceiveScreen(
            state = state,
            onBack = { mode = QrMode.Choice },
            onGenerate = { amt ->
                state.balance.accounts.firstOrNull()?.let { vm.generateQr(it.id, amt?.takeIf { it >= 100.0 }) }
            }
        )
        QrMode.Pay -> PayQrScreen(
            state = state,
            payload = scannedPayload,
            onConfirm = { amount ->
                paidAmount = amount
                state.balance.accounts.firstOrNull()?.let { account ->
                    vm.payQr(account.id, scannedPayload, amount)
                }
            },
            onBack = { mode = QrMode.Scan }
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
        // Icône centrale avec glow
        Box(
            modifier = Modifier
                .size(90.dp).clip(CircleShape)
                .background(BrandPrimary.copy(alpha = 0.15f))
                .border(2.dp, BrandPrimary.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.QrCode2, null, tint = BrandPrimary, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Paiement QR", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(8.dp))
        Text("Envoyez ou recevez de l'argent instantanément", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(48.dp))

        // Scanner — bouton principal
        Button(
            onClick = onScan,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = Color.White)
        ) {
            Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text("Scanner pour payer", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(14.dp))

        // Mon QR — bouton secondaire
        Button(
            onClick = onReceive,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BgSurfaceHigh, contentColor = Ink),
            border = androidx.compose.foundation.BorderStroke(1.dp, BgSurfaceTop)
        ) {
            Icon(Icons.Default.QrCode2, null, tint = Ink, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text("Mon code QR", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        }

        Spacer(Modifier.height(32.dp))

        // Logos compatibles
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(
                Triple("P", Color(0xFFE90038), Color.White),
                Triple("SC", Color(0xFF2D5BFF), Color.White),
                Triple("V", BrandPrimary, Color.White)
            ).forEach { (label, bg, fg) ->
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(bg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = fg, fontSize = if (label.length > 1) 12.sp else 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("MVola · SCpay · Visa", color = Muted, fontSize = 12.sp)

        Spacer(Modifier.height(32.dp))
        TextButton(onClick = onBack) {
            Text("Retour à l'accueil", color = Muted, fontSize = 15.sp)
        }
    }
}

@Composable
private fun QrReceiveScreen(state: BankUiState, onBack: () -> Unit, onGenerate: (Double?) -> Unit) {
    var amountText by remember { mutableStateOf("20") }
    // Use the backend‑generated payload (base64 JSON). If not yet generated, create a demo payload that matches the backend schema.
    val accountId = state.balance.accounts.firstOrNull()?.id ?: 0
    val payload = state.qrPayload ?: run {
        val demoMap = mapOf(
            "type" to "bank_payment",
            "account_id" to accountId,
            "amount" to (amountText.toDoubleOrNull() ?: 0.0),
            "currency" to "MGA",
            "nonce" to "demo"
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
                    placeholder = { Text("Montant à recevoir", color = Muted) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = Line,
                        cursorColor = BrandPrimary
                    )
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { onGenerate(amountText.toDoubleOrNull()?.takeIf { it >= 100.0 }) },
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
private fun PayQrScreen(state: BankUiState, payload: String, onConfirm: (Double) -> Unit, onBack: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var showPinOverlay by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val decodedQr: Map<String, Any>? = remember(payload) {
        runCatching {
            val normalized = payload.trim()
                .replace("-", "+").replace("_", "/")
                .replace("\\s".toRegex(), "")
            val padded = normalized + "=".repeat((4 - normalized.length % 4) % 4)
            val json = String(android.util.Base64.decode(padded, android.util.Base64.DEFAULT))
            @Suppress("UNCHECKED_CAST")
            Gson().fromJson(json, Map::class.java) as Map<String, Any>
        }.getOrNull()
    }
    val qrAccountId = (decodedQr?.get("account_id") as? Double)?.toInt()
        ?: (state.scannedQrData?.get("account_id") as? Double)?.toInt()
    val qrAmountFromPayload = (decodedQr?.get("amount") as? Double)?.takeIf { it >= 100.0 }
        ?: (state.scannedQrData?.get("amount") as? Double)?.takeIf { it >= 100.0 }

    LaunchedEffect(qrAmountFromPayload) {
        if (qrAmountFromPayload != null && amount.isBlank()) amount = qrAmountFromPayload.toLong().toString()
    }

    val recipientName = qrAccountId?.let { id ->
        state.beneficiaries.firstOrNull { it.id == id }?.name
            ?: state.balance.accounts.firstOrNull { it.id == id }?.let { "Mon compte ${it.accountNumber.takeLast(4)}" }
    } ?: "Compte SCpay #${qrAccountId ?: "?"}"

    val parsedAmount = amount.toDoubleOrNull() ?: 0.0
    val amountValid = parsedAmount >= 100.0
    val availableBalance = state.balance.accounts.firstOrNull()?.balance ?: 0.0
    val hasFunds = parsedAmount <= availableBalance

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBg)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // ── Header ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ink)
                }
                Text(
                    "Payer par QR",
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Destinataire ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgSurfaceElevated)
                    .border(1.dp, BgSurfaceTop, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp).clip(CircleShape)
                        .background(BrandPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        recipientName.firstOrNull()?.uppercase() ?: "?",
                        color = BrandPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Destinataire", color = Muted, fontSize = 11.sp)
                    Text(recipientName, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.QrCode2, null,
                    tint = BrandPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Saisie du montant ────────────────────────────
            Text("Montant", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("0", color = Muted, fontSize = 32.sp) },
                suffix = {
                    Text(
                        "MGA",
                        color = Muted,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 16.sp
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                    focusedBorderColor = BrandPrimary,
                    unfocusedBorderColor = BgSurfaceTop,
                    cursorColor = BrandPrimary
                )
            )

            Spacer(Modifier.height(8.dp))

            // Solde + validations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Disponible : %,.0f MGA".format(availableBalance).replace(",", " "),
                    color = Muted, fontSize = 12.sp
                )
                if (amount.isNotEmpty()) {
                    when {
                        !amountValid -> Text("Min. 100 MGA", color = SemanticDanger, fontSize = 12.sp)
                        !hasFunds -> Text("Solde insuffisant", color = SemanticDanger, fontSize = 12.sp)
                        else -> Text("✓ Montant valide", color = SemanticSuccess, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Résumé avant confirmation ────────────────────
            if (amountValid && hasFunds) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BrandPrimary.copy(alpha = 0.08f))
                        .border(1.dp, BrandPrimary.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Vous envoyez", color = Muted, fontSize = 12.sp)
                            Text(
                                "%,.0f MGA".format(parsedAmount).replace(",", " "),
                                color = Ink,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.Send, null,
                            tint = BrandPrimary, modifier = Modifier.size(22.dp)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text("À", color = Muted, fontSize = 12.sp)
                            Text(
                                recipientName,
                                color = Ink,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 140.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Bouton Confirmer ─────────────────────────────
            Button(
                onClick = { if (amountValid && hasFunds) showPinOverlay = true },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                enabled = amountValid && hasFunds,
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary,
                    disabledContainerColor = BgSurfaceHigh
                )
            ) {
                Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Confirmer avec PIN", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.height(24.dp))
        }

        // ── PIN Overlay (slide-up) ────────────────────────────
        if (showPinOverlay) {
            QrPinConfirmOverlay(
                amount = parsedAmount,
                recipient = recipientName,
                context = context,
                activity = activity,
                onSuccess = {
                    showPinOverlay = false
                    onConfirm(parsedAmount)
                },
                onDismiss = { showPinOverlay = false }
            )
        }
    }
}

@Composable
private fun QrPinConfirmOverlay(
    amount: Double,
    recipient: String,
    context: android.content.Context,
    activity: FragmentActivity?,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var attempts by remember { mutableIntStateOf(0) }
    var cooldown by remember { mutableIntStateOf(0) }
    val hasBiometric = remember { SecurityUtil.isBiometricEnabled(context) }
    val hasPin = remember { SecurityUtil.hasPinCode(context) }

    LaunchedEffect(cooldown) {
        if (cooldown > 0) { kotlinx.coroutines.delay(1000); cooldown -= 1 }
    }

    // Vérification automatique quand 4 chiffres saisis
    LaunchedEffect(pin) {
        if (pin.length == 4) {
            kotlinx.coroutines.delay(120)
            if (!hasPin || SecurityUtil.verifyPinCode(context, pin)) {
                onSuccess()
            } else {
                error = "PIN incorrect"
                pin = ""
                attempts++
                if (attempts >= 3) { cooldown = 30; attempts = 0; error = "Trop de tentatives — attendez ${cooldown}s" }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(BgSurface)
                .border(
                    width = 1.dp,
                    color = BgSurfaceTop,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .background(BgSurfaceTop, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.height(20.dp))

            // Résumé du paiement
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BgSurfaceElevated)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Confirmer le paiement", color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "%,.0f MGA".format(amount).replace(",", " "),
                        color = Ink,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Text(
                        "→ $recipient",
                        color = Muted, fontSize = 13.sp, maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Titre PIN
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, null, tint = BrandPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (!hasPin) "Saisir votre code PIN" else "Code PIN SCpay",
                    color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(16.dp))

            // Dots indicateur
            if (cooldown > 0) {
                Text("Trop de tentatives — réessayez dans ${cooldown}s", color = SemanticDanger, fontSize = 13.sp)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    for (i in 0 until 4) {
                        val filled = i < pin.length
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (filled) BrandPrimary else BgSurfaceTop)
                                .border(1.dp, if (filled) BrandPrimary else BgSurfaceHigh, CircleShape)
                        )
                    }
                }
                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = SemanticDanger, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Clavier numérique
            PinKeypadCompact(
                enabled = cooldown == 0,
                showBiometric = hasBiometric,
                onKey = { if (pin.length < 4) pin += it },
                onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                onBiometric = {
                    if (activity != null) {
                        val executor = ContextCompat.getMainExecutor(activity)
                        BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationSucceeded(r: BiometricPrompt.AuthenticationResult) { onSuccess() }
                            override fun onAuthenticationError(code: Int, msg: CharSequence) { error = "Biométrie échouée, utilisez le PIN" }
                        }).authenticate(
                            BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Confirmer le paiement")
                                .setSubtitle("%,.0f MGA → $recipient".format(amount).replace(",", " "))
                                .setNegativeButtonText("Utiliser le PIN")
                                .build()
                        )
                    }
                }
            )

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Annuler", color = Muted, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun PinKeypadCompact(
    enabled: Boolean,
    showBiometric: Boolean,
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onBiometric: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("bio", "0", "del")
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                when (key) {
                                    "bio", "del" -> Color.Transparent
                                    else -> BgSurfaceHigh
                                }
                            )
                            .clickable(enabled = enabled) {
                                when (key) {
                                    "bio" -> if (showBiometric) onBiometric()
                                    "del" -> onBackspace()
                                    else -> onKey(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (key) {
                            "bio" -> if (showBiometric) {
                                Icon(Icons.Default.Fingerprint, null, tint = BrandPrimary, modifier = Modifier.size(30.dp))
                            }
                            "del" -> Icon(Icons.Default.Backspace, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
                            else -> Text(key, color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
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
    var torchEnabled by remember { mutableStateOf(false) }
    val cameraRef = remember { androidx.compose.runtime.mutableStateOf<androidx.camera.core.Camera?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )
    val albumLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                try {
                    val bmp = android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    val px = IntArray(bmp.width * bmp.height)
                    bmp.getPixels(px, 0, bmp.width, 0, 0, bmp.width, bmp.height)
                    val src = com.google.zxing.RGBLuminanceSource(bmp.width, bmp.height, px)
                    val result = MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(src)))
                    if (result.text.isNotBlank()) { onPayloadChange(result.text); onScan() }
                } catch (_: Exception) {}
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(torchEnabled) {
        cameraRef.value?.cameraControl?.enableTorch(torchEnabled)
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
                IconButton(onClick = { torchEnabled = !torchEnabled }) {
                    Icon(
                        if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Torche",
                        tint = if (torchEnabled) Color.Yellow else Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
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
                                    val camera = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                    )
                                    cameraRef.value = camera
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
                        .background(BrandPrimary)
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
                Box(Modifier.size(48.dp).clip(CircleShape).background(BgSurfaceElevated), contentAlignment = Alignment.Center) {
                    Text("SC", color = Color(0xFF2D5BFF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.size(48.dp).clip(CircleShape).background(BgSurfaceElevated), contentAlignment = Alignment.Center) {
                    Text("V", color = BrandPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
                colors = ButtonDefaults.buttonColors(containerColor = BgSurfaceElevated, contentColor = Ink),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, null)
                Spacer(Modifier.width(8.dp))
                Text("Valider le scan")
            }
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                ScanBottomAction("Mon code", Icons.Default.QrCode2, onMyCode)
                ScanBottomAction("Album", Icons.Default.Image) { albumLauncher.launch("image/*") }
            }
        }
    }
}

@Composable
private fun ScanBottomAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
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
            .background(BgSurfaceElevated)
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
private fun NotificationsScreen(state: BankUiState, vm: BankViewModel, onBack: () -> Unit) {
    LaunchedEffect(Unit) { vm.loadNotifications() }

    val notifications = state.notifications

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ink) }
                Text("Notifications", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (notifications.any { !it.read }) {
                    TextButton(onClick = { vm.markAllNotificationsRead() }) {
                        Text("Tout lire", color = BrandPrimary, fontSize = 13.sp)
                    }
                }
            }
        }
        if (notifications.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.Mail, null, tint = Muted, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Aucune notification", color = Muted, fontSize = 16.sp)
                }
            }
        } else {
            items(notifications) { notif ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!notif.read) Color(0xFFFFF0F3) else Color.Transparent),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(Modifier.size(34.dp).clip(CircleShape).background(if (!notif.read) BrandPrimary.copy(alpha = 0.12f) else Soft), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Mail, null, tint = if (!notif.read) BrandPrimary else Ink, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(notif.title, color = Ink, fontSize = 15.sp, fontWeight = if (!notif.read) FontWeight.Bold else FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            if (!notif.read) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(BrandPrimary))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(notif.body, color = Muted, fontSize = 13.sp, lineHeight = 18.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(notif.createdAt, color = Muted, fontSize = 11.sp)
                        Spacer(Modifier.height(14.dp))
                        Divider(color = Line)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun SupportChatScreen(state: BankUiState, vm: BankViewModel, onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val ticket = state.supportTicket
    val messages = ticket?.messages ?: emptyList()

    LaunchedEffect(Unit) { vm.loadSupportTicket() }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(BgSurfaceElevated).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Ink) }
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(BrandPrimary.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.HeadsetMic, null, tint = BrandPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("SCpay Support", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (state.supportLoading) "Chargement..." else if (ticket?.status == "open") "En ligne · Répond en moins de 10 min" else "Ticket fermé",
                    color = if (ticket?.status == "open") SemanticSuccess else Muted,
                    fontSize = 12.sp
                )
            }
        }
        Divider(color = Line)

        // Messages list
        if (state.supportLoading && ticket == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary)
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { msg ->
                    val isAdmin = msg.sender == "admin"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isAdmin) Arrangement.Start else Arrangement.End
                    ) {
                        if (isAdmin) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(CircleShape).background(BrandPrimary.copy(0.1f)).align(Alignment.Bottom),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.HeadsetMic, null, tint = BrandPrimary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(if (isAdmin) 0.82f else 0.74f),
                            horizontalAlignment = if (isAdmin) Alignment.Start else Alignment.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 300.dp)
                                    .clip(RoundedCornerShape(
                                        topStart = 18.dp, topEnd = 18.dp,
                                        bottomEnd = if (isAdmin) 18.dp else 4.dp,
                                        bottomStart = if (isAdmin) 4.dp else 18.dp
                                    ))
                                    .background(if (isAdmin) Soft else BrandPrimary)
                                    .padding(12.dp, 10.dp)
                            ) {
                                Text(msg.message, color = if (isAdmin) Ink else Color.White, fontSize = 15.sp, lineHeight = 22.sp)
                            }
                            Text(
                                msg.createdAt,
                                color = Muted,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 3.dp),
                                textAlign = if (isAdmin) androidx.compose.ui.text.style.TextAlign.Start else androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    }
                }
                if (state.supportLoading) {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Box(Modifier.size(32.dp).clip(CircleShape).background(Soft), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.HeadsetMic, null, tint = Muted, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Box(Modifier.clip(RoundedCornerShape(18.dp)).background(Soft).padding(14.dp, 10.dp)) {
                                Text("...", color = Muted, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }

        // Input bar
        Divider(color = Line)
        Row(
            modifier = Modifier.fillMaxWidth().background(BgSurfaceElevated).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Envoyer un message...", color = Muted, fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                singleLine = false,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimary,
                    unfocusedBorderColor = Line,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (messageText.isNotBlank() && !state.supportLoading) {
                        vm.sendSupportMessage(messageText.trim())
                        messageText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (messageText.isNotBlank()) BrandPrimary else Soft)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null, tint = if (messageText.isNotBlank()) Color.White else Muted)
            }
        }
    }
}
