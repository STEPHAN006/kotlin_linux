# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**SCpay** — a full-stack mobile banking app for Madagascar (currency: MGA).
- **Backend**: Laravel 11 + Sanctum API in `back/`
- **Mobile**: Android Kotlin + Jetpack Compose in `mobil/`
- **Admin panel**: Laravel Blade web UI (session-based, no API)

---

## Backend (`back/`)

### Running the server
```bash
cd back
php artisan serve          # starts at http://localhost:8000
php artisan migrate        # run all migrations
php artisan migrate:fresh --seed  # reset + seed (3 accounts, 50 transactions)
php artisan withdrawals:process   # manually run scheduled withdrawals (cron runs this hourly)
```

### Running tests
```bash
cd back
php artisan test                           # run all 52 tests
php artisan test --filter=test_name        # single test
php artisan test tests/Feature/BankingFlowTest.php  # entire suite
```

Tests use SQLite in-memory (`DB_CONNECTION=sqlite`, `DB_DATABASE=:memory:` set in `phpunit.xml`). All tests are in `tests/Feature/BankingFlowTest.php` and use `RefreshDatabase`.

**SQLite/MySQL compatibility**: `TransactionRepository` uses a conditional for time-based queries:
```php
$isMySQL = config('database.default') !== 'sqlite';
->when($isMySQL, fn($q) => $q->whereRaw('HOUR(created_at) < 5'),
                 fn($q) => $q->whereRaw("strftime('%H', created_at) < '05'"))
```
Always apply this pattern for any raw time/date functions.

### Key packages
- `barryvdh/laravel-dompdf` — PDF statement generation (`StatementController`)
- `laravel/sanctum` — token-based API auth for mobile
- Session-based auth for the admin web panel (separate from Sanctum)

### Architecture
```
app/
  Http/Controllers/Api/     # Mobile JSON API controllers
  Http/Controllers/Admin/   # Web admin panel controllers (session auth)
  Services/                 # Business logic
    TransferService         # transfer + OTP + notification flow
    AuthService             # login/register/KYC
    AccountService          # balance queries
    TransactionService      # transaction helpers
    AuditService            # writes to audit_logs table
    AdminService            # admin dashboard aggregates
  Repositories/             # DB query layer (AccountRepository, TransactionRepository)
  Models/                   # Eloquent models
  Console/Commands/
    ProcessScheduledWithdrawals  # artisan withdrawals:process, scheduled hourly
```

**API response envelope**: all API responses are wrapped in `ApiEnvelope<T>`:
```json
{ "success": true, "message": "...", "data": { ... } }
```

**Transfer flow**: `TransferController` → `TransferService::initiate()` → OTP email for transfers >500k MGA → `TransferService::complete()` (creates `UserNotification` for sender + receiver).

**Anomaly detection**: `TransactionRepository::getFraudAlerts()` flags: repeated amounts, atypical hours (<5h), high-value transactions.

**Admin panel credentials**: `admin@bankingapp.mg` / `Admin@2026`  
Admin panel login checks `users.role = 'admin'` (not `is_admin`). Auth middleware: `AdminWebAuth` (web session), `EnsureUserIsAdmin` (API).

### API route groups
- Public: `POST /api/register`, `POST /api/login`, `GET /api/health`
- `auth:sanctum`: balance, transactions, transfers, QR, cards, deposits, scheduled-withdrawals, KYC, statements, notifications, support, crypto, card-payments, beneficiaries, profile
- `auth:sanctum` + `EnsureUserIsAdmin`: `GET /api/admin/dashboard`, `/api/admin/transactions`, `/api/admin/fraud-alerts`
- Web (session, `AdminWebAuth`): `/admin/*` — dashboard, transactions, users, cards, support, kyc, fraud, export
- Web (public): `/shop` — e-commerce QR payment demo (creates a `CardPayment`, mobile user confirms/declines)

### Export formats
- CSV: `GET /api/transactions/export`
- PDF statement: `GET /api/statements/monthly`
- SWIFT/MT940 is included in `StatementController`

---

## Mobile (`mobil/`)

### Building
```bash
cd mobil
./gradlew assembleDebug     # debug APK
./gradlew assembleRelease   # signed release APK (keystore: app/scpay.keystore)
```
Signing config in `app/build.gradle.kts`: keystore `scpay.keystore`, alias `scpay_key`, password `scpay2026`.

### API base URL
Set in `data/api/ApiClient.kt`:
```kotlin
private const val BASE_URL = "http://192.168.88.239:8000/api/"
```
Change this IP to match the Laravel server on the local network. For the emulator use `10.0.2.2`.

### Mock mode
`BankRepository.useMockData = false` by default. Set to `true` to bypass all API calls and use hardcoded mock data (for UI development without a running backend).

### Architecture
```
MVVM:
  BankViewModel      — single ViewModel for all banking ops, exposes BankUiState
  CryptoViewModel    — crypto portfolio + trading, exposes CryptoUiState; calls CoinGecko API
  SupportViewModel   — support ticket chat, exposes SupportUiState

  BankRepository     — wraps ApiService + Room cache; single source of truth
  ApiService         — Retrofit interface for the Laravel backend
  CoinGeckoApiService — external price feed (api.coingecko.com/api/v3)
  ForexClient        — USD/EUR→MGA exchange rates (open.er-api.com/v6)
  AppDatabase (Room) — local cache: TransactionEntity, AccountEntity, NotificationEntity

Screens (all @Composable, receive state + vm):
  MainActivity.kt           # nav shell, tab bar, theme toggle, BiometricPrompt
  DashboardScreen.kt        # home tab; also houses PremiumTransactionRow,
                            # TransactionDetailSheet, NotificationsScreen,
                            # SupportChatScreen, QR screens (choice/scan/receive/pay)
  CardsScreenPremium.kt     # "Les atouts" tab — large gradient card UI
  AssetsScreen.kt           # "Actifs" tab — crypto portfolio + CryptoDetailSheet
  TransferScreen.kt         # send money + OTP verification
  TransactionsScreen.kt     # transaction history (filterable + chart)
  ProfileScreen.kt          # user profile, beneficiaries, KYC entry point,
                            # card management (CardsScreen), settings entry point

Sub-screens / sheets (navigated to from main tabs):
  CardsScreen.kt            # card list + settings + ChooseCardScreen
  CryptoDetailSheet.kt      # per-coin detail: buy/sell/swap/send/receive modals
  DepositScreen.kt          # deposit flow (MVola/Orange/bank + USSD)
  WithdrawScreen.kt         # withdrawal + scheduled withdrawals
  SettingsScreen.kt         # dark mode toggle, change PIN (ChangePinFlow)
  KycScreen.kt              # CIN photo upload
  CurrencyConverterScreen.kt # MGA/USD/EUR converter
  CardPaymentConfirmScreen.kt # confirm/decline e-commerce card payment
  SpendingChartScreen.kt    # MPAndroidChart spending breakdown
  WelcomeScreen.kt          # login/register onboarding (video bg, always dark)
  PinScreen.kt              # PIN lock + biometric (always dark, BgDeep bg)
  SharedComponents.kt       # PremiumInputField (shared across transfer/deposit/etc.)
```

### Tab mapping (MainActivity)
| Tab label | Screen enum | Composable |
|-----------|-------------|------------|
| Accueil | Dashboard | DashboardScreen |
| Les atouts | Transactions | CardsScreenPremium |
| Envoyer | Transfer | TransferScreen |
| Actifs | Cards | AssetsScreen |
| Profil | Profile | ProfileScreen |

### Theme system — dark/light mode

`BrandPrimary = Color(0xFFFFFFFF)` (WHITE). This is the design-system accent used for dark mode. In light mode, white would be invisible — use `LocalBrandColor` instead.

Two composition locals provided by `MobilTheme` in `ui/theme/Theme.kt`:
- `LocalDarkMode` — `Boolean`, whether dark theme is active
- `LocalBrandColor` — `Color(0xFFFFFFFF)` in dark, `Color(0xFF17181C)` in light

**Pattern for every composable that uses brand colors:**
```kotlin
val darkMode = LocalDarkMode.current
val brand    = LocalBrandColor.current
// Then use `brand` instead of `BrandPrimary` for tints, borders, button containers
// Use `if (darkMode) BgBase else Color.White` for page backgrounds, etc.
```

**Exceptions — keep `BrandPrimary` directly:**
- File-level `private val` constants (e.g. `CryptoDetailSheet.kt`'s `Accent = BrandPrimary`) — composition locals are not accessible at file scope
- `PinScreen.kt` — always dark (`BgDeep` background)
- `WelcomeScreen.kt` — always dark (video + `DarkSlate` card containers)

`BrandPrimarySoft = Color(0x1AFFFFFF)` (10% white) is nearly invisible in light mode — replace with `if (darkMode) BrandPrimarySoft else Color(0xFFF0F0F2)`.

Dark mode preference is stored in `SharedPreferences("scpay_app")` key `dark_mode`, toggled from `SettingsScreen`.

### System bars (transparent)
Three-layer setup required — changing only one layer is not enough:
1. `mobil/app/src/main/res/values/themes.xml` — `NoActionBar` parent, transparent colors
2. `ui/theme/Theme.kt` — `SideEffect` sets `WindowCompat.setDecorFitsSystemWindows(window, false)` + transparent colors
3. `WelcomeScreen.kt` — `DisposableEffect` `onDispose {}` must stay empty (do not restore white bars)

### Security
`SecurityUtil` (singleton) manages all sensitive local state via `EncryptedSharedPreferences` (AES256):
- Sanctum auth token — persisted across sessions, cleared on logout
- 4-digit PIN — stored hashed, verified locally
- Biometric enabled flag
- Device ID (UUID generated once)

### Notifications (push + poll)
- **Firebase FCM**: `ScpayFirebaseService` receives push messages and shows local notifications. FCM token is stored locally and synced to backend via `POST /api/fcm-token` after login.
- **WorkManager poll**: `PushNotificationWorker` polls `GET /api/notifications` in background as fallback.
- **In-app**: `vm.loadNotifications()` called on `NotificationsScreen` open.

### Key dependencies
- Retrofit2 + Gson — API calls
- Room — local cache (`scpay.db`, `fallbackToDestructiveMigration`)
- `androidx.security:security-crypto` — PIN + token in `EncryptedSharedPreferences`
- `androidx.biometric:biometric` — fingerprint auth via `BiometricPrompt` (requires `FragmentActivity`)
- `androidx.work:work-runtime-ktx` — `PushNotificationWorker`
- Firebase Messaging — push notifications
- `material-icons-extended` — required for icons beyond the default set
- `com.github.PhilJay:MPAndroidChart` — spending category charts
- ZXing + CameraX — QR code generation and scanning
- Coil — async image loading (avatar, support chat images)
- ExoPlayer — video background on `WelcomeScreen`
- minSdk = 24, targetSdk = 36

### Notifications & Support chat
- **Notifications**: created server-side on transfer events and admin replies; mobile polls via `vm.loadNotifications()` on screen open
- **Support chat**: single open ticket per user; mobile calls `vm.loadSupportTicket()` on screen open and after each message; admin replies via web panel at `/admin/support`
