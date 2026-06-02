package com.stephan.mobil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.ui.theme.*
import com.stephan.mobil.ui.viewmodel.BankUiState

@Composable
fun TransactionsScreen(state: BankUiState, darkMode: Boolean = false) {
    var filterMode by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

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
            .background(if (darkMode) Color(0xFF101114) else Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Historique",
            color = if (darkMode) Color.White else Color(0xFF17181C),
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
                .background(Color(0xFFF7F8FA), RoundedCornerShape(14.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF17181C),
                unfocusedTextColor = Color(0xFF17181C),
                focusedBorderColor = Color(0xFFD92C55).copy(alpha = 0.8f),
                unfocusedBorderColor = Color(0xFFE6E8EC),
                cursorColor = Color(0xFFD92C55)
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
                            if (selected) Color(0xFFEDEDEF) else Color(0xFFF7F8FA),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) Color(0xFFD92C55).copy(alpha = 0.6f) else Color(0xFFE6E8EC),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { filterMode = item.first }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.second,
                        color = if (selected) Color(0xFFD92C55) else Color(0xFF17181C),
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
                    PremiumTransactionRow(txn)
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
