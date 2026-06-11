package com.stephan.mobil.ui.screens
import com.stephan.mobil.ui.theme.*

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.data.model.Card
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel
import kotlin.math.absoluteValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

private val CardPageBg = BgBase
private val CardInk = TextPrimary
private val CardMuted = TextSecondary
private val CardSoft = BgSurfaceHigh

@Composable
fun CardsScreen(state: BankUiState, vm: BankViewModel) {
    val darkMode = LocalDarkMode.current
    var showChooseCard by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var settingsCardId by remember { mutableIntStateOf(0) }

    val selectedCard = state.cards.firstOrNull { it.id == settingsCardId }
        ?: state.cards.firstOrNull()

    when {
        showSettings && selectedCard != null -> CardSettingsPage(
            card = selectedCard,
            accountId = state.balance.accounts.firstOrNull()?.id ?: 1,
            vm = vm,
            onBack = { showSettings = false }
        )
        showChooseCard -> ChooseCardScreen(
            state = state,
            vm = vm,
            onBack = { showChooseCard = false }
        )
        else -> CardsMainContent(
            state = state,
            vm = vm,
            onAddCard = { showChooseCard = true },
            onSettings = { cardId -> settingsCardId = cardId; showSettings = true }
        )
    }
}

@Composable
private fun CardSettingsPage(
    card: Card,
    accountId: Int,
    vm: BankViewModel,
    onBack: () -> Unit
) {
    val darkMode = LocalDarkMode.current
    val ink = if (darkMode) TextPrimary else BgSurface
    val bg = if (darkMode) BgBase else Color.White
    val cardBg = if (darkMode) BgSurfaceElevated else LightBackground
    val dividerColor = if (darkMode) BgSurfaceTop else Color(0xFFE5E7EB)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(24.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ink)
            }
            Text("Paramètres de la carte", color = ink, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Carte ••${card.lastFour}", color = ink, fontWeight = FontWeight.SemiBold)
                Text(
                    if (card.isBlocked) "Bloquée" else "Active",
                    color = if (card.isBlocked) SemanticDanger else SemanticSuccess,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Divider(color = dividerColor)
            SettingsRow("Type", card.type.replaceFirstChar { it.uppercase() }, ink)
            SettingsRow("Expiration", card.expiryDate.ifBlank { "N/A" }, ink)
            SettingsRow("Limite journalière", "%,.0f MGA".format(card.dailyLimit).replace(",", " "), ink)
            SettingsRow("Statut", if (card.isBlocked) "🔴 Bloquée" else "🟢 Active", ink)
        }

        Spacer(Modifier.height(28.dp))
        Text("Actions", color = CardMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        if (card.id != 0) {
            SettingsActionButton(
                label = if (card.isBlocked) "Débloquer la carte" else "Bloquer la carte",
                icon = if (card.isBlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                color = if (card.isBlocked) SemanticSuccess else BrandPrimary
            ) { vm.toggleCard(card.id); onBack() }

            Spacer(Modifier.height(12.dp))

            SettingsActionButton(
                label = "Augmenter la limite (+500 000 MGA)",
                icon = Icons.Default.Tune,
                color = Color(0xFF5B8DEF)
            ) { vm.updateCardLimit(card.id, card.dailyLimit + 500_000.0); onBack() }
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String, ink: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = CardMuted, fontSize = 14.sp)
        Text(value, color = ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SettingsActionButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, color = color, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
private fun ChooseCardScreen(
    state: BankUiState,
    vm: BankViewModel,
    onBack: () -> Unit
) {
    val darkMode = LocalDarkMode.current
    val context = LocalContext.current
    val accountId = state.balance.accounts.firstOrNull()?.id ?: 1
    val sharedPrefs = remember { context.getSharedPreferences("card_designs", Context.MODE_PRIVATE) }
    var isVirtualSelected by remember { mutableStateOf(true) }
    var selectedDesign by remember { mutableStateOf(1) } // 1 = Solana (Default in screenshot 1)
    val pageBg = if (darkMode) BgBase else Color.White
    val ink = if (darkMode) TextPrimary else BgSurface
    val muted = if (darkMode) TextSecondary else Color(0xFF737780)
    val segmentBg = if (darkMode) BgSurfaceElevated else LightBackground
    val segmentSelectedBg = if (darkMode) BgSurfaceHigh else Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = ink
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Title
        Text(
            text = "Choisir une carte",
            color = ink,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(24.dp))

        // Tab Selector (Segmented control)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(segmentBg)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isVirtualSelected) {
                            segmentSelectedBg
                        } else Color.Transparent
                    )
                    .clickable { isVirtualSelected = true }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Carte virtuelle",
                    color = ink,
                    fontWeight = if (isVirtualSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (!isVirtualSelected) {
                            segmentSelectedBg
                        } else Color.Transparent
                    )
                    .clickable { isVirtualSelected = false }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Carte physique",
                    color = ink,
                    fontWeight = if (!isVirtualSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Vertical Card Preview — hauteur fixe pour éviter le débordement
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(292.dp),
            contentAlignment = Alignment.Center
        ) {
            VerticalCardPreview(
                designIndex = selectedDesign,
                isVirtual = isVirtualSelected
            )
        }

        Spacer(Modifier.height(20.dp))

        // Text Info below card
        val cardTitle = when (selectedDesign) {
            1 -> "Carte Solana"
            2 -> "Nature · Palmier émeraude"
            else -> "Carte standard"
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = cardTitle,
                color = ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Compatible avec Apple Pay et Google Pay.",
                color = muted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Indicator Selector dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp)
        ) {
            listOf(0, 1, 2).forEach { index ->
                val isSelected = index == selectedDesign
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                if (darkMode) Color.White.copy(alpha = 0.15f) else BrandPrimary.copy(alpha = 0.14f)
                            } else Color.Transparent
                        )
                        .clickable { selectedDesign = index }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                when (index) {
                                    1 -> Brush.linearGradient(listOf(Color(0xFF7D56F4), Color(0xFF28D0A0), Color(0xFF1E3C72)))
                                    2 -> Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D)))
                                    else -> Brush.linearGradient(listOf(Color(0xFF171717), Color(0xFF434343)))
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Demande de carte button
        Button(
            onClick = {
                if (!isVirtualSelected) {
                    vm.notify("Les cartes physiques ne sont pas disponibles actuellement. Veuillez choisir une carte virtuelle.")
                } else {
                    // Set pending design mapping in SharedPreferences
                    sharedPrefs.edit().putInt("pending_card_design", selectedDesign).apply()
                    vm.createCard(accountId, 5_000_000.0)
                    onBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandPrimary,
                contentColor = Color.White
            )
        ) {
            Text("Demande de carte · 10 USD", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun VerticalCardPreview(designIndex: Int, isVirtual: Boolean) {
    val brush = getCardBrush(designIndex)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.72f)
            .aspectRatio(0.65f)
            .clip(RoundedCornerShape(18.dp))
            .background(brush)
            .padding(20.dp)
    ) {
        // RedotPay text vertically rotated on the right side
        Text(
            text = "RedotPay",
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .graphicsLayer {
                    rotationZ = 90f
                    translationX = 30f
                    translationY = 40f
                }
        )

        // Visa Logo at bottom-left
        Text(
            "VISA",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            modifier = Modifier.align(Alignment.BottomStart)
        )

        // Specific design features
        when (designIndex) {
            1 -> {
                // Solana logo: three parallel slanted white bars
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(Modifier.size(width = 24.dp, height = 4.dp).graphicsLayer { rotationZ = -15f }.background(Color.White, RoundedCornerShape(2.dp)))
                    Box(Modifier.size(width = 24.dp, height = 4.dp).graphicsLayer { rotationZ = -15f }.background(Color.White, RoundedCornerShape(2.dp)))
                    Box(Modifier.size(width = 24.dp, height = 4.dp).graphicsLayer { rotationZ = -15f }.background(Color.White, RoundedCornerShape(2.dp)))
                }
            }
            2 -> {
                // Nature: Leaf logo or faint veins in background
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val path1 = Path().apply {
                        moveTo(0f, h * 0.2f)
                        quadraticTo(w * 0.4f, h * 0.4f, w, h * 0.3f)
                    }
                    val path2 = Path().apply {
                        moveTo(0f, h * 0.4f)
                        quadraticTo(w * 0.5f, h * 0.6f, w, h * 0.5f)
                    }
                    val path3 = Path().apply {
                        moveTo(0f, h * 0.6f)
                        quadraticTo(w * 0.3f, h * 0.8f, w, h * 0.7f)
                    }
                    drawPath(path1, Color.White.copy(alpha = 0.08f), style = Stroke(width = 4f))
                    drawPath(path2, Color.White.copy(alpha = 0.08f), style = Stroke(width = 4f))
                    drawPath(path3, Color.White.copy(alpha = 0.08f), style = Stroke(width = 4f))
                }
                Canvas(modifier = Modifier.size(28.dp).align(Alignment.BottomEnd)) {
                    val leafPath = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.8f)
                        quadraticTo(size.width * 0.1f, size.height * 0.3f, size.width * 0.8f, size.height * 0.2f)
                        quadraticTo(size.width * 0.8f, size.height * 0.7f, size.width * 0.2f, size.height * 0.8f)
                        moveTo(size.width * 0.2f, size.height * 0.8f)
                        lineTo(size.width * 0.8f, size.height * 0.2f)
                    }
                    drawPath(leafPath, Color.White.copy(alpha = 0.8f))
                }
            }
            else -> {
                // Standard: plain clean look with gold chip representation
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 24.dp)
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE5A93C))
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardsMainContent(
    state: BankUiState,
    vm: BankViewModel,
    onAddCard: () -> Unit,
    onSettings: (Int) -> Unit = {}
) {
    val darkMode = LocalDarkMode.current
    val accountId = state.balance.accounts.firstOrNull()?.id ?: 1
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("card_designs", Context.MODE_PRIVATE) }

    val cards = state.cards.ifEmpty {
        listOf(
            Card(
                id = 0,
                cardNumberMasked = "•••• •••• •••• 1000",
                lastFour = "1000",
                expiryDate = "••••",
                isBlocked = false,
                type = "virtual",
                dailyLimit = 20.0
            )
        )
    }

    var selectedCardIndex by remember { mutableStateOf(0) }
    var revealedCardId by remember { mutableStateOf<Int?>(null) }
    var showWalletDialog by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    var limitInput by remember { mutableStateOf("") }

    // Keep active card selection in bounds
    LaunchedEffect(cards.size) {
        if (selectedCardIndex >= cards.size) {
            selectedCardIndex = maxOf(0, cards.size - 1)
        }
    }

    val card = cards.getOrNull(selectedCardIndex) ?: cards.first()
    val isRevealed = revealedCardId == card.id
    val pageBg = if (darkMode) BgBase else Color.White
    val ink = if (darkMode) TextPrimary else BgSurface
    val muted = if (darkMode) TextSecondary else Color(0xFF737780)

    // Dialog: modifier la limite
    if (showLimitDialog && card.id != 0) {
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            title = { Text("Modifier la limite", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Limite actuelle : %,.0f MGA".format(card.dailyLimit).replace(",", " "), color = TextSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { limitInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Nouvelle limite (MGA)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newLimit = limitInput.toDoubleOrNull()
                        if (newLimit != null && newLimit >= 10_000) {
                            vm.updateCardLimit(card.id, newLimit)
                            showLimitDialog = false
                            limitInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false; limitInput = "" }) { Text("Annuler") }
            }
        )
    }

    // Dialog: Google Wallet
    if (showWalletDialog) {
        AlertDialog(
            onDismissRequest = { showWalletDialog = false },
            title = { Text("Google Wallet", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ajouter votre carte SCpay •••• ${card.lastFour} à Google Wallet ?")
                    Spacer(Modifier.height(4.dp))
                    Text("Cette fonctionnalité nécessite une validation bancaire supplémentaire. Contactez votre conseiller SCpay pour activer le paiement NFC.", color = TextSecondary, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pay.google.com"))
                        context.startActivity(intent)
                        showWalletDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                ) { Text("Ouvrir Google Pay") }
            },
            dismissButton = {
                TextButton(onClick = { showWalletDialog = false }) { Text("Annuler") }
            }
        )
    }

    PullToRefreshBox(
        isRefreshing = state.loading,
        onRefresh = { vm.refreshAll() },
        modifier = Modifier.fillMaxSize()
    ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cartes",
                    color = ink,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val url = "http://192.168.88.239:8000/api/statements/monthly?account_id=${state.balance.accounts.firstOrNull()?.id ?: 1}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }) {
                    Icon(
                        Icons.Default.MailOutline,
                        null,
                        tint = ink,
                        modifier = Modifier.size(26.dp)
                    )
                }
                IconButton(onClick = onAddCard) {
                    Icon(
                        Icons.Default.AddCircleOutline,
                        null,
                        tint = ink,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Horizontal Pager with snap-to-center
        item {
            val pagerState = rememberPagerState(
                initialPage = selectedCardIndex.coerceIn(0, cards.size - 1),
                pageCount = { cards.size }
            )

            // Keep selectedCardIndex in sync with pager
            LaunchedEffect(pagerState.currentPage) {
                selectedCardIndex = pagerState.currentPage
            }
            // If selectedCardIndex changes externally (e.g. initial load), animate pager
            LaunchedEffect(selectedCardIndex) {
                if (pagerState.currentPage != selectedCardIndex) {
                    pagerState.animateScrollToPage(selectedCardIndex.coerceIn(0, cards.size - 1))
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    pageSpacing = 10.dp,
                ) { page ->
                    val c = cards[page]
                    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    val scale by animateFloatAsState(
                        targetValue = if (pageOffset == 0f) 1f else 0.88f,
                        animationSpec = spring(stiffness = 300f),
                        label = "cardScale_$page"
                    )
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                alpha = if (pageOffset.absoluteValue > 0.5f) 0.6f else 1f
                            }
                    ) {
                        FlippableScpayCard(
                            card = c,
                            userName = state.user?.name ?: "AROVANA",
                            designIndex = getCardDesign(c, sharedPrefs),
                            revealed = revealedCardId == c.id,
                            cvv = if (revealedCardId == c.id) state.revealedCard?.cvv.orEmpty() else ""
                        )
                    }
                }

                if (cards.size > 1) {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        cards.forEachIndexed { index, _ ->
                            val isActive = index == pagerState.currentPage
                            val dotWidth by animateFloatAsState(
                                targetValue = if (isActive) 22f else 8f,
                                animationSpec = spring(stiffness = 400f),
                                label = "dot_$index"
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(width = dotWidth.dp, height = 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isActive) BrandPrimary
                                        else muted.copy(alpha = 0.35f)
                                    )
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CardAction(
                    label = if (isRevealed) "Masquer" else "Voir",
                    icon = Icons.Default.RemoveRedEye
                ) {
                    if (isRevealed) {
                        revealedCardId = null
                        vm.clearRevealedCard()
                    } else {
                        revealedCardId = card.id
                        vm.revealCard(card.id)
                    }
                }
                CardAction(
                    label = if (card.isBlocked) "Débloquer" else "Bloquer",
                    icon = if (card.isBlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                    onClick = { if (card.id != 0) vm.toggleCard(card.id) }
                )
                CardAction("Limite", Icons.Default.Tune) {
                    if (card.id != 0) { limitInput = card.dailyLimit.toLong().toString(); showLimitDialog = true }
                    else vm.notify("Créez une carte pour gérer la limite")
                }
                CardAction("Paramètre", Icons.Default.Settings) { onSettings(card.id) }
            }
        }

        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (darkMode) BgSurfaceHigh else Color(0xFFEDEEF2))
                        .clickable { showWalletDialog = true }
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFFBBC05), Color(0xFFEA4335))
                                )
                            )
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Add to", color = Color.White, fontSize = 12.sp)
                        Text("Google Wallet", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Transactions",
                    color = ink,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ViewModule, null, tint = muted)
                Spacer(Modifier.width(18.dp))
                Text("...", color = muted, fontSize = 24.sp)
            }
        }

        if (state.transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucune transaction récente", color = muted, fontSize = 14.sp)
                }
            }
        } else {
            items(state.transactions.take(5)) { txn ->
                PremiumTransactionRow(txn)
            }
        }

        item { Spacer(Modifier.height(22.dp)) }
    }
    } // PullToRefreshBox
}

@Composable
private fun FlippableScpayCard(card: Card, userName: String, designIndex: Int, revealed: Boolean = false, cvv: String = "") {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 120f),
        label = "cardFlip"
    )
    val showBack = rotation.absoluteValue > 90f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(218.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 16f * density
            }
            .clickable { flipped = !flipped }
    ) {
        if (showBack) {
            CardBack(card, designIndex, revealed, Modifier.graphicsLayer { rotationY = 180f })
        } else {
            CardFront(card, userName, designIndex, revealed = revealed, cvv = cvv)
        }
    }
}

@Composable
private fun CardFront(card: Card, userName: String, designIndex: Int, modifier: Modifier = Modifier, revealed: Boolean = false, cvv: String = "") {
    val brush = getCardBrush(designIndex)
    // Generate deterministic partial card number for revealed mode
    val fakePart1 = (4500 + (card.id * 37) % 99).toString()
    val fakePart2 = ((card.id * 1234 + 5678) % 10000).toString().padStart(4, '0')
    val fakePart3 = ((card.id * 9876 + 1234) % 10000).toString().padStart(4, '0')
    val cardNumber = if (revealed) "$fakePart1 $fakePart2 $fakePart3 ${card.lastFour}"
                     else "•••• •••• •••• ${card.lastFour}"
    val expiryDisplay = if (revealed && card.expiryDate.isNotBlank()) card.expiryDate else "••/••"
    val cvvDisplay = if (revealed && cvv.isNotBlank()) cvv else "•••"

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(brush)
            .padding(24.dp)
    ) {
        Text("MGA", color = Color.White, fontSize = 17.sp, modifier = Modifier.align(Alignment.TopEnd))
        Column(Modifier.align(Alignment.BottomStart)) {
            Text(cardNumber, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(44.dp)) {
                Column {
                    Text("Valide jusqu'au", color = Color.White.copy(alpha = 0.82f), fontSize = 13.sp)
                    Text(expiryDisplay, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("CVV", color = Color.White.copy(alpha = 0.82f), fontSize = 13.sp)
                    Text(cvvDisplay, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Card design-specific elements
        when (designIndex) {
            1 -> {
                // Solana logo
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(Modifier.size(width = 16.dp, height = 3.dp).graphicsLayer { rotationZ = -15f }.background(Color.White, RoundedCornerShape(1.dp)))
                    Box(Modifier.size(width = 16.dp, height = 3.dp).graphicsLayer { rotationZ = -15f }.background(Color.White, RoundedCornerShape(1.dp)))
                    Box(Modifier.size(width = 16.dp, height = 3.dp).graphicsLayer { rotationZ = -15f }.background(Color.White, RoundedCornerShape(1.dp)))
                }
            }
            2 -> {
                // Nature: Leaf logo or faint veins in background
                Canvas(modifier = Modifier.size(24.dp).align(Alignment.BottomEnd)) {
                    val leafPath = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.8f)
                        quadraticTo(size.width * 0.1f, size.height * 0.3f, size.width * 0.8f, size.height * 0.2f)
                        quadraticTo(size.width * 0.8f, size.height * 0.7f, size.width * 0.2f, size.height * 0.8f)
                        moveTo(size.width * 0.2f, size.height * 0.8f)
                        lineTo(size.width * 0.8f, size.height * 0.2f)
                    }
                    drawPath(leafPath, Color.White.copy(alpha = 0.8f))
                }
            }
            else -> {
                Text(userName.uppercase(), color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp, modifier = Modifier.align(Alignment.BottomEnd))
            }
        }
    }
}

@Composable
private fun CardBack(card: Card, designIndex: Int, revealed: Boolean = false, modifier: Modifier = Modifier) {
    val brush = getCardBrush(designIndex)
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(brush)
            .padding(24.dp)
    ) {
        Text("SCpay", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopStart))
        Column(Modifier.align(Alignment.BottomStart)) {
            Text("Limite : %,.0f MGA".format(card.dailyLimit).replace(",", " "), color = Color.White.copy(0.7f), fontSize = 12.sp)
            Text(if (revealed) card.expiryDate.ifBlank { "N/A" } else "Expiration : ••/••", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("VISA", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
private fun CardAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    val darkMode = LocalDarkMode.current
    val ink = if (darkMode) TextPrimary else BgSurface
    val soft = if (darkMode) BgSurfaceHigh else SoftBackground
    Column(
        modifier = Modifier
            .width(78.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(soft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = ink, modifier = Modifier.size(27.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(label, color = ink, fontSize = 14.sp)
    }
}

private fun getCardDesign(card: Card, sharedPrefs: android.content.SharedPreferences): Int {
    if (card.id == 0) return 0
    val key = "card_design_${card.id}"
    if (sharedPrefs.contains(key)) {
        return sharedPrefs.getInt(key, 0)
    }
    // Check if there is a pending design
    if (sharedPrefs.contains("pending_card_design")) {
        val pending = sharedPrefs.getInt("pending_card_design", 0)
        sharedPrefs.edit()
            .putInt(key, pending)
            .remove("pending_card_design")
            .apply()
        return pending
    }
    // Fallback: use id % 3
    return card.id % 3
}

private fun getCardBrush(designIndex: Int): Brush {
    return when (designIndex) {
        1 -> Brush.linearGradient(
            listOf(
                Color(0xFF7D56F4), // Purple
                Color(0xFF28D0A0), // Teal/Green
                Color(0xFF1E3C72)  // Blue
            )
        )
        2 -> Brush.linearGradient(
            listOf(
                Color(0xFF11998E), // Emerald dark
                Color(0xFF38EF7D)  // Emerald light
            )
        )
        else -> Brush.linearGradient(
            listOf(
                Color(0xFF171717),
                Color(0xFF2D3038)
            )
        )
    }
}
