package com.stephan.mobil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.security.SecurityUtil
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel

private val SettingsBg   = Color(0xFFF4F5F7)
private val SettingsCard = Color.White
private val SettingsInk  = Color(0xFF17181C)
private val SettingsMuted = Color(0xFF8B8F98)
private val SettingsLine  = Color(0xFFECEEF2)
private val SettingsRed   = Color(0xFFD92C55)

@Composable
fun SettingsScreen(
    state: BankUiState,
    vm: BankViewModel,
    darkMode: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var biometricEnabled by remember { mutableStateOf(SecurityUtil.isBiometricEnabled(context)) }
    var notifTransactions by remember { mutableStateOf(true) }
    var notifSecurity by remember { mutableStateOf(true) }
    var notifMarketing by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    val bg = if (darkMode) Color(0xFF101114) else SettingsBg
    val cardBg = if (darkMode) Color(0xFF1E1F24) else SettingsCard
    val ink = if (darkMode) Color.White else SettingsInk

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Déconnexion", fontWeight = FontWeight.Bold) },
            text = { Text("Voulez-vous vraiment vous déconnecter ?") },
            confirmButton = {
                TextButton(onClick = { vm.logout(); showLogoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = SettingsRed)) {
                    Text("Déconnecter", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Annuler") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ink)
                }
                Text("Paramètres", color = ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        // User card
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(SettingsRed, Color(0xFFFF6B8A))))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (state.user?.name?.take(2) ?: "SC").uppercase(),
                            color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(state.user?.name ?: "Client SCpay", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(state.user?.email ?: "—", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Text(state.user?.phone ?: "—", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Edit, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                }
            }
        }

        // Security section
        item {
            SettingsSection(title = "Sécurité", cardBg = cardBg, ink = ink) {
                SettingsToggleRow(
                    icon = Icons.Default.Fingerprint,
                    label = "Biométrique",
                    subtitle = "Empreinte digitale / Face ID",
                    checked = biometricEnabled,
                    onCheckedChange = {
                        biometricEnabled = it
                        SecurityUtil.setBiometricEnabled(context, it)
                    },
                    ink = ink
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.Lock,
                    label = "Changer le code PIN",
                    subtitle = "Modifier votre PIN de sécurité",
                    ink = ink,
                    onClick = { vm.notify("Fonctionnalité changement PIN disponible dans la prochaine version") }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.Security,
                    label = "Authentification 2FA",
                    subtitle = "Double authentification par email",
                    ink = ink,
                    onClick = { vm.notify("2FA: vérification par email activée sur les gros virements") }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.History,
                    label = "Journal d'activité",
                    subtitle = "Voir les connexions récentes",
                    ink = ink,
                    onClick = { vm.notify("Journal: dernière connexion depuis ce device") }
                )
            }
        }

        // Notifications section
        item {
            SettingsSection(title = "Notifications", cardBg = cardBg, ink = ink) {
                SettingsToggleRow(
                    icon = Icons.Default.NotificationsNone,
                    label = "Transactions",
                    subtitle = "Alertes à chaque paiement",
                    checked = notifTransactions,
                    onCheckedChange = { notifTransactions = it },
                    ink = ink
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Default.Warning,
                    label = "Alertes sécurité",
                    subtitle = "Connexions suspectes, fraude",
                    checked = notifSecurity,
                    onCheckedChange = { notifSecurity = it },
                    ink = ink
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Default.Campaign,
                    label = "Offres & promotions",
                    subtitle = "Nouveaux produits SCpay",
                    checked = notifMarketing,
                    onCheckedChange = { notifMarketing = it },
                    ink = ink
                )
            }
        }

        // Apparence section
        item {
            SettingsSection(title = "Apparence", cardBg = cardBg, ink = ink) {
                SettingsToggleRow(
                    icon = Icons.Default.DarkMode,
                    label = "Mode sombre",
                    subtitle = "Thème foncé pour l'application",
                    checked = darkMode,
                    onCheckedChange = { onToggleTheme() },
                    ink = ink
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Default.Dns,
                    label = "Mode démo",
                    subtitle = "Données de test sans réseau",
                    checked = state.mockMode,
                    onCheckedChange = { vm.setMockMode(it) },
                    ink = ink
                )
            }
        }

        // Compte section
        item {
            SettingsSection(title = "Compte", cardBg = cardBg, ink = ink) {
                val accountId = state.balance.accounts.firstOrNull()?.id ?: 0
                SettingsRow(
                    icon = Icons.Default.Download,
                    label = "Relevé mensuel PDF",
                    subtitle = "Télécharger votre relevé",
                    ink = ink,
                    onClick = { if (accountId > 0) vm.downloadStatement(context, accountId) else vm.notify("Aucun compte trouvé") }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.PersonAdd,
                    label = "Inviter des amis",
                    subtitle = "Gagnez jusqu'à 40% de commissions",
                    ink = ink,
                    onClick = { vm.notify("Code parrainage: SCPAY-REF-${state.user?.id ?: "0000"}") }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.Info,
                    label = "À propos de SCpay",
                    subtitle = "Version 2.0.0 — Mentions légales",
                    ink = ink,
                    onClick = { vm.notify("SCpay v2.0.0 — Application bancaire mobile sécurisée") }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.HelpOutline,
                    label = "Centre d'aide",
                    subtitle = "FAQ, guides et support",
                    ink = ink,
                    onClick = { vm.notify("Centre d'aide: faq.scpay.mg") }
                )
            }
        }

        // Logout
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEBEE),
                        contentColor = SettingsRed
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Déconnexion", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    cardBg: Color,
    ink: Color,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = title.uppercase(),
            color = Color(0xFF8B8F98),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    ink: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF4F5F7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFF8B8F98), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color(0xFF8B8F98), fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    ink: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF4F5F7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFF8B8F98), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color(0xFF8B8F98), fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFD92C55),
                uncheckedThumbColor = Color(0xFF8B8F98),
                uncheckedTrackColor = Color(0xFFECEEF2)
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    Divider(
        modifier = Modifier.padding(start = 66.dp),
        color = Color(0xFFECEEF2),
        thickness = 0.5.dp
    )
}
