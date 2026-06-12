# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> The root `../CLAUDE.md` covers the full project (backend + mobile overview). This file adds mobile-specific detail.

---

## Build & run

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires key.properties + scpay.keystore)
./gradlew assembleRelease

# Compile-only check (fast — use this before a full build)
./gradlew :app:compileDebugKotlin
```

**Change the dev server IP** without rebuild: set `DEV_API_URL` in `local.properties`:
```
DEV_API_URL=http://192.168.X.X:8000/api/
```
Or change it at runtime via Settings → Serveur field (persisted in SharedPreferences, triggers `Activity.recreate()`).

---

## Navigation

Single `FragmentActivity`. No Jetpack Navigation component — navigation is manual state:

- **Top-level tabs** — `private enum class Screen` in `MainActivity.kt`. `selected: Screen` drives which composable renders in the content area.
- **Dashboard overlays** — `private enum class HomeOverlay` in `DashboardScreen.kt`. Full-screen overlays (QR, Notifications, Support, Converter) replace the Dashboard content without changing the tab bar.
- **Sub-screens pushed from Profile** — passed as lambda callbacks (`onOpenCards`, `onOpenSettings`, etc.); rendered as full-screen composables with a back arrow.

Tab → Screen enum → Composable mapping (the enum label is the tab label):

| `Screen` enum | Tab label | Composable rendered |
|---|---|---|
| `Dashboard` | Accueil | `DashboardScreen` |
| `Transactions` | Les atouts | `CardsScreen` (premium card UI) |
| `Transfer` | Envoyer | `TransferScreen` |
| `Cards` | Actifs | `AssetsScreen` (crypto) |
| `Profile` | Profil | `ProfileScreen` |

> The enum names don't match the composable names — `Transactions` renders cards, `Cards` renders crypto assets.

---

## ViewModels & state

Three ViewModels, each with a single `UiState` data class exposed as `StateFlow`:

| ViewModel | State class | Owns |
|---|---|---|
| `BankViewModel` | `BankUiState` | Auth, balance, transactions, cards, transfers, deposits, withdrawals, notifications, KYC, beneficiaries |
| `CryptoViewModel` | `CryptoUiState` | Crypto portfolio, market prices (CoinGecko), MGA/USD rate (ForexClient) |
| `SupportViewModel` | `SupportUiState` | Single open/closed ticket + messages |

All three are created in `MainActivity` and passed down as parameters. No DI framework.

---

## Data layer

```
ApiService          — Retrofit interface for SCpay backend
CoinGeckoApiService — Retrofit interface for CoinGecko price feed
ForexClient         — Retrofit interface for open.er-api.com exchange rates
BankRepository      — wraps ApiService + Room cache; all methods return Result<T> via runCatching
AppDatabase (Room)  — scpay.db; entities: TransactionEntity, AccountEntity, NotificationEntity
```

**Adding a new API call** — 4 touch points:
1. `ApiService.kt` — add the `@GET`/`@POST`/etc. suspend fun returning `Response<ApiEnvelope<YourModel>>`
2. `Models.kt` — add the data class if needed
3. `BankRepository.kt` — wrap in `runCatching { apiService.foo().bodyOrThrow() }`
4. `BankViewModel.kt` (or `CryptoViewModel`/`SupportViewModel`) — call from a coroutine, update state

`bodyOrThrow()` is a private extension on `Response<ApiEnvelope<T>>` in `BankRepository.kt` that unwraps the envelope or throws.

---

## Theme & colors

`MobilTheme` (in `ui/theme/Theme.kt`) provides two composition locals:
- `LocalDarkMode: Boolean`
- `LocalBrandColor: Color` — `0xFFFFFFFF` (white) in dark, `0xFF17181C` (near-black) in light

**Every composable that touches brand color must read these locals:**
```kotlin
val darkMode = LocalDarkMode.current
val brand    = LocalBrandColor.current
```

`BgBase`, `TextPrimary`, and other constants in `Color.kt` are **dark-mode values only** — they are not adaptive. Use `if (darkMode) BgBase else Color.White` for backgrounds.

`BrandPrimarySoft` (`0x1AFFFFFF`) is invisible on white backgrounds — replace with `if (darkMode) BrandPrimarySoft else Color(0xFFF0F0F2)`.

**Exceptions** — use `BrandPrimary` directly (composition locals not accessible at file scope or in always-dark screens):
- File-level `private val` constants
- `PinScreen.kt` — always dark
- `WelcomeScreen.kt` — always dark

---

## Async UI feedback pattern (BottomSheets)

**Never show a `Dialog` on top of a `ModalBottomSheet`** — the Dialog steals window focus and triggers `onDismissRequest` on the BottomSheet, dismissing it before the API responds.

Standard pattern for async actions inside a BottomSheet (used in Buy/Sell/Swap/Send modals in `CryptoDetailSheet.kt`):

```kotlin
var txState by remember { mutableStateOf("form") } // "form" | "loading" | "success"

ModalBottomSheet(
    onDismissRequest = { if (txState != "loading") onClose() }
) {
    when (txState) {
        "loading" -> ModalTxLoading(ink, "...")
        "success" -> ModalTxSuccess(ink, accent, "Titre", "Sous-titre") { onClose() }
        else      -> { /* form content */ }
    }
}
```

`ModalTxLoading` and `ModalTxSuccess` are private helpers at the bottom of `CryptoDetailSheet.kt`.

---

## Security & session

`SecurityUtil` (singleton, `EncryptedSharedPreferences` AES256) holds: auth token, hashed PIN, biometric flag, device UUID.

PIN lock behaviour in `MainActivity.kt`:
- Shown on first login if PIN is configured
- Re-locks after 30 s in background (`onPause`/`onResume` lifecycle observer)
- `SecurityUtil.hasPinCode(context)` controls whether the lock screen appears

---

## Adding a new screen

1. Create `YourScreen.kt` in `ui/screens/`
2. Add a navigation entry — either:
   - A new `Screen` enum value in `MainActivity.kt` (new tab), or
   - A lambda callback passed down from an existing screen (sub-screen / overlay)
3. If the screen needs state not in an existing ViewModel, add it to `BankUiState` + `BankViewModel`, or create a new ViewModel following the `SupportViewModel` pattern
