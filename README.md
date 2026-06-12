# SCpay — Application bancaire mobile (Madagascar)

> **Séance 1 — Rendu de base (20 %)**  
> Écrans principaux, authentification Sanctum, données mockées et architecture MVVM sécurisée.

**Stack :** Laravel 11 + Sanctum · Android Kotlin / Jetpack Compose  
**Repo :** `main` → Séance 1 (ici) · `semaine_1` → Semaine 1 · `semaine_2` → Semaine 3

---

## Fonctionnalités

### Mobile (Kotlin / Jetpack Compose)
| Écran | Fonctionnalités |
|-------|----------------|
| Dashboard | Affichage solde, liste des transactions récentes (débits rouge, crédits vert) |
| Transactions | Historique complet |
| Virement | Formulaire de virement (UI, validation basique) |
| Cartes | Affichage des cartes |
| Profil | Informations du compte |

### Sécurité
- Authentification **Sanctum** (login / register)
- Token stocké en **EncryptedSharedPreferences AES256**
- **PIN code** 4 chiffres
- Architecture **MVVM** sécurisée
- Certificate pinning HTTPS forcé

### Backend (Laravel 11)
- `GET /api/balance` — solde du compte
- `GET /api/transactions` — historique des transactions
- Migrations : `accounts`, `transactions`, `transfers`, `cards`, `beneficiaries`
- Seeders : 3 comptes, 50 transactions historiques
- Dashboard admin de base

---

## Installation

### Backend

```bash
cd back
composer install
cp .env.example .env
php artisan key:generate
php artisan migrate --seed        # 3 comptes, 50 transactions
php artisan serve --host=0.0.0.0 --port=8000
```

Réinitialiser la base :
```bash
php artisan migrate:fresh --seed
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

| Rôle | Email | Mot de passe |
|------|-------|-------------|
| Utilisateur | (voir DatabaseSeeder) | Password@123 |
| Admin | admin@bankingapp.mg | Admin@2026 |

Panel admin : [http://localhost:8000/admin](http://localhost:8000/admin)

---

## Architecture

```
back/
  app/Http/Controllers/Api/    # AuthController, AccountController, TransactionController
  app/Models/                  # User, Account, Transaction, Card, Beneficiary
  database/migrations/         # Structure de base
  database/seeders/            # Données de test

mobil/
  data/api/          # Retrofit (ApiService)
  data/repository/   # BankRepository
  ui/screens/        # Dashboard, Transactions, Transfer, Cards, Profile
  ui/viewmodel/      # BankViewModel
  security/          # SecurityUtil — token + PIN chiffrés
```
