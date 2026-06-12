# SCpay — Application bancaire mobile (Madagascar)

> **Semaine 3 — Livrable complet (50 %)**  
> Application bancaire fullstack Android + Laravel avec crypto trading, notifications push, relevés PDF et panel admin complet.

**Stack :** Laravel 11 + Sanctum · Android Kotlin / Jetpack Compose · Room · Firebase FCM  
**Repo :** `main` → Séance 1 · `semaine_1` → Semaine 1 · `semaine_2` → Semaine 3 (ici)

---

## Fonctionnalités

### Mobile (Kotlin / Jetpack Compose)
| Écran | Fonctionnalités |
|-------|----------------|
| Dashboard | Solde temps réel, transactions récentes, masquer montant, notifications live |
| Virements | Virement interne/bénéficiaires, validation solde, OTP email (> 500 000 MGA) |
| Cartes | Cartes virtuelles générées (numéro, CVV, expiration), blocage/déblocage |
| Actifs (Crypto) | Portfolio BTC/ETH/USDT, prix live CoinGecko, achat/vente/swap/envoi/réception |
| Transactions | Historique filtré (date, montant, type), graphique dépenses MPAndroidChart |
| QR Code | Générer QR, scanner QR, confirmation PIN pour paiement e-commerce |
| Dépôt / Retrait | Dépôt MVola/Orange, retrait programmé (cron horaire) |
| Support | Chat utilisateur ↔ admin avec pièces jointes |
| Profil | Gestion bénéficiaires, KYC (photo CIN), relevé PDF |
| Paramètres | Dark mode, changement PIN, biométrie |

### Sécurité
- Auth **Sanctum** + token persisté en **EncryptedSharedPreferences AES256**
- PIN 4 chiffres stocké hashé localement
- **BiometricPrompt** (empreinte digitale)
- Gel temporaire sur activité suspecte · Journal d'audit complet
- Certificate pinning HTTPS forcé

### Backend (Laravel 11)
- **52 tests PHPUnit** (SQLite en mémoire)
- Détection anomalies : montants répétés, heures atypiques (< 5h)
- Export **CSV** et **SWIFT/MT940** · Relevé **PDF** (DomPDF)
- Notifications **Firebase FCM** push + polling WorkManager
- Panel admin : utilisateurs, transactions, fraude, KYC, support

---

## Installation

### Backend

```bash
cd back
composer install
cp .env.example .env
php artisan key:generate
php artisan migrate --seed        # 3 comptes, 50 transactions, portefeuilles crypto
php artisan serve --host=0.0.0.0 --port=8000
```

Réinitialiser la base :
```bash
php artisan migrate:fresh --seed
```

Lancer les tests :
```bash
php artisan test                  # 52 tests
```

### Mobile

1. Mettre à jour l'IP du serveur dans `mobil/app/src/main/java/com/stephan/mobil/data/api/ApiClient.kt` :
```kotlin
private const val BASE_URL = "http://<IP_DU_PC>:8000/api/"
```

2. Build et installation :
```bash
cd mobil
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

> Téléphone et PC doivent être sur le **même réseau Wi-Fi**.  
> Émulateur : utiliser `10.0.2.2` comme IP.

---

## Comptes de test

| Rôle | Email | Mot de passe | Solde | Crypto |
|------|-------|-------------|-------|--------|
| Utilisateur 1 | rasoa@example.com | Password@123 | 12 750 000 MGA | BTC 0.05 · ETH 1.2 · USDT 500 |
| Utilisateur 2 | rakoto@example.com | Password@123 | 4 850 000 MGA | BTC 0.02 · ETH 0.8 |
| Admin | admin@bankingapp.mg | Admin@2026 | — | — |

Panel admin : [http://localhost:8000/admin](http://localhost:8000/admin)

---

## Architecture

```
back/
  app/Http/Controllers/Api/      # Contrôleurs API mobile (JSON + Sanctum)
  app/Http/Controllers/Admin/    # Panel web admin (session Blade)
  app/Services/                  # Logique métier (Transfer, Auth, Account, Audit…)
  app/Repositories/              # Couche requêtes DB
  app/Models/                    # Modèles Eloquent

mobil/
  data/api/          # Retrofit (ApiService, CoinGeckoApiService, ForexClient)
  data/repository/   # BankRepository — source de vérité unique
  data/local/        # Room (transactions, comptes, notifications)
  ui/screens/        # Composables Jetpack Compose
  ui/viewmodel/      # BankViewModel, CryptoViewModel, SupportViewModel
  security/          # SecurityUtil — EncryptedSharedPreferences
```

---

## Variables d'environnement (`.env`)

```
APP_NAME=SCpay
DB_CONNECTION=mysql
MAIL_MAILER=smtp          # pour OTP virements
```
