package com.stephan.mobil

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    Transactions("Les atouts"),
    Transfer("Envoyer"),
    Cards("Actifs"),
    Profile("Profil")
}

@Composable
private fun BankApp(vm: BankViewModel, activity: FragmentActivity) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    var unlocked by remember { mutableStateOf(!SecurityUtil.hasPinCode(context)) }

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
    var darkMode by remember { mutableStateOf(false) }
    var immersive by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val shellBg = if (darkMode) Color(0xFF101114) else Color.White

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
                    containerColor = shellBg,
                    tonalElevation = 8.dp
                ) {
                    Screen.entries.forEach { screen ->
                        NavigationBarItem(
                            selected = selected == screen,
                            onClick = { selected = screen },
                            icon = { Icon(screen.icon(), contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFD92C55),
                                selectedTextColor = Color(0xFFD92C55),
                                indicatorColor = Color(0xFFEDEDEF),
                                unselectedIconColor = if (darkMode) Color.White else Color(0xFF17181C),
                                unselectedTextColor = if (darkMode) Color.White else Color(0xFF17181C)
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
                    darkMode = darkMode,
                    onFullScreenChanged = { immersive = it }
                )
                Screen.Transactions -> TransactionsScreen(state = state, darkMode = darkMode)
                Screen.Transfer -> TransferScreen(state = state, vm = vm, darkMode = darkMode)
                Screen.Cards -> CardsScreen(state = state, vm = vm, darkMode = darkMode)
                Screen.Profile -> ProfileScreen(state = state, vm = vm, darkMode = darkMode, onToggleTheme = { darkMode = !darkMode })
            }
        }
    }
}

private fun Screen.icon() = when (this) {
    Screen.Dashboard -> Icons.Default.AccountBalance
    Screen.Transactions -> Icons.AutoMirrored.Filled.ReceiptLong
    Screen.Transfer -> Icons.AutoMirrored.Filled.Send
    Screen.Cards -> Icons.Default.CreditCard
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
