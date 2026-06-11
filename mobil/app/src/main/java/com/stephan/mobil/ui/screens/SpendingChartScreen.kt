package com.stephan.mobil.ui.screens

import android.graphics.Typeface
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.stephan.mobil.data.model.Transaction
import com.stephan.mobil.ui.theme.*
import kotlin.math.abs

private val chartColors = listOf(
    Color(0xFFE2E2E5),
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
fun SpendingChart(transactions: List<Transaction>) {
    val darkMode = LocalDarkMode.current
    val ink   = if (darkMode) Color.White else Color(0xFF17181C)
    val bg    = if (darkMode) Color(0xFF1E1F24) else Color.White
    val muted = TextSecondary

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
            modifier = Modifier.padding(bottom = 16.dp)
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

        val entries = byCategory.map { (cat, amount) ->
            PieEntry(amount.toFloat(), categoryLabels[cat] ?: cat.replaceFirstChar { it.uppercase() })
        }
        val argbColors = chartColors.map { it.toArgb() }
        val bgArgb = bg.toArgb()
        val inkArgb = ink.toArgb()
        val mutedArgb = muted.toArgb()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AndroidView(
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        isDrawHoleEnabled = true
                        holeRadius = 52f
                        transparentCircleRadius = 57f
                        setHoleColor(bgArgb)
                        setTransparentCircleColor(bgArgb)
                        setTransparentCircleAlpha(110)
                        isRotationEnabled = false
                        setDrawEntryLabels(false)
                        legend.isEnabled = false
                        setCenterText("")
                        setExtraOffsets(0f, 0f, 0f, 0f)
                        setNoDataTextColor(mutedArgb)
                        animateY(800)
                    }
                },
                update = { chart ->
                    val dataSet = PieDataSet(entries, "").apply {
                        colors = argbColors
                        sliceSpace = 2f
                        selectionShift = 4f
                        valueTextSize = 0f        // hide value labels on slices
                        valueTextColor = android.graphics.Color.TRANSPARENT
                    }
                    val data = PieData(dataSet).apply {
                        setValueTextSize(0f)
                    }
                    chart.data = data
                    chart.setHoleColor(bgArgb)
                    chart.invalidate()
                },
                modifier = Modifier.size(150.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                byCategory.forEachIndexed { index, (cat, amount) ->
                    val color = chartColors[index % chartColors.size]
                    val label = categoryLabels[cat] ?: cat.replaceFirstChar { it.uppercase() }
                    val pct   = (amount / total * 100).toInt()
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
                                text = "$pct%  •  ${formatMgaAmount(amount)} MGA",
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

private fun formatMgaAmount(amount: Double): String {
    val v = amount.toLong()
    return when {
        v >= 1_000_000 -> "${v / 1_000_000}M"
        v >= 1_000     -> "${v / 1_000}k"
        else           -> "$v"
    }
}
