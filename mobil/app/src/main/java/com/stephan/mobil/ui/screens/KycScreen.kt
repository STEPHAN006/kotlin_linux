package com.stephan.mobil.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel

private val KycBg       = Color(0xFFF4F5F7)
private val KycCard     = Color.White
private val KycInk      = Color(0xFF17181C)
private val KycMuted    = Color(0xFF8B8F98)
private val KycRed      = Color(0xFFD92C55)
private val KycBorder   = Color(0xFFE5E7EB)
private val KycSuccess  = Color(0xFF10B981)
private val KycAmber    = Color(0xFFF59E0B)

@Composable
fun KycScreen(
    state: BankUiState,
    vm: BankViewModel
) {
    val kycStatus = state.user?.kycStatus ?: "none"

    when (kycStatus) {
        "pending"  -> KycPendingContent(state, vm)
        "approved" -> Unit // handled at navigation level
        else       -> KycFormContent(state, vm)
    }
}

// ─── Pending screen ─────────────────────────────────────────────────────────

@Composable
private fun KycPendingContent(state: BankUiState, vm: BankViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KycBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(KycAmber.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    tint = KycAmber,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Vérification en cours",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = KycInk,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Vos documents ont bien été reçus. Notre équipe vérifie votre identité, généralement sous 24h.",
                fontSize = 14.sp,
                color = KycMuted,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(32.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KycCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, KycBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    KycStep(1, "Documents soumis", true)
                    KycStep(2, "Vérification en cours…", false, inProgress = true)
                    KycStep(3, "Accès complet activé", false)
                }
            }
            Spacer(Modifier.height(32.dp))
            OutlinedButton(
                onClick = { vm.logout() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KycMuted)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Se déconnecter", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun KycStep(number: Int, label: String, done: Boolean, inProgress: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    when {
                        done       -> KycSuccess.copy(alpha = 0.12f)
                        inProgress -> KycAmber.copy(alpha = 0.12f)
                        else       -> Color(0xFFF3F4F6)
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                done -> Icon(Icons.Default.Check, contentDescription = null, tint = KycSuccess, modifier = Modifier.size(16.dp))
                inProgress -> CircularProgressIndicator(color = KycAmber, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else -> Text("$number", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KycMuted)
            }
        }
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = if (inProgress) FontWeight.SemiBold else FontWeight.Normal,
            color = when {
                done -> KycSuccess
                inProgress -> KycAmber
                else -> KycMuted
            }
        )
    }
}

// ─── Submission form ─────────────────────────────────────────────────────────

@Composable
private fun KycFormContent(state: BankUiState, vm: BankViewModel) {
    val kycStatus = state.user?.kycStatus ?: "none"
    val isRejected = kycStatus == "rejected"
    var cinFullName by remember { mutableStateOf("") }
    var cinRectoUri by remember { mutableStateOf<Uri?>(null) }
    var cinVersoUri by remember { mutableStateOf<Uri?>(null) }

    val rectoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) cinRectoUri = uri
    }
    val versoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) cinVersoUri = uri
    }

    val canSubmit = cinFullName.isNotBlank() && cinRectoUri != null && cinVersoUri != null && !state.kycSubmitting

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KycBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFFB80035), KycRed))
                    )
                    .padding(top = 52.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (isRejected) "Re-soumission KYC" else "Vérification d'identité",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (isRejected) "Votre dossier a été refusé. Corrigez les informations et re-soumettez."
                        else "Pour accéder à l'application, veuillez vérifier votre identité.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rejection reason banner
                if (isRejected && !state.kycRejectionReason.isNullOrBlank()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFFCDD5), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = KycRed, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Motif du refus", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KycRed)
                                Spacer(Modifier.height(2.dp))
                                Text(state.kycRejectionReason, fontSize = 13.sp, color = Color(0xFF9B1C2E))
                            }
                        }
                    }
                }

                // Steps indicator
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = KycCard),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, KycBorder, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KycStepChip(1, "Nom", cinFullName.isNotBlank())
                        KycDivider(cinFullName.isNotBlank())
                        KycStepChip(2, "Recto", cinRectoUri != null)
                        KycDivider(cinRectoUri != null)
                        KycStepChip(3, "Verso", cinVersoUri != null)
                    }
                }

                // Name field
                KycSectionCard(
                    icon = { Icon(Icons.Default.Badge, contentDescription = null, tint = KycRed, modifier = Modifier.size(20.dp)) },
                    title = "Nom complet (tel qu'écrit sur la CIN)"
                ) {
                    OutlinedTextField(
                        value = cinFullName,
                        onValueChange = { cinFullName = it },
                        placeholder = { Text("Ex : RAKOTO Jean Pierre", color = KycMuted, fontSize = 14.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KycRed,
                            unfocusedBorderColor = KycBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // CIN Recto
                KycSectionCard(
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = KycRed, modifier = Modifier.size(20.dp)) },
                    title = "CIN Recto"
                ) {
                    KycImagePicker(
                        uri = cinRectoUri,
                        label = "Sélectionner la face avant",
                        onClick = { rectoLauncher.launch("image/*") }
                    )
                }

                // CIN Verso
                KycSectionCard(
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = KycRed, modifier = Modifier.size(20.dp)) },
                    title = "CIN Verso"
                ) {
                    KycImagePicker(
                        uri = cinVersoUri,
                        label = "Sélectionner la face arrière",
                        onClick = { versoLauncher.launch("image/*") }
                    )
                }

                // Error
                if (!state.error.isNullOrBlank()) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.ErrorOutline, null, tint = KycRed, modifier = Modifier.size(18.dp))
                            Text(state.error, fontSize = 13.sp, color = KycRed)
                        }
                    }
                }

                // Submit button
                Button(
                    onClick = {
                        if (canSubmit) {
                            vm.submitKyc(cinFullName, cinRectoUri!!, cinVersoUri!!)
                        }
                    },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KycRed,
                        disabledContainerColor = KycRed.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (state.kycSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Envoi en cours…", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Envoyer pour vérification", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Logout link
                TextButton(
                    onClick = { vm.logout() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Se déconnecter", color = KycMuted, fontSize = 13.sp)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun KycSectionCard(
    icon: @Composable () -> Unit,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = KycCard),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, KycBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                icon()
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = KycInk)
            }
            content()
        }
    }
}

@Composable
private fun KycImagePicker(uri: Uri?, label: String, onClick: () -> Unit) {
    if (uri != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, KycSuccess.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
        ) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Overlay badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(KycSuccess, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Text("Sélectionné", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            // Change overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Appuyer pour changer", fontSize = 12.sp, color = Color.White)
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(listOf(KycBorder, KycBorder)),
                    shape = RoundedCornerShape(10.dp)
                )
                .background(Color(0xFFF9FAFB))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AddAPhoto, null, tint = KycRed, modifier = Modifier.size(32.dp))
                Text(label, fontSize = 13.sp, color = KycMuted, fontWeight = FontWeight.Medium)
                Text("JPG ou PNG · max 8 Mo", fontSize = 11.sp, color = Color(0xFFBCC0C8))
            }
        }
    }
}

@Composable
private fun KycStepChip(number: Int, label: String, done: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(if (done) KycRed else Color(0xFFEEEFF1), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (done) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            else Text("$number", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KycMuted)
        }
        Text(label, fontSize = 10.sp, color = if (done) KycRed else KycMuted, fontWeight = if (done) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun KycDivider(active: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.5.dp)
            .background(if (active) KycRed.copy(alpha = 0.3f) else KycBorder)
    )
}
