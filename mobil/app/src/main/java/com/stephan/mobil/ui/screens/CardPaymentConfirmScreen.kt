package com.stephan.mobil.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.stephan.mobil.data.model.PendingCardPayment
import com.stephan.mobil.ui.viewmodel.BankViewModel
import com.stephan.mobil.ui.theme.*

@Composable
fun CardPaymentConfirmScreen(
    payment: PendingCardPayment,
    vm: BankViewModel
) {
    val context = LocalContext.current
    val darkMode = remember {
        context.getSharedPreferences("scpay_app", android.content.Context.MODE_PRIVATE)
            .getBoolean("dark_mode", false)
    }

    val cardBg       = if (darkMode) Color(0xFF17181C) else Color.White
    val amountBg     = if (darkMode) Color(0xFF1E1F24) else Color(0xFFF4F5F7)
    val iconBg       = if (darkMode) Color(0xFF1E1F24) else Color(0xFFFFEBEF)
    val ink          = if (darkMode) Color.White else Color(0xFF17181C)
    val muted        = if (darkMode) Color.White.copy(alpha = 0.5f) else Color(0xFF8B8F98)
    val subtle       = if (darkMode) Color.White.copy(alpha = 0.3f) else Color(0xFFB0B4BC)
    val amountSub    = if (darkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF6B7280)
    val cardLabel    = if (darkMode) Color.White.copy(alpha = 0.4f) else Color(0xFF9CA3AF)
    val cardValue    = if (darkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF4B5563)
    val outlineBorder = if (darkMode) Color.White.copy(alpha = 0.2f) else Color(0xFF17181C).copy(alpha = 0.15f)

    var confirmed by remember { mutableStateOf<Boolean?>(null) }
    var loading by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = confirmed == null,
                enter = fadeIn() + slideInVertically { it / 3 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(cardBg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top gradient bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Brush.horizontalGradient(listOf(Color(0xFF1C1C1C), Color(0xFF282A2D))))
                    )

                    Spacer(Modifier.height(28.dp))

                    // Icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Confirmation requise",
                        color = muted,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        payment.merchant,
                        color = ink,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(24.dp))

                    // Amount
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(amountBg)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                String.format("%,.0f", payment.amount).replace(",", " ") + " MGA",
                                color = ink,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                payment.product,
                                color = amountSub,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Card info
                    payment.cardMasked?.let {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Carte ", color = cardLabel, fontSize = 13.sp)
                            Text(it, color = cardValue, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Decline
                        OutlinedButton(
                            onClick = {
                                loading = true
                                confirmed = false
                                vm.declineCardPayment(payment.reference)
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ink),
                            border = androidx.compose.foundation.BorderStroke(1.dp, outlineBorder),
                            enabled = !loading
                        ) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Refuser", fontWeight = FontWeight.SemiBold)
                        }

                        // Confirm
                        Button(
                            onClick = {
                                loading = true
                                confirmed = true
                                vm.confirmCardPayment(payment.reference)
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            enabled = !loading
                        ) {
                            if (loading && confirmed == true) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Confirmer", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Ce paiement sera débité de votre compte",
                        color = subtle,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}
