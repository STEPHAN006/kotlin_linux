package com.stephan.mobil.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephan.mobil.BuildConfig
import com.stephan.mobil.data.api.ApiClient
import com.stephan.mobil.security.SecurityUtil
import com.stephan.mobil.ui.theme.*
import com.stephan.mobil.ui.theme.LocalDarkMode
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel
import kotlinx.coroutines.delay

private val SettingsBg    = Color(0xFFF4F5F7)
private val SettingsCard  = Color.White
private val SettingsInk   = Color(0xFF17181C)
private val SettingsMuted = Color(0xFF8B8F98)
private val SettingsRed   = Color(0xFFE2E2E5)

private enum class PinStep { VERIFY_OLD, ENTER_NEW, CONFIRM_NEW }

@Composable
fun SettingsScreen(
    state: BankUiState,
    vm: BankViewModel,
    onToggleTheme: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val darkMode = LocalDarkMode.current
    val context = LocalContext.current
    val notifPrefs = remember { context.getSharedPreferences("scpay_notif", Context.MODE_PRIVATE) }

    var showEditProfile  by remember { mutableStateOf(false) }
    var showChangePin    by remember { mutableStateOf(false) }
    var showAbout        by remember { mutableStateOf(false) }
    var showHelp         by remember { mutableStateOf(false) }
    var showActivityLog  by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    var biometricEnabled   by remember { mutableStateOf(SecurityUtil.isBiometricEnabled(context)) }
    var notifTransactions  by remember { mutableStateOf(notifPrefs.getBoolean("transactions", true)) }
    var notifSecurity      by remember { mutableStateOf(notifPrefs.getBoolean("security", true)) }
    var notifMarketing     by remember { mutableStateOf(notifPrefs.getBoolean("marketing", false)) }

    var serverUrl by remember { mutableStateOf(ApiClient.getBaseUrl(context)) }
    var urlApplied by remember { mutableStateOf(false) }

    val bg     = if (darkMode) Color(0xFF101114) else SettingsBg
    val cardBg = if (darkMode) Color(0xFF1E1F24) else SettingsCard
    val ink    = if (darkMode) Color.White else SettingsInk

    // Full-screen overlays — rendered before rest to take over the screen
    if (showEditProfile) {
        EditProfileOverlay(state = state, vm = vm, onBack = { showEditProfile = false })
        return
    }
    if (showChangePin) {
        ChangePinFlow(onBack = { showChangePin = false })
        return
    }

    // Dialogs
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Déconnexion", fontWeight = FontWeight.Bold) },
            text = { Text("Voulez-vous vraiment vous déconnecter ?") },
            confirmButton = {
                TextButton(
                    onClick = { vm.logout(); showLogoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = SettingsRed)
                ) { Text("Déconnecter", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Annuler") } }
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("À propos de SCpay", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Version 2.0.0", fontWeight = FontWeight.SemiBold)
                    Text("SCpay est une application bancaire mobile sécurisée conçue pour Madagascar.")
                    Spacer(Modifier.height(4.dp))
                    Text("Fonctionnalités :", fontWeight = FontWeight.SemiBold)
                    Text("• Virements instantanés avec OTP\n• Crypto-monnaies intégrées\n• Cartes virtuelles\n• Vérification KYC\n• Support client 24/7")
                    Spacer(Modifier.height(4.dp))
                    Text("Équipe :", fontWeight = FontWeight.SemiBold)
                    Text("• AROVANA Rakotorahalahy Stephan\n• RAMAHANDRIVOHITRA Ny Ankasitrahana Richie", fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("TEAM STEPHAN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SettingsMuted)
                    Spacer(Modifier.height(4.dp))
                    Text("© 2026 SCpay Madagascar. Tous droits réservés.", fontSize = 11.sp, color = SettingsMuted)
                }
            },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("Fermer") } }
        )
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Centre d'aide", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HelpItem("Comment faire un virement ?", "Onglet Envoyer → saisir le numéro de compte et le montant. Les virements > 500 000 MGA nécessitent un OTP par email.")
                    HelpItem("Comment activer la biométrie ?", "Paramètres → Sécurité → activer le toggle Biométrique.")
                    HelpItem("Comment changer mon PIN ?", "Paramètres → Sécurité → Changer le code PIN.")
                    HelpItem("Où sont mes relevés ?", "Paramètres → Compte → Relevé mensuel PDF.")
                    HelpItem("Contacter le support ?", "Onglet Accueil → icône chat support en bas.")
                }
            },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text("Fermer") } }
        )
    }

    if (showActivityLog) {
        AlertDialog(
            onDismissRequest = { showActivityLog = false },
            title = { Text("Journal d'activité", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Ce device (Android)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Session active — Aujourd'hui", fontSize = 12.sp, color = SettingsMuted)
                        }
                    }
                    HorizontalDivider(color = Color(0xFFECEEF2))
                    Text("Dernières transactions", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    if (state.transactions.isEmpty()) {
                        Text("Aucune activité récente", color = SettingsMuted, fontSize = 13.sp)
                    } else {
                        state.transactions.take(5).forEach { tx ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = tx.description ?: if (tx.isCredit) "Réception" else "Envoi",
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${if (tx.isCredit) "+" else "-"}${String.format("%,.0f", tx.amount)} MGA",
                                    fontSize = 12.sp,
                                    color = if (tx.isCredit) Color(0xFF4CAF50) else SettingsRed,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showActivityLog = false }) { Text("Fermer") } }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(bg),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp).statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ink)
                }
                Text("Paramètres", color = ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        // User card — tap to edit profile
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF1E2022), Color(0xFF282A2D))))
                    .clickable { showEditProfile = true }
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (state.user?.name?.take(2) ?: "SC").uppercase(),
                            color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(state.user?.name ?: "Client SCpay", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(state.user?.email ?: "—", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Text(state.user?.phone ?: "—", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Edit, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(20.dp))
                        Text("Modifier", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                    }
                }
            }
        }

        item {
            SettingsSection(title = "Sécurité", cardBg = cardBg, ink = ink) {
                SettingsToggleRow(
                    icon = Icons.Default.Fingerprint,
                    label = "Biométrique",
                    subtitle = "Empreinte digitale / Face ID",
                    checked = biometricEnabled,
                    onCheckedChange = { biometricEnabled = it; SecurityUtil.setBiometricEnabled(context, it) },
                    ink = ink
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.Lock,
                    label = if (SecurityUtil.hasPinCode(context)) "Changer le code PIN" else "Définir un code PIN",
                    subtitle = if (SecurityUtil.hasPinCode(context)) "Modifier votre PIN de sécurité" else "Protéger l'accès à l'application",
                    ink = ink,
                    onClick = { showChangePin = true }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.Security,
                    label = "Authentification 2FA",
                    subtitle = "OTP email activé sur les virements > 500 000 MGA",
                    ink = ink,
                    onClick = { vm.notify("2FA active — Chaque virement important déclenche un code OTP par email") }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.History,
                    label = "Journal d'activité",
                    subtitle = "Connexions et transactions récentes",
                    ink = ink,
                    onClick = { showActivityLog = true }
                )
            }
        }

        item {
            SettingsSection(title = "Notifications", cardBg = cardBg, ink = ink) {
                SettingsToggleRow(
                    icon = Icons.Default.NotificationsNone,
                    label = "Transactions",
                    subtitle = "Alertes à chaque paiement",
                    checked = notifTransactions,
                    onCheckedChange = { notifTransactions = it; notifPrefs.edit().putBoolean("transactions", it).apply() },
                    ink = ink
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Default.Warning,
                    label = "Alertes sécurité",
                    subtitle = "Connexions suspectes, fraude",
                    checked = notifSecurity,
                    onCheckedChange = { notifSecurity = it; notifPrefs.edit().putBoolean("security", it).apply() },
                    ink = ink
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Default.Campaign,
                    label = "Offres & promotions",
                    subtitle = "Nouveaux produits SCpay",
                    checked = notifMarketing,
                    onCheckedChange = { notifMarketing = it; notifPrefs.edit().putBoolean("marketing", it).apply() },
                    ink = ink
                )
            }
        }

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
                if (BuildConfig.DEBUG) {
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
        }

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
                    onClick = {
                        val code = "SCPAY-REF-${state.user?.id ?: "0000"}"
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Rejoins SCpay avec mon code : $code\n\nGère tes finances depuis ton mobile avec l'app SCpay Madagascar !")
                            putExtra(Intent.EXTRA_SUBJECT, "Invitation SCpay")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Inviter via"))
                    }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.Info,
                    label = "À propos de SCpay",
                    subtitle = "Version 2.0.0 — Mentions légales",
                    ink = ink,
                    onClick = { showAbout = true }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Default.HelpOutline,
                    label = "Centre d'aide",
                    subtitle = "FAQ, guides et support",
                    ink = ink,
                    onClick = { showHelp = true }
                )
            }
        }

        item {
            SettingsSection(title = "Serveur", cardBg = cardBg, ink = ink) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it; urlApplied = false },
                        label = { Text("URL du serveur Laravel", color = SettingsMuted, fontSize = 12.sp) },
                        placeholder = { Text("http://192.168.x.x:8000/api/", color = SettingsMuted, fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ink, unfocusedTextColor = ink,
                            focusedBorderColor = if (darkMode) Color.White else Color(0xFF17181C),
                            unfocusedBorderColor = SettingsMuted.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )
                    Button(
                        onClick = {
                            val normalized = serverUrl.trim().let {
                                if (!it.startsWith("http")) "http://$it" else it
                            }.trimEnd('/') + "/"
                            serverUrl = normalized
                            ApiClient.saveCustomUrl(context, normalized)
                            urlApplied = true
                            (context as? Activity)?.recreate()
                        },
                        enabled = serverUrl.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (darkMode) Color.White else Color(0xFF17181C)
                        )
                    ) {
                        Icon(Icons.Default.Wifi, null, modifier = Modifier.size(16.dp),
                            tint = if (darkMode) Color(0xFF17181C) else Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (urlApplied) "Appliqué ✓" else "Appliquer et redémarrer",
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = if (darkMode) Color(0xFF17181C) else Color.White
                        )
                    }
                }
            }
        }

        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = SettingsRed),
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

// ── Edit Profile ─────────────────────────────────────────────────────────────

@Composable
fun EditProfileOverlay(
    state: BankUiState,
    vm: BankViewModel,
    onBack: () -> Unit
) {
    val darkMode = LocalDarkMode.current
    val bg          = if (darkMode) Color(0xFF101114) else Color(0xFFF4F5F7)
    val cardBg      = if (darkMode) Color(0xFF1E1F24) else Color.White
    val ink         = if (darkMode) Color.White else SettingsInk
    val borderColor = if (darkMode) Color(0xFF2A2D3A) else Color(0xFFECEEF2)

    var name     by remember { mutableStateOf(state.user?.name ?: "") }
    var email    by remember { mutableStateOf(state.user?.email ?: "") }
    var phone    by remember { mutableStateOf(state.user?.phone ?: "") }
    var password by remember { mutableStateOf("") }
    var pwVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.message) {
        if (state.message?.contains("mis à jour") == true) {
            delay(500)
            onBack()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(bg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp).statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ink)
            }
            Text("Modifier le profil", color = ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cardBg).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Informations personnelles", color = ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    ProfileTextField("Nom complet", name, { name = it }, Icons.Default.Person, KeyboardType.Text, ink, borderColor)
                    ProfileTextField("Adresse email", email, { email = it }, Icons.Default.Email, KeyboardType.Email, ink, borderColor)
                    ProfileTextField("Téléphone", phone, { phone = it }, Icons.Default.Phone, KeyboardType.Phone, ink, borderColor)
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cardBg).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Confirmation", color = ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("Votre mot de passe actuel est requis pour sauvegarder.", color = SettingsMuted, fontSize = 12.sp)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; localError = null },
                        label = { Text("Mot de passe actuel") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { pwVisible = !pwVisible }) {
                                Icon(if (pwVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = localError != null,
                        supportingText = localError?.let { { Text(it, color = SettingsRed) } }
                    )
                }
            }

            if (state.error != null) {
                item {
                    Text(state.error, color = SettingsRed, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }

            item {
                Button(
                    onClick = {
                        if (password.isBlank()) { localError = "Mot de passe requis"; return@Button }
                        val newName  = name.trim().takeIf { it != state.user?.name && it.isNotBlank() }
                        val newEmail = email.trim().takeIf { it != state.user?.email && it.isNotBlank() }
                        val newPhone = phone.trim().takeIf { it.isNotBlank() && it != (state.user?.phone ?: "") }
                        vm.updateProfile(password, newName, newEmail, newPhone)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SettingsRed),
                    enabled = !state.loading
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Enregistrer les modifications", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    keyboardType: KeyboardType,
    ink: Color,
    borderColor: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = SettingsMuted) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SettingsRed,
            unfocusedBorderColor = borderColor,
            focusedTextColor = ink,
            unfocusedTextColor = ink,
            cursorColor = SettingsRed
        )
    )
}

// ── Change PIN ────────────────────────────────────────────────────────────────

@Composable
fun ChangePinFlow(onBack: () -> Unit) {
    val darkMode = LocalDarkMode.current
    val brand    = LocalBrandColor.current
    val context = LocalContext.current
    val hasPin = remember { SecurityUtil.hasPinCode(context) }
    val bg  = if (darkMode) Color(0xFF0A0B0E) else Color(0xFFF4F5F7)
    val ink = if (darkMode) Color.White else Color(0xFF17181C)

    var step   by remember { mutableStateOf(if (hasPin) PinStep.VERIFY_OLD else PinStep.ENTER_NEW) }
    var pin    by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var error  by remember { mutableStateOf<String?>(null) }
    var done   by remember { mutableStateOf(false) }

    LaunchedEffect(pin) {
        if (pin.length < 4) return@LaunchedEffect
        delay(150)
        when (step) {
            PinStep.VERIFY_OLD -> {
                if (SecurityUtil.verifyPinCode(context, pin)) {
                    step = PinStep.ENTER_NEW; error = null
                } else {
                    error = "PIN incorrect, réessayez"
                }
                pin = ""
            }
            PinStep.ENTER_NEW -> {
                newPin = pin
                step = PinStep.CONFIRM_NEW
                pin = ""; error = null
            }
            PinStep.CONFIRM_NEW -> {
                if (pin == newPin) {
                    SecurityUtil.savePinCode(context, pin)
                    done = true
                } else {
                    error = "Les PIN ne correspondent pas"
                    step = PinStep.ENTER_NEW
                }
                pin = ""
            }
        }
    }

    LaunchedEffect(done) {
        if (done) { delay(900); onBack() }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = bg) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
        ) {
            Row(modifier = Modifier.padding(start = 4.dp, top = 8.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ink)
                }
            }

            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 20.dp)) {
                    Icon(
                        imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (done) Color(0xFF4CAF50) else brand,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = when {
                            done -> "PIN modifié !"
                            step == PinStep.VERIFY_OLD -> "PIN actuel"
                            step == PinStep.ENTER_NEW  -> if (hasPin) "Nouveau PIN" else "Définir votre PIN"
                            else                        -> "Confirmer le PIN"
                        },
                        color = ink,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = when {
                            done -> "Votre code PIN a été mis à jour avec succès"
                            step == PinStep.VERIFY_OLD -> "Entrez votre PIN actuel pour continuer"
                            step == PinStep.ENTER_NEW  -> "Choisissez un nouveau code à 4 chiffres"
                            else                        -> "Saisissez à nouveau votre nouveau PIN"
                        },
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    if (done) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(64.dp))
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            for (i in 0 until 4) {
                                val filled = i < pin.length
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (filled) brand else Color.DarkGray.copy(alpha = 0.5f))
                                        .border(1.dp, if (filled) brand else Color.Gray.copy(alpha = 0.3f), CircleShape)
                                )
                            }
                        }
                        if (error != null) {
                            Spacer(Modifier.height(12.dp))
                            Text(error!!, color = Color(0xFFFF5252), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (!done) {
                    CustomNumericKeypad(
                        enabled = true,
                        showBiometrics = false,
                        onKeyPress = { digit -> if (pin.length < 4) pin += digit },
                        onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                        onBiometricClick = {}
                    )
                } else {
                    Spacer(Modifier.height(200.dp))
                }
            }
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun HelpItem(question: String, answer: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(question, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(answer, fontSize = 12.sp, color = SettingsMuted)
    }
}

@Composable
private fun SettingsSection(title: String, cardBg: Color, ink: Color, content: @Composable () -> Unit) {
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
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cardBg)
        ) { content() }
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
    val darkMode = LocalDarkMode.current
    val iconBg      = if (darkMode) Color(0xFF2A2D3A) else Color(0xFFF4F5F7)
    val chevronTint = if (darkMode) Color(0xFF4B5063) else Color(0xFFD1D5DB)
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color(0xFF8B8F98), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color(0xFF8B8F98), fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = chevronTint, modifier = Modifier.size(18.dp))
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
    val darkMode = LocalDarkMode.current
    val iconBg       = if (darkMode) Color(0xFF2A2D3A) else Color(0xFFF4F5F7)
    val uncheckedBg  = if (darkMode) Color(0xFF3A3D4A) else Color(0xFFECEEF2)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconBg), contentAlignment = Alignment.Center) {
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
                checkedTrackColor = Color(0xFFE2E2E5),
                uncheckedThumbColor = Color(0xFF8B8F98),
                uncheckedTrackColor = uncheckedBg
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    val darkMode = LocalDarkMode.current
    HorizontalDivider(
        modifier = Modifier.padding(start = 66.dp),
        color = if (darkMode) Color(0xFF2A2D3A) else Color(0xFFECEEF2),
        thickness = 0.5.dp
    )
}
