# SCpay — Application bancaire mobile (Madagascar)

> **Semaine 1 — Livrable intermédiaire (30 %)**  
> Authentification biométrique, virements sécurisés OTP, QR Code de paiement et gestion des bénéficiaires.

**Stack :** Laravel 11 + Sanctum · Android Kotlin / Jetpack Compose  
**Repo :** `main` → Séance 1 · `semaine_1` → Semaine 1 (ici) · `semaine_2` → Semaine 3

---

## Fonctionnalités

### Mobile (Kotlin / Jetpack Compose)
| Écran | Fonctionnalités |
|-------|----------------|
| Dashboard | Solde temps réel, transactions récentes, masquer montant |
| Virements | Virement interne et vers bénéficiaires, validation solde, OTP email (> 500 000 MGA) |
| Transactions | Historique filtré par date, montant et type |
| QR Code | Générer un QR de paiement, scanner un QR |
| Cartes | Liste et paramètres des cartes |
| Bénéficiaires | Ajouter, supprimer, sélectionner lors d'un virement |
| Profil | Informations compte, gestion bénéficiaires |
| Paramètres | Dark mode, changement PIN |

### Sécurité
- Auth **Sanctum** + token persisté en **EncryptedSharedPreferences AES256**
- PIN 4 chiffres stocké hashé localement
- **BiometricPrompt** (empreinte digitale)
- **Gel temporaire** du compte sur activité suspecte
- **Journal d'audit** de toutes les opérations
- Certificate pinning HTTPS forcé

### Backend (Laravel 11)
- `POST /api/transfers` — virement avec validation solde, journalisation
- Double validation **OTP email** pour les virements > 500 000 MGA
- Détection fraude basique (gel temporaire)
- Dashboard admin : transactions du jour, alertes fraude

---

## Installation

### Backend

```bash
cd back
composer install
cp .env.example .env
php artisan key:generate
php artisan migrate --seed        # 3 comptes, 50 transactions historiques
php artisan serve --host=0.0.0.0 --port=8000
```

Réinitialiser la base :
```bash
php artisan migrate:fresh --seed
```

Lancer les tests :
```bash
php artisan test
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

| Rôle | Email | Mot de passe | Solde |
|------|-------|-------------|-------|
| Utilisateur 1 | rasoa@example.com | Password@123 | 12 750 000 MGA |
| Utilisateur 2 | rakoto@example.com | Password@123 | 4 850 000 MGA |
| Admin | admin@bankingapp.mg | Admin@2026 | — |

Panel admin : [http://localhost:8000/admin](http://localhost:8000/admin)

---

## Architecture

```
back/
  app/Http/Controllers/Api/    # Contrôleurs API (Auth, Account, Transfer, QR…)
  app/Services/                # TransferService (OTP, virement, audit)
  app/Repositories/            # Couche requêtes DB
  app/Models/                  # Eloquent (User, Account, Transaction, Beneficiary…)

mobil/
  data/api/          # Retrofit (ApiService)
  data/repository/   # BankRepository — source de vérité unique
  ui/screens/        # Composables (Dashboard, Transfer, Cards, Profile…)
  ui/viewmodel/      # BankViewModel — état UI centralisé
  security/          # SecurityUtil — token + PIN en EncryptedSharedPreferences
```
