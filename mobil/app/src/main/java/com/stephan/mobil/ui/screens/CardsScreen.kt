package com.stephan.mobil.ui.screens

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

private val CardPageBg = Color.White
private val CardInk = Color(0xFF15171B)
private val CardMuted = Color(0xFF8A8E96)
private val CardSoft = Color(0xFFF4F5F7)

@Composable
fun CardsScreen(state: BankUiState, vm: BankViewModel, darkMode: Boolean = false) {
    var showChooseCard by remember { mutableStateOf(false) }

    if (showChooseCard) {
        ChooseCardScreen(
            state = state,
            vm = vm,
            darkMode = darkMode,
            onBack = { showChooseCard = false }
        )
    } else {
        CardsMainContent(
            state = state,
            vm = vm,
            darkMode = darkMode,
            onAddCard = { showChooseCard = true }
        )
    }
}

@Composable
private fun ChooseCardScreen(
    state: BankUiState,
    vm: BankViewModel,
    darkMode: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val accountId = state.balance.accounts.firstOrNull()?.id ?: 1
    val sharedPrefs = remember { context.getSharedPreferences("card_designs", Context.MODE_PRIVATE) }
    var isVirtualSelected by remember { mutableStateOf(true) }
    var selectedDesign by remember { mutableStateOf(1) } // 1 = Solana (Default in screenshot 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkMode) Color(0xFF101114) else Color.White)
            .padding(24.dp)
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
                    tint = if (darkMode) Color.White else CardInk
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Title
        Text(
            text = "Choisir une carte",
            color = if (darkMode) Color.White else CardInk,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(24.dp))

        // Tab Selector (Segmented control)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (darkMode) Color(0xFF1F2023) else Color(0xFFF1F2F5))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isVirtualSelected) {
                            if (darkMode) Color(0xFF2F3035) else Color.White
                        } else Color.Transparent
                    )
                    .clickable { isVirtualSelected = true }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Carte virtuelle",
                    color = if (darkMode) Color.White else Color.Black,
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
                            if (darkMode) Color(0xFF2F3035) else Color.White
                        } else Color.Transparent
                    )
                    .clickable { isVirtualSelected = false }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Carte physique",
                    color = if (darkMode) Color.White else Color.Black,
                    fontWeight = if (!isVirtualSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(Modifier.weight(0.1f))

        // Vertical Card Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            VerticalCardPreview(
                designIndex = selectedDesign,
                isVirtual = isVirtualSelected
            )
        }

        Spacer(Modifier.height(16.dp))

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
                color = if (darkMode) Color.White else CardInk,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Compatible avec Apple Pay et Google Pay. Accepté par plus de 130 millions de commerçants dans le monde entier.",
                color = CardMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
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
                                if (darkMode) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
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

        Spacer(Modifier.weight(0.1f))

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
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (darkMode) Color.White else Color(0xFF17181C),
                contentColor = if (darkMode) Color.Black else Color.White
            )
        ) {
            Text("Demande de carte · 10 USD", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VerticalCardPreview(designIndex: Int, isVirtual: Boolean) {
    val brush = getCardBrush(designIndex)
    Box(
        modifier = Modifier
            .width(220.dp)
            .height(350.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(brush)
            .padding(24.dp)
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

@Composable
private fun CardsMainContent(
    state: BankUiState,
    vm: BankViewModel,
    darkMode: Boolean,
    onAddCard: () -> Unit
) {
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

    // Keep active card selection in bounds
    LaunchedEffect(cards.size) {
        if (selectedCardIndex >= cards.size) {
            selectedCardIndex = maxOf(0, cards.size - 1)
        }
    }

    val card = cards.getOrNull(selectedCardIndex) ?: cards.first()
    val designIndex = getCardDesign(card, sharedPrefs)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkMode) Color(0xFF101114) else CardPageBg)
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
                    color = if (darkMode) Color.White else CardInk,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { vm.notify("Releves mensuels disponibles via l'API /statements/monthly") }) {
                    Icon(
                        Icons.Default.MailOutline,
                        null,
                        tint = if (darkMode) Color.White else CardInk,
                        modifier = Modifier.size(26.dp)
                    )
                }
                IconButton(onClick = onAddCard) {
                    Icon(
                        Icons.Default.AddCircleOutline,
                        null,
                        tint = if (darkMode) Color.White else CardInk,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Horizontal Carousel of Cards
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    itemsIndexed(cards) { index, c ->
                        val isSelected = index == selectedCardIndex
                        Box(
                            modifier = Modifier
                                .width(300.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) Color(0xFFD92C55) else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(4.dp)
                                .clickable { selectedCardIndex = index }
                        ) {
                            FlippableScpayCard(
                                card = c,
                                userName = state.user?.name ?: "AROVANA",
                                designIndex = getCardDesign(c, sharedPrefs)
                            )
                        }
                    }
                }

                if (cards.size > 1) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        cards.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == selectedCardIndex) Color(0xFFD92C55)
                                        else CardMuted.copy(alpha = 0.4f)
                                    )
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CardAction("Voir", Icons.Default.RemoveRedEye) {
                    vm.notify("Touchez la carte pour afficher le verso")
                }
                CardAction("Bloquer", Icons.Default.Lock, onClick = {
                    if (card.id != 0) vm.toggleCard(card.id)
                })
                CardAction("Limite", Icons.Default.Tune) {
                    vm.createCard(accountId, card.dailyLimit + 500_000.0)
                }
                CardAction("Paramètre", Icons.Default.Settings) {
                    vm.notify("Parametres carte: type = ${card.type}, limite = ${card.dailyLimit.toLong()} MGA")
                }
            }
        }

        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(CardInk)
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
                    color = if (darkMode) Color.White else CardInk,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ViewModule, null, tint = CardMuted)
                Spacer(Modifier.width(18.dp))
                Text("...", color = CardMuted, fontSize = 24.sp)
            }
        }

        item {
            Text("mer., avr. 15", color = CardMuted, fontSize = 17.sp)
            Spacer(Modifier.height(24.dp))
            TransactionLine("CLAUDE.AI SUBSCRIPTION", "-20,00 USD", "Refusé", false, "2026-04-15 11:56:31")
        }

        item { Spacer(Modifier.height(22.dp)) }
    }
}

@Composable
private fun FlippableScpayCard(card: Card, userName: String, designIndex: Int) {
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
            .height(200.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 16f * density
            }
            .clickable { flipped = !flipped }
    ) {
        if (showBack) {
            CardBack(card, designIndex, Modifier.graphicsLayer { rotationY = 180f })
        } else {
            CardFront(card, userName, designIndex)
        }
    }
}

@Composable
private fun CardFront(card: Card, userName: String, designIndex: Int, modifier: Modifier = Modifier) {
    val brush = getCardBrush(designIndex)
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(brush)
            .padding(24.dp)
    ) {
        Text("USD", color = Color.White, fontSize = 17.sp, modifier = Modifier.align(Alignment.TopEnd))
        Column(Modifier.align(Alignment.BottomStart)) {
            Text("•••• •••• •••• ${card.lastFour}", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(44.dp)) {
                Column {
                    Text("Valide jusqu'au", color = Color.White.copy(alpha = 0.82f), fontSize = 14.sp)
                    Text(card.expiryDate.ifBlank { "••••" }, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("CVV", color = Color.White.copy(alpha = 0.82f), fontSize = 14.sp)
                    Text("•••", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
private fun CardBack(card: Card, designIndex: Int, modifier: Modifier = Modifier) {
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
            Text("•• ${card.lastFour}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("VISA", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
private fun CardAction(label: String, icon: ImageVector, onClick: () -> Unit) {
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
                .background(CardSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = CardInk, modifier = Modifier.size(27.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(label, color = CardInk, fontSize = 14.sp)
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
