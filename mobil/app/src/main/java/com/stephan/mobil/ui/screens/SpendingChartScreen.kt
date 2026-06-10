package com.stephan.mobil.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.data.model.Transaction
import kotlin.math.abs

private val chartColors = listOf(
    Color(0xFFD92C55),
    Color(0xFF2196F3),
    Color(0xFF4CAF50),
    Color(0xFFFF9800),
    Color(0xFF9C27B0),
    Color(0xFF00BCD4),
    Color(0xFF795548),
)

private val categoryLabels = mapOf(
    "groceries"    to "Alimentation",
    "transport"    to "Transport",
    "salary"       to "Salaire",
    "refund"       to "Remboursement",
    "transfer_out" to "Virement sortant",
    "transfer_in"  to "Virement entrant",
    "other"        to "Autre",
)

@Composable
fun SpendingChart(transactions: List<Transaction>, darkMode: Boolean = false) {
    val ink   = if (darkMode) Color.White else Color(0xFF17181C)
    val bg    = if (darkMode) Color(0xFF1E1F24) else Color.White
    val muted = Color(0xFF8B8F98)

    // Only debit transactions for spending chart
    val debits = transactions.filter { !it.isCredit && it.amount > 0 }

    val byCategory = remember(debits) {
        debits.groupBy { it.category ?: "other" }
              .mapValues { (_, txns) -> txns.sumOf { abs(it.amount) } }
              .entries.sortedByDescending { it.value }
              .take(7)
    }

    val total = byCategory.sumOf { it.value }.takeIf { it > 0 } ?: 1.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(20.dp)
    ) {
        Text(
            text = "Dépenses par catégorie",
            color = ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${debits.size} transaction${if (debits.size > 1) "s" else ""}",
            color = muted,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (byCategory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucune dépense à afficher", color = muted, fontSize = 13.sp)
            }
            return@Column
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donut chart
            DonutChart(
                slices = byCategory.map { (cat, amount) -> amount / total },
                colors = chartColors,
                modifier = Modifier.size(140.dp)
            )

            Spacer(Modifier.width(20.dp))

            // Legend
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                byCategory.forEachIndexed { index, (cat, amount) ->
                    val color  = chartColors[index % chartColors.size]
                    val label  = categoryLabels[cat] ?: cat.replaceFirstChar { it.uppercase() }
                    val pct    = (amount / total * 100).toInt()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = label,
                                color = ink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$pct%  •  ${formatAmount(amount)} MGA",
                                color = muted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    slices: List<Double>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke       = Stroke(width = size.minDimension * 0.18f)
        val inset        = stroke.width / 2f
        var startAngle   = -90f

        slices.forEachIndexed { i, fraction ->
            val sweep = (fraction * 360f).toFloat()
            drawArc(
                color      = colors[i % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep - 2f,
                useCenter  = false,
                style      = stroke,
                topLeft    = androidx.compose.ui.geometry.Offset(inset, inset),
                size       = androidx.compose.ui.geometry.Size(
                    size.width  - inset * 2,
                    size.height - inset * 2
                )
            )
            startAngle += sweep
        }
    }
}

private fun formatAmount(amount: Double): String {
    val v = amount.toLong()
    return when {
        v >= 1_000_000 -> "${v / 1_000_000}M"
        v >= 1_000     -> "${v / 1_000}k"
        else           -> "$v"
    }
}
