package com.stephan.mobil.ui.screens

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.stephan.mobil.ui.theme.*
import com.stephan.mobil.ui.viewmodel.BankUiState

private val ChartColorInts = listOf(
    BrandPrimary, Color(0xFF5B8DEF), SemanticSuccess,
    Color(0xFFF4A428), Color(0xFFAB6FD8), Color(0xFF3EC9C9),
    Color(0xFFFF7043), Color(0xFF78909C)
).map { it.toArgb() }

@Composable
private fun SpendingChart(state: BankUiState, darkMode: Boolean) {
    var visible by remember { mutableStateOf(true) }

    val debits = state.transactions.filter { !it.isCredit && it.category != null }
    if (debits.isEmpty()) return

    val byCategory = debits
        .groupBy { it.category ?: "other" }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }
        .entries
        .sortedByDescending { it.value }
        .take(6)

    val labels = byCategory.map { (cat, _) ->
        cat.replace("_", " ").replaceFirstChar { it.uppercase() }
    }
    val entries = byCategory.mapIndexed { i, (_, amount) ->
        BarEntry(i.toFloat(), amount.toFloat())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgSurfaceElevated)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { visible = !visible },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Dépenses par catégorie",
                color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (visible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null, tint = TextSecondary
            )
        }

        if (visible && entries.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            // MPAndroidChart — HorizontalBarChart
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((entries.size * 44).dp),
                factory = { ctx ->
                    HorizontalBarChart(ctx).apply {
                        description.isEnabled = false
                        legend.isEnabled = false
                        setTouchEnabled(false)
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        animateY(600)

                        // X axis (left — categories)
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            setDrawAxisLine(false)
                            textColor = TextSecondary.toArgb()
                            textSize = 11f
                            typeface = Typeface.DEFAULT_BOLD
                            valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float) =
                                    labels.getOrElse(value.toInt()) { "" }
                            }
                            labelCount = entries.size
                        }

                        // Y axis (values)
                        axisLeft.apply {
                            setDrawGridLines(false)
                            setDrawAxisLine(false)
                            setDrawLabels(false)
                        }
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    val dataSet = BarDataSet(entries, "Dépenses").apply {
                        colors = ChartColorInts.take(entries.size)
                        setDrawValues(true)
                        valueTextColor = TextSecondary.toArgb()
                        valueTextSize = 10f
                        valueTypeface = Typeface.MONOSPACE
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float) =
                                "%,.0f".format(value).replace(",", " ")
                        }
                    }
                    chart.data = BarData(dataSet).apply { barWidth = 0.6f }
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun TransactionsScreen(state: BankUiState, darkMode: Boolean = false) {
    var filterMode by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTxn by remember { mutableStateOf<com.stephan.mobil.data.model.Transaction?>(null) }

    if (selectedTxn != null) {
        TransactionDetailScreen(txn = selectedTxn!!, onBack = { selectedTxn = null })
        return
    }

    val filteredList = remember(state.transactions, filterMode, searchQuery) {
        state.transactions.filter { txn ->
            val matchesFilter = when (filterMode) {
                "credit" -> txn.isCredit
                "debit" -> !txn.isCredit
                else -> true
            }
            val matchesSearch = txn.description?.contains(searchQuery, ignoreCase = true) == true ||
                    txn.category?.contains(searchQuery, ignoreCase = true) == true ||
                    txn.reference?.contains(searchQuery, ignoreCase = true) == true

            matchesFilter && (searchQuery.isBlank() || matchesSearch)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Historique",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Premium Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher une transaction...", color = LightSlate) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LightSlate) },
            modifier = Modifier
                .fillMaxWidth()
                .background(LightBackground, RoundedCornerShape(14.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BgSurface,
                unfocusedTextColor = BgSurface,
                focusedBorderColor = BrandPrimary.copy(alpha = 0.8f),
                unfocusedBorderColor = LineColor,
                cursorColor = BrandPrimary
            ),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Custom Filter Chips (Segmented Pills style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("all" to "Tous", "credit" to "Entrées", "debit" to "Sorties").forEach { item ->
                val selected = filterMode == item.first
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (selected) BgSurfaceHigh else LightBackground,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) BrandPrimary.copy(alpha = 0.6f) else LineColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { filterMode = item.first }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.second,
                        color = if (selected) BrandPrimary else BgSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Spending chart by category
        SpendingChart(state = state, darkMode = darkMode)
        Spacer(modifier = Modifier.height(16.dp))

        // Transaction List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune transaction correspondante.",
                    color = LightSlate,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredList) { txn ->
                    PremiumTransactionRow(txn, onClick = { selectedTxn = txn })
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
