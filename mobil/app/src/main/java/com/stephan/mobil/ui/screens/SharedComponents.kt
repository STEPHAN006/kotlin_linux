package com.stephan.mobil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.ui.theme.*
import com.stephan.mobil.ui.theme.LocalDarkMode

@Composable
fun PremiumInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    icon: ImageVector
) {
    val darkMode = LocalDarkMode.current
    val fieldBg = if (darkMode) BgSurfaceHigh else LightBackground
    val textColor = if (darkMode) TextPrimary else BgSurface
    val borderColor = if (darkMode) BgSurfaceTop else LineColor
    val labelColor = if (darkMode) TextSecondary else Color(0xFF737780)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(fieldBg, RoundedCornerShape(14.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = BrandPrimary,
                unfocusedBorderColor = borderColor,
                cursorColor = BrandPrimary,
                focusedLeadingIconColor = BrandPrimary,
                unfocusedLeadingIconColor = BrandPrimary,
                focusedContainerColor = fieldBg,
                unfocusedContainerColor = fieldBg
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )
    }
}
