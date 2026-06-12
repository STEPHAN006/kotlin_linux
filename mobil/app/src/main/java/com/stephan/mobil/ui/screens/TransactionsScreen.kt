package com.stephan.mobil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.data.model.Transaction
import com.stephan.mobil.ui.theme.*
import com.stephan.mobil.ui.theme.LocalDarkMode
import com.stephan.mobil.ui.viewmodel.BankUiState

@Composable
fun TransactionsScreen(state: BankUiState) {
    val darkMode = LocalDarkMode.current
    val brand = LocalBrandColor.current
    var filterMode by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var showChart by remember { mutableStateOf(false) }
    var selectedTxn by remember { mutableStateOf<Transaction?>(null) }

    if (selectedTxn != null) {
        TransactionDetailSheet(txn = selectedTxn!!, onDismiss = { selectedTxn = null })
        return
    }

    val pageBg = if (darkMode) BgBase else Color.White
    val ink = if (darkMode) TextPrimary else Color(0xFF17181C)
    val muted = if (darkMode) TextSecondary else Color(0xFF737780)
    val fieldBg = if (darkMode) BgSurfaceElevated else LightBackground
    val chipBg = if (darkMode) BgSurfaceElevated else LightBackground
    val chipSelectedBg = if (darkMode) BgSurfaceHigh else Color(0xFFEDEDEF)
    val border = if (darkMode) BgSurfaceTop else LineColor

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
            .background(pageBg)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Historique",
                color = ink,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showChart = !showChart }) {
                Icon(
                    imageVector = if (showChart) Icons.Default.ListAlt else Icons.Default.BarChart,
                    contentDescription = "Graphique",
                    tint = brand
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Premium Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher une transaction...", color = muted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = muted) },
            modifier = Modifier
                .fillMaxWidth()
                .background(fieldBg, RoundedCornerShape(14.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ink,
                unfocusedTextColor = ink,
                focusedBorderColor = brand.copy(alpha = 0.8f),
                unfocusedBorderColor = border,
                cursorColor = brand,
                focusedContainerColor = fieldBg,
                unfocusedContainerColor = fieldBg
            ),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Spending chart
        if (showChart) {
            SpendingChart(transactions = state.transactions)
            Spacer(modifier = Modifier.height(16.dp))
        }

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
                            if (selected) chipSelectedBg else chipBg,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) brand.copy(alpha = 0.6f) else border,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { filterMode = item.first }
                        .heightIn(min = 48.dp)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.second,
                        color = if (selected) brand else ink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

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
                    color = muted,
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
