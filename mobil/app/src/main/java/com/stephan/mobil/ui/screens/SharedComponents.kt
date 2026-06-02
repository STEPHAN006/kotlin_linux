package com.stephan.mobil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.ui.theme.LightSlate
import com.stephan.mobil.ui.theme.NeonEmerald

@Composable
fun PremiumInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = LightSlate,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = { Icon(icon, contentDescription = null, tint = LightSlate, modifier = Modifier.size(20.dp)) },
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
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )
    }
}
