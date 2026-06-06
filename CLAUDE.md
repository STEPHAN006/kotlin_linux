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
  Http/Controllers/
    AdminSupportController  # Web admin panel (session auth)
  Services/                 # Business logic (TransferService, AuthService, etc.)
  Repositories/             # DB query layer (AccountRepository, TransactionRepository)
  Models/                   # Eloquent models
```

**Transfer flow**: `TransferController` → `TransferService::initiate()` → OTP email for transfers >500k MGA → `TransferService::complete()` (creates `UserNotification` for sender + receiver).

**Anomaly detection**: `TransactionRepository::getFraudAlerts()` flags: repeated amounts, atypical hours (<5h), high-value transactions.

**Admin panel credentials**: `admin@bankingapp.mg` / `Admin@2026`  
Admin panel login checks `users.role = 'admin'` (not `is_admin`).

### API route groups
- Public: `POST /api/register`, `POST /api/login`
- `auth:sanctum`: all user routes (balance, transactions, transfers, QR, cards, statements, notifications, support)
- `auth:sanctum` + `EnsureUserIsAdmin`: `GET /api/admin/dashboard`, `/api/admin/transactions`, `/api/admin/fraud-alerts`
- Web (session): `/admin/*` routes for the Blade admin panel

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
  BankViewModel (single ViewModel for entire app)
    └─ BankUiState (single StateFlow)
  BankRepository (wraps ApiService, has mock fallback)
  ApiService (Retrofit interface)

Screens (all @Composable, receive state + vm):
  MainActivity.kt           # nav shell, tab bar
  DashboardScreen.kt        # home + NotificationsScreen + SupportChatScreen
  CardsScreenPremium.kt     # "Les atouts" tab — large gradient card UI
  AssetsScreen.kt           # "Actifs" tab — crypto portfolio
  TransferScreen.kt         # send money + OTP verification
  TransactionsScreen.kt     # transaction history (filterable)
  ProfileScreen.kt          # user profile + settings
  WelcomeScreen.kt          # login / register onboarding
  PinScreen.kt              # PIN lock + biometric
```

### Tab mapping (MainActivity)
| Tab label | Screen enum | Composable |
|-----------|-------------|------------|
| Accueil | Dashboard | DashboardScreen |
| Les atouts | Transactions | CardsScreenPremium |
| Envoyer | Transfer | TransferScreen |
| Actifs | Cards | AssetsScreen |
| Profil | Profile | ProfileScreen |

### System bars (transparent)
Three-layer setup required — changing only one layer is not enough:
1. `mobil/app/src/main/res/values/themes.xml` — `NoActionBar` parent, transparent colors
2. `ui/theme/Theme.kt` — `SideEffect` sets `WindowCompat.setDecorFitsSystemWindows(window, false)` + transparent colors
3. `WelcomeScreen.kt` — `DisposableEffect` `onDispose {}` must stay empty (do not restore white bars)

### Key dependencies
- Retrofit2 + Gson — API calls
- `androidx.security:security-crypto` — PIN stored in `EncryptedSharedPreferences`
- `androidx.biometric:biometric` — fingerprint auth via `BiometricPrompt` (requires `FragmentActivity`)
- `androidx.work:work-runtime-ktx` — `PushNotificationWorker` (polls notifications in background)
- `material-icons-extended` — required for icons beyond the default set
- `com.github.PhilJay:MPAndroidChart` — spending category charts
- ZXing + CameraX — QR code generation and scanning
- minSdk = 24, targetSdk = 36

### Notifications & Support chat
- **Notifications**: created server-side on transfer events and admin replies; mobile polls via `vm.loadNotifications()` on screen open
- **Support chat**: single open ticket per user; mobile calls `vm.loadSupportTicket()` on screen open and after each message; admin replies via web panel at `/admin/support`
