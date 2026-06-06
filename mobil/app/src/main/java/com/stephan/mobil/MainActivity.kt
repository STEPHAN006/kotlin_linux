package com.stephan.mobil

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.stephan.mobil.ui.theme.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stephan.mobil.data.api.ApiClient
import com.stephan.mobil.data.repository.BankRepository
import com.stephan.mobil.security.SecurityUtil
import com.stephan.mobil.ui.theme.MobilTheme
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel
import com.stephan.mobil.ui.viewmodel.BankViewModelFactory
import com.stephan.mobil.ui.screens.*
import com.stephan.mobil.notifications.PushNotificationWorker
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

class MainActivity : FragmentActivity() {

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not — on tente quand même */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Demande la permission POST_NOTIFICATIONS sur Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Crée le canal de notification dès le démarrage
        PushNotificationWorker.createChannel(this)

        setContent {
            MobilTheme {
                val context = LocalContext.current
                val api = remember { ApiClient.getClient(context).create(com.stephan.mobil.data.api.ApiService::class.java) }
                val repo = remember { BankRepository(api, context.applicationContext) }
                val vm: BankViewModel = viewModel(factory = BankViewModelFactory(repo))
                BankApp(vm, this)
            }
        }
    }
}

private enum class Screen(val label: String) {
    Dashboard("Accueil"),
    Transactions("Les atouts"),  // shows CardsScreenPremium
    Transfer("Envoyer"),
    Cards("Actifs"),
    Profile("Profil")
}

@Composable
private fun BankApp(vm: BankViewModel, activity: FragmentActivity) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    var unlocked by remember { mutableStateOf(!SecurityUtil.hasPinCode(context)) }

    // Splash screen while restoring session from token
    if (!state.sessionRestored) {
        Box(
            modifier = Modifier.fillMaxSize().background(BgDeep),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SCpay", color = TextPrimary, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(32.dp))
                CircularProgressIndicator(color = BrandPrimary)
            }
        }
        return
    }

    // If user is not authenticated/registered in state, show the beautiful onboarding welcome screen
    if (state.user == null) {
        WelcomeScreen(
            onLoginSubmit = { email, password -> vm.login(email, password) },
            onRegisterSubmit = { name, email, phone, password -> vm.register(name, email, phone, password) },
            isLoading = state.loading,
            errorMessage = state.error
        )
        return
    }

    // Once logged in/authenticated, handle the secure PIN lock screen
    if (!unlocked) {
        PinScreen(
            onUnlock = { unlocked = true },
            onBiometric = {
                launchBiometric(activity) { unlocked = true }
            }
        )
        return
    }

    // Main App Shell once logged in and unlocked
    MainShell(state, vm)
}

@Composable
private fun MainShell(state: BankUiState, vm: BankViewModel) {
    var selected by remember { mutableStateOf(Screen.Dashboard) }
    var darkMode by remember { mutableStateOf(true) }
    var immersive by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val shellBg = BgBase

    LaunchedEffect(state.message, state.error) {
        val msg = state.message ?: state.error
        if (msg != null) {
            snackbar.showSnackbar(msg)
            vm.consumeMessages()
        }
    }

    LaunchedEffect(selected) {
        if (selected != Screen.Dashboard) immersive = false
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (!immersive) {
                NavigationBar(
                    containerColor = BgDeep,
                    tonalElevation = 0.dp
                ) {
                    Screen.entries.forEach { screen ->
                        NavigationBarItem(
                            selected = selected == screen,
                            onClick = { selected = screen },
                            icon = { Icon(screen.icon(), contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandPrimary,
                                selectedTextColor = BrandPrimary,
                                indicatorColor = BrandPrimarySoft,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(shellBg)
                .padding(if (immersive) PaddingValues(0.dp) else padding)
        ) {
            when (selected) {
                Screen.Dashboard -> DashboardScreen(
                    state = state,
                    vm = vm,
                    openTransfer = { selected = Screen.Transfer },
                    openCards = { selected = Screen.Cards },
                    openTransactions = { selected = Screen.Transactions },
                    darkMode = darkMode,
                    onFullScreenChanged = { immersive = it }
                )
                Screen.Transactions -> CardsScreenPremium(state = state, vm = vm, darkMode = darkMode)
                Screen.Transfer -> TransferScreen(state = state, vm = vm, darkMode = darkMode)
                Screen.Cards -> AssetsScreen(state = state, vm = vm, darkMode = darkMode)
                Screen.Profile -> ProfileScreen(state = state, vm = vm, darkMode = darkMode, onToggleTheme = { darkMode = !darkMode })
            }
        }
    }
}

private fun Screen.icon() = when (this) {
    Screen.Dashboard -> Icons.Default.AccountBalance
    Screen.Transactions -> Icons.Default.CreditCard
    Screen.Transfer -> Icons.AutoMirrored.Filled.Send
    Screen.Cards -> Icons.Default.AccountBalanceWallet
    Screen.Profile -> Icons.Default.Person
}

private fun launchBiometric(activity: FragmentActivity, onSuccess: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onSuccess()
        }
    })
    prompt.authenticate(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authentification bancaire")
            .setSubtitle("Confirmez votre identite")
            .setNegativeButtonText("Annuler")
            .build()
    )
}
