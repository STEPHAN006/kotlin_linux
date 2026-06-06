package com.stephan.mobil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.data.model.Card
import com.stephan.mobil.data.model.Transaction
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel

private val CardBg = Color(0xFF17181C)
private val CardInk = Color(0xFF17181C)
private val CardMuted = Color(0xFF8B8F98)
private val CardLine = Color(0xFFF0F1F3)
private val CardSoft = Color(0xFFF4F5F7)

// Card gradients matching the image (dark → purple → teal)
private val cardGradients = listOf(
    listOf(Color(0xFF0D0D0D), Color(0xFF3B1F6E), Color(0xFF00BFA5)),
    listOf(Color(0xFF0D0D0D), Color(0xFF1A237E), Color(0xFF00897B)),
    listOf(Color(0xFF1A0533), Color(0xFF6A1B9A), Color(0xFF00ACC1)),
    listOf(Color(0xFF0D1B2A), Color(0xFF1565C0), Color(0xFF00BCD4)),
)

@Composable
fun CardsScreenPremium(
    state: BankUiState,
    vm: BankViewModel,
    darkMode: Boolean = false
) {
    val cards = state.cards
    var selectedIndex by remember { mutableStateOf(0) }
    val listState = rememberLazyListState()

    val bg = if (darkMode) Color(0xFF101114) else Color.White
    val ink = if (darkMode) Color.White else CardInk

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().background(bg).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Cartes", color = ink, fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Wallet, null, tint = ink, modifier = Modifier.size(24.dp))
                }
                IconButton(
                    onClick = {
                        state.balance.accounts.firstOrNull()?.let { vm.createCard(it.id, 5_000_000.0) }
                    }
                ) {
                    Icon(Icons.Default.Add, null, tint = ink, modifier = Modifier.size(24.dp))
                }
            }
        }

        // ── Card visual ─────────────────────────────────────────────────
        item {
            if (cards.isEmpty()) {
                EmptyCardPlaceholder(
                    onAdd = { state.balance.accounts.firstOrNull()?.let { vm.createCard(it.id, 5_000_000.0) } },
                    darkMode = darkMode
                )
            } else {
                val card = cards.getOrNull(selectedIndex) ?: cards.first()
                val gradient = cardGradients[selectedIndex % cardGradients.size]

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    PremiumCardVisual(
                        card = card,
                        gradient = gradient,
                        isBlocked = card.isBlocked
                    )

                    // Dots indicator
                    if (cards.size > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            cards.forEachIndexed { i, _ ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(if (i == selectedIndex) 20.dp else 7.dp, 7.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (i == selectedIndex) CardInk
                                            else CardLine
                                        )
                                        .clickable { selectedIndex = i }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Action buttons ───────────────────────────────────────────────
        if (cards.isNotEmpty()) {
            item {
                val card = cards.getOrNull(selectedIndex) ?: cards.first()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CardAction(Icons.Default.RemoveRedEye, "Voir", ink) {}
                    CardAction(
                        if (card.isBlocked) Icons.Default.PlayArrow else Icons.Default.AcUnit,
                        if (card.isBlocked) "Débloquer" else "Bloquer",
                        if (card.isBlocked) Color(0xFF16A34A) else Color(0xFF3B82F6)
                    ) { vm.toggleCard(card.id) }
                    CardAction(Icons.Default.Tune, "Limite", ink) {}
                    CardAction(Icons.Default.Settings, "Paramètre", ink) {}
                }
            }

            // ── Google Wallet button ─────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFF1A1A1A))
                        .clickable { }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Google Wallet icon (colored squares)
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(Modifier.size(10.dp).background(Color(0xFF4285F4), RoundedCornerShape(2.dp)))
                            Box(Modifier.size(10.dp).background(Color(0xFFEA4335), RoundedCornerShape(2.dp)))
                            Box(Modifier.size(10.dp).background(Color(0xFFFBBC04), RoundedCornerShape(2.dp)))
                            Box(Modifier.size(10.dp).background(Color(0xFF34A853), RoundedCornerShape(2.dp)))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Add to", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text("Google Wallet", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }

        // ── Transactions header ──────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transactions", color = ink, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(Icons.Default.BarChart, null, tint = CardMuted, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreHoriz, null, tint = CardMuted, modifier = Modifier.size(20.dp))
                }
            }
        }

        // ── Transaction list ─────────────────────────────────────────────
        if (state.transactions.isEmpty()) {
            item {
                // Mock transactions like the image
                val mockTxns = listOf(
                    Triple("ANTHROPIC* CLAUDE SUB", "-20,00 USD", "2026-06-03"),
                    Triple("CLAUDE.AI SUBSCRIPTION", "-20,00 USD", "2026-04-15"),
                    Triple("Achat par carte", "-10,00 USD", "2026-03-07"),
                )
                var lastDate = ""
                mockTxns.forEach { (title, amount, date) ->
                    if (date != lastDate) {
                        lastDate = date
                        Text(
                            formatDateHeader(date),
                            color = CardMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                    CardTransactionRow(
                        title = title,
                        subtitle = "•• 1000 · $date 14:01:20",
                        amount = amount,
                        status = "Autorisé",
                        ink = ink
                    )
                }
            }
        } else {
            var lastDate = ""
            itemsIndexed(state.transactions) { _, txn ->
                val dateStr = txn.createdAt?.take(10) ?: ""
                if (dateStr != lastDate) {
                    lastDate = dateStr
                    Text(
                        formatDateHeader(dateStr),
                        color = CardMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }
                val amount = (if (txn.isCredit) "+" else "-") + "%,.0f MGA".format(txn.amount).replace(",", " ")
                CardTransactionRow(
                    title = txn.description ?: "Opération SCpay",
                    subtitle = "${txn.category ?: "—"} · ${txn.createdAt ?: ""}",
                    amount = amount,
                    status = if (txn.isCredit) "Crédit" else "Débit",
                    ink = ink
                )
            }
        }
    }
}

@Composable
private fun PremiumCardVisual(card: Card, gradient: List<Color>, isBlocked: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(elevation = 20.dp, shape = RoundedCornerShape(24.dp), ambientColor = gradient.last().copy(0.4f), spotColor = gradient.last().copy(0.4f))
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(gradient))
    ) {
        // Frosted glass circle decoration
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 160.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = 220.dp, y = 30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: brand + logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(Color.White.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("S", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("SCpay", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                // Solana-style stacked bars icon
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.End) {
                    repeat(3) { i ->
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .width(if (i == 1) 20.dp else 28.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White)
                        )
                    }
                }
            }

            // Blocked overlay
            if (isBlocked) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Red.copy(0.25f))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("BLOQUÉE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }

            // Bottom row: number + VISA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "•• ${card.lastFour}",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )
                Text(
                    "VISA",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyCardPlaceholder(onAdd: () -> Unit, darkMode: Boolean) {
    val ink = if (darkMode) Color.White else CardInk
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (darkMode) Color(0xFF1A1D27) else CardSoft)
            .clickable(onClick = onAdd),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AddCard, null, tint = CardMuted, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text("Créer une carte virtuelle", color = ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("Appuyez pour ajouter", color = CardMuted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun CardAction(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(CardSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = CardInk, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CardTransactionRow(title: String, subtitle: String, amount: String, status: String, ink: Color) {
    val isCredit = amount.startsWith("+")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.GridView, null, tint = CardMuted, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = CardMuted, fontSize = 12.sp, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(amount, color = if (isCredit) Color(0xFF16A34A) else ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(status, color = CardMuted, fontSize = 12.sp)
        }
    }
    Divider(modifier = Modifier.padding(horizontal = 20.dp), color = CardLine, thickness = 0.5.dp)
}

private fun formatDateHeader(dateStr: String): String {
    return try {
        val parts = dateStr.split("-")
        if (parts.size < 3) return dateStr
        val months = listOf("", "janv.", "févr.", "mars", "avr.", "mai", "juin", "juil.", "août", "sept.", "oct.", "nov.", "déc.")
        val days = listOf("dim.", "lun.", "mar.", "mer.", "jeu.", "ven.", "sam.")
        val month = months.getOrNull(parts[1].toIntOrNull() ?: 0) ?: ""
        "${parts[2]} $month"
    } catch (_: Exception) { dateStr }
}
