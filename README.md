# SCpay — Application bancaire mobile (Madagascar)

Stack : Laravel 11 (backend API) + Android Kotlin / Jetpack Compose (mobile)

---

## Lancer le backend

```bash
cd back
php artisan serve --host=0.0.0.0 --port=8000
```

> `--host=0.0.0.0` est obligatoire pour que le téléphone puisse atteindre le serveur via le Wi-Fi local.

### Première installation

```bash
cd back
composer install
cp .env.example .env
php artisan key:generate
php artisan migrate --seed        # crée la base + données de test
```

### Réinitialiser la base

```bash
php artisan migrate:fresh --seed
```

### Lancer les tests (52 tests, SQLite en mémoire)

```bash
php artisan test
```

---

## Lancer l'application mobile

### Prérequis

- Android Studio ou SDK Android installé
- Téléphone et PC sur le **même réseau Wi-Fi**
- IP du PC à jour dans `mobil/app/src/main/java/com/stephan/mobil/data/api/ApiClient.kt` :

```kotlin
private const val BASE_URL = "http://<IP_DU_PC>:8000/api/"
```

Trouver l'IP du PC :

```bash
ip addr show | grep 'inet ' | grep -v 127
```

### Build + installation

```bash
cd mobil
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Comptes de test

| Rôle  | Email                  | Mot de passe |
|-------|------------------------|--------------|
| User  | (voir DatabaseSeeder)  | password     |
| Admin | admin@bankingapp.mg    | Admin@2026   |

Panel admin : [http://localhost:8000/admin](http://localhost:8000/admin)
