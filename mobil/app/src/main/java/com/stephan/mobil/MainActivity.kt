package com.stephan.mobil

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stephan.mobil.data.api.ApiClient
import com.stephan.mobil.data.repository.BankRepository
import com.stephan.mobil.security.SecurityUtil
import com.stephan.mobil.ui.notifications.NotificationHelper
import com.stephan.mobil.ui.theme.BrandPrimary
import com.stephan.mobil.ui.theme.BgBase
import com.stephan.mobil.ui.theme.BgSurfaceHigh
import com.stephan.mobil.ui.theme.TextPrimary
import com.stephan.mobil.ui.theme.MobilTheme
import com.stephan.mobil.ui.theme.LocalDarkMode
import com.stephan.mobil.ui.viewmodel.BankUiState
import com.stephan.mobil.ui.viewmodel.BankViewModel
import com.stephan.mobil.ui.viewmodel.BankViewModelFactory
import com.stephan.mobil.ui.viewmodel.CryptoViewModel
import com.stephan.mobil.ui.viewmodel.CryptoViewModelFactory
import com.stephan.mobil.ui.viewmodel.SupportViewModel
import com.stephan.mobil.ui.viewmodel.SupportViewModelFactory
import com.stephan.mobil.ui.screens.*

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannels(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
        setContent {
            val context = LocalContext.current
            val appPrefs = remember { context.getSharedPreferences("scpay_app", Context.MODE_PRIVATE) }
            var darkMode by remember { mutableStateOf(appPrefs.getBoolean("dark_mode", false)) }
            MobilTheme(darkTheme = darkMode) {
                val api  = remember { ApiClient.getClient(context).create(com.stephan.mobil.data.api.ApiService::class.java) }
                val repo = remember { BankRepository(api, context.applicationContext) }
                val vm: BankViewModel = viewModel(factory = BankViewModelFactory(repo, context.applicationContext))
                val supportVm: SupportViewModel = viewModel(factory = SupportViewModelFactory(repo))
                val cryptoVm: CryptoViewModel = viewModel(
                    factory = CryptoViewModelFactory(repo, onBalanceChanged = { vm.refreshAll() })
                )
                BankApp(vm, cryptoVm, supportVm, this,
                    onToggleTheme = {
                        darkMode = !darkMode
                        appPrefs.edit().putBoolean("dark_mode", darkMode).apply()
                    }
                )
            }
        }
    }
}

private enum class Screen(val label: String) {
    Dashboard("Accueil"),
    Transactions("Les atouts"),
    Transfer("Envoyer"),
    Cards("Actifs"),
    Profile("Profil")
}

@Composable
private fun BankApp(vm: BankViewModel, cryptoVm: CryptoViewModel, supportVm: SupportViewModel, activity: FragmentActivity, onToggleTheme: () -> Unit = {}) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    var unlocked by remember { mutableStateOf(!SecurityUtil.hasPinCode(context)) }

    // Re-lock when user logs in fresh (after a logout), not on session restore
    val wasLoggedOut = remember { arrayOf(state.user == null) }
    LaunchedEffect(state.user) {
        val loggedIn = state.user != null
        if (wasLoggedOut[0] && loggedIn && SecurityUtil.hasPinCode(context)) {
            unlocked = false
        }
        wasLoggedOut[0] = !loggedIn
    }

    // Re-lock after 30s in background
    DisposableEffect(activity) {
        var pausedAt = 0L
        val observer = object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                if (SecurityUtil.hasPinCode(context)) pausedAt = System.currentTimeMillis()
            }
            override fun onResume(owner: LifecycleOwner) {
                if (SecurityUtil.hasPinCode(context) && pausedAt > 0L &&
                    System.currentTimeMillis() - pausedAt > 30_000L) {
                    unlocked = false
                }
                pausedAt = 0L
            }
        }
        activity.lifecycle.addObserver(observer)
        onDispose { activity.lifecycle.removeObserver(observer) }
    }

    if (state.initializing) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0A0B0E)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_dark),
                    contentDescription = "SCpay",
                    modifier = Modifier.size(160.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "créé par Stephan & Cassie",
                    color = Color(0xFF8B8F98),
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    if (state.user == null) {
        WelcomeScreen(
            onLoginSubmit    = { email, password -> vm.login(email, password) },
            onRegisterSubmit = { name, email, phone, password -> vm.register(name, email, phone, password) },
            isLoading        = state.loading,
            errorMessage     = state.error
        )
        return
    }

    if (!unlocked) {
        PinScreen(
            onUnlock   = { unlocked = true },
            onBiometric = { launchBiometric(activity) { unlocked = true } }
        )
        return
    }

    LaunchedEffect(Unit) {
        while (true) {
            vm.loadPendingCardPayments()
            kotlinx.coroutines.delay(5_000)
        }
    }

    val pendingPayment = state.pendingCardPayments.firstOrNull()
    if (pendingPayment != null) {
        CardPaymentConfirmScreen(payment = pendingPayment, vm = vm)
    }

    MainShell(state, cryptoVm.uiState.collectAsState().value, vm, cryptoVm, supportVm, context, onToggleTheme)
}

@Composable
private fun MainShell(
    state: BankUiState,
    cryptoState: com.stephan.mobil.ui.viewmodel.CryptoUiState,
    vm: BankViewModel,
    cryptoVm: CryptoViewModel,
    supportVm: SupportViewModel,
    context: Context,
    onToggleTheme: () -> Unit = {}
) {
    val darkMode     = LocalDarkMode.current
    var selected     by remember { mutableStateOf(Screen.Dashboard) }
    var immersive    by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showKyc      by remember { mutableStateOf(false) }
    var showDeposit  by remember { mutableStateOf(false) }
    var showWithdraw by remember { mutableStateOf(false) }
    val snackbar     = remember { SnackbarHostState() }
    val shellBg      = if (darkMode) BgBase else Color.White
    val navInk       = if (darkMode) TextPrimary else Color(0xFF17181C)
    val navIndicator = if (darkMode) BgSurfaceHigh else Color(0xFFEDEDEF)

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

    if (showDeposit) {
        DepositScreen(state = state, vm = vm, onBack = { showDeposit = false })
        return
    }

    if (showWithdraw) {
        WithdrawScreen(state = state, vm = vm, onBack = { showWithdraw = false })
        return
    }

    if (showKyc) {
        KycScreen(state = state, vm = vm, onBack = { showKyc = false })
        return
    }

    if (showSettings) {
        SettingsScreen(
            state          = state,
            vm             = vm,
            onToggleTheme  = onToggleTheme,
            onBack         = { showSettings = false }
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (!immersive) {
                NavigationBar(containerColor = shellBg, tonalElevation = 8.dp) {
                    Screen.entries.forEach { screen ->
                        NavigationBarItem(
                            selected  = selected == screen,
                            onClick   = { selected = screen },
                            icon      = { Icon(screen.icon(), contentDescription = screen.label) },
                            label     = { Text(screen.label) },
                            colors    = NavigationBarItemDefaults.colors(
                                selectedIconColor   = BrandPrimary,
                                selectedTextColor   = BrandPrimary,
                                indicatorColor      = navIndicator,
                                unselectedIconColor = navInk,
                                unselectedTextColor = navInk
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
                    state             = state,
                    cryptoState       = cryptoState,
                    vm                = vm,
                    supportVm         = supportVm,
                    openTransfer      = { selected = Screen.Transfer },
                    openCards         = { selected = Screen.Transactions },
                    openDeposit       = { showDeposit = true },
                    openWithdraw      = { showWithdraw = true },
                    onFullScreenChanged = { immersive = it },
                    onOpenKyc         = { showKyc = true }
                )
                Screen.Transactions -> CardsScreen(state = state, vm = vm)
                Screen.Transfer     -> TransferScreen(state = state, vm = vm)
                Screen.Cards        -> AssetsScreen(state = state, cryptoState = cryptoState, vm = vm, cryptoVm = cryptoVm)
                Screen.Profile      -> ProfileScreen(
                    state          = state,
                    vm             = vm,
                    onToggleTheme  = onToggleTheme,
                    onOpenSettings = { showSettings = true }
                )
            }
        }
    }
}

private fun Screen.icon() = when (this) {
    Screen.Dashboard    -> Icons.Default.Home
    Screen.Transactions -> Icons.Default.CreditCard
    Screen.Transfer     -> Icons.AutoMirrored.Filled.Send
    Screen.Cards        -> Icons.Default.BarChart
    Screen.Profile      -> Icons.Default.Person
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
            .setSubtitle("Confirmez votre identité")
            .setNegativeButtonText("Annuler")
            .build()
    )
}
