# Préparation aux questions — Application Bancaire Kotlin + Laravel

---

## Concepts généraux

**Q : C'est quoi une API ?**
Une API (Application Programming Interface) est un ensemble de règles qui permet à deux applications de communiquer. Dans notre projet, l'app Android envoie des requêtes HTTP à l'API Laravel, qui répond avec des données JSON. Ex : l'app demande `GET /api/balance`, Laravel répond `{ "balance": 1250.00 }`.

**Q : C'est quoi une requête HTTP ?**
C'est un message envoyé par le client (l'app Android) au serveur (Laravel) pour demander ou envoyer quelque chose. Il existe plusieurs types :
- `GET` : récupérer des données (ex : lire le solde)
- `POST` : envoyer des données (ex : faire un virement)
- `PUT/PATCH` : modifier une donnée existante
- `DELETE` : supprimer une donnée

**Q : C'est quoi une réponse HTTP ?**
C'est ce que renvoie le serveur après avoir reçu une requête. Elle contient un **code de statut** (200 = OK, 201 = créé, 401 = non autorisé, 422 = erreur de validation, 500 = erreur serveur) et un **corps** (souvent du JSON).

**Q : C'est quoi le JSON ?**
JSON (JavaScript Object Notation) est un format texte pour échanger des données. C'est ce que Laravel renvoie à Kotlin. Ex : `{ "id": 1, "amount": 200, "type": "credit" }`.

**Q : C'est quoi une dépendance ?**
Une dépendance est une bibliothèque externe qu'on ajoute au projet pour ne pas récrire du code existant. Dans Kotlin on les déclare dans `build.gradle`, dans Laravel dans `composer.json`.

**Q : C'est quoi une bibliothèque (lib) ?**
Un ensemble de code pré-écrit qu'on réutilise. Ex : Retrofit est une lib qui facilite les appels HTTP, DomPDF est une lib qui génère des PDFs.

**Q : Différence entre lib et framework ?**
Une **lib** fait une chose précise (on l'appelle quand on veut). Un **framework** donne une structure complète au projet (il appelle notre code). Laravel est un framework, DomPDF est une lib.

**Q : C'est quoi un endpoint ?**
Un endpoint est une URL précise de l'API qui correspond à une action. Ex : `/api/transfers` est l'endpoint pour créer un virement.

**Q : C'est quoi un token ?**
Un token est une chaîne de caractères générée par le serveur après connexion, que le client envoie dans chaque requête pour prouver qu'il est authentifié. Dans notre projet on utilise les tokens Sanctum.

**Q : C'est quoi le HTTPS ?**
HTTPS est HTTP sécurisé : les données échangées sont chiffrées via TLS/SSL. Notre app force HTTPS pour protéger les données bancaires en transit.

**Q : C'est quoi une migration (base de données) ?**
Un fichier qui décrit comment créer ou modifier une table en base de données. Ça permet de versionner la structure de la BDD. Ex : `create_accounts_table.php` crée la table `accounts`.

**Q : C'est quoi un seeder ?**
Un seeder est un script qui insère des données de test en base. Dans notre projet : 3 comptes et 50 transactions historiques sont injectés via seeders pour tester l'app.

**Q : C'est quoi un modèle (Model) ?**
Une classe qui représente une table en base de données. Dans Laravel, le modèle `Account` correspond à la table `accounts`. Il contient les règles de validation et les relations.

---

## Laravel

**Q : C'est quoi Laravel ?**
Laravel est un framework PHP côté serveur (backend). Il reçoit les requêtes de l'app Android, traite la logique métier, accède à la base de données et renvoie des réponses JSON.

**Q : C'est quoi Sanctum ?**
Laravel Sanctum est un système d'authentification par token pour les API. Quand l'utilisateur se connecte, Laravel génère un token que l'app Android stocke et envoie dans le header `Authorization: Bearer <token>` à chaque requête.

**Q : C'est quoi Composer ?**
Composer est le gestionnaire de dépendances de PHP. On l'utilise pour installer les libs Laravel (comme DomPDF). L'équivalent de `npm` pour Node.js ou `Gradle` pour Android.

**Q : C'est quoi DomPDF ?**
DomPDF est une bibliothèque PHP qui convertit du HTML/CSS en fichier PDF. On l'utilise pour générer les relevés de compte mensuels téléchargeables.

**Q : C'est quoi un Controller dans Laravel ?**
Un Controller reçoit la requête HTTP, appelle la logique nécessaire (modèles, services) et retourne une réponse. Ex : `TransferController` gère les virements.

**Q : C'est quoi un Middleware dans Laravel ?**
Un middleware s'exécute avant que la requête atteigne le controller. On l'utilise pour vérifier l'authentification, les permissions, ou loguer les accès. Ex : `auth:sanctum` bloque toute requête sans token valide.

**Q : C'est quoi la validation dans Laravel ?**
Avant de traiter une requête, Laravel vérifie que les données envoyées sont correctes (format, champs obligatoires, solde suffisant). Si la validation échoue, il renvoie automatiquement une erreur 422.

**Q : C'est quoi PHPUnit ?**
PHPUnit est le framework de tests pour PHP. On écrit des tests automatisés pour vérifier que les endpoints fonctionnent correctement (ex : un virement avec solde insuffisant doit renvoyer une erreur).

**Q : C'est quoi un OTP ?**
OTP (One-Time Password) est un mot de passe à usage unique envoyé par email. Dans notre projet, les gros virements nécessitent une validation OTP pour sécuriser l'opération (double validation).

**Q : C'est quoi un journal d'audit ?**
Un journal d'audit enregistre toutes les opérations sensibles (qui a fait quoi, quand, depuis quelle IP). Ça permet de retracer les actions en cas de fraude ou litige.

**Q : C'est quoi un Seeder vs Factory dans Laravel ?**
Un **Seeder** insère des données fixes définies manuellement. Une **Factory** génère des données aléatoires en masse (utile pour les tests). On peut les combiner.

**Q : C'est quoi Eloquent ?**
Eloquent est l'ORM (Object-Relational Mapping) de Laravel. Il permet de manipuler la base de données avec du PHP objet au lieu d'écrire du SQL brut. Ex : `Account::find(1)` récupère le compte avec l'id 1.

**Q : C'est quoi SWIFT/CSV dans votre projet ?**
SWIFT est un format standard d'échange bancaire international. CSV (Comma-Separated Values) est un fichier tableur simple. On exporte les transactions dans ces formats pour la comptabilité.

---

## Kotlin / Android

**Q : C'est quoi Kotlin ?**
Kotlin est un langage de programmation moderne pour Android, créé par JetBrains et officiellement supporté par Google. Il remplace Java pour le développement Android.

**Q : C'est quoi Retrofit ?**
Retrofit est une bibliothèque Android qui simplifie les appels HTTP. On définit des interfaces avec des annotations (`@GET`, `@POST`) et Retrofit gère tout le réseau automatiquement.

**Q : C'est quoi MVVM ?**
MVVM (Model-View-ViewModel) est une architecture Android :
- **Model** : les données et la logique métier
- **View** : les écrans (Activity/Fragment)
- **ViewModel** : le lien entre les deux, gère l'état de l'UI

Ça sépare les responsabilités et facilite les tests.

**Q : C'est quoi le certificate pinning ?**
C'est une technique de sécurité qui force l'app à n'accepter que le certificat SSL de notre serveur. Ça empêche les attaques de type "man-in-the-middle" où quelqu'un intercepte le trafic réseau.

**Q : C'est quoi EncryptedSharedPreferences ?**
C'est un stockage chiffré sur Android pour les données sensibles (comme le PIN code). Les données sont automatiquement chiffrées/déchiffrées avec AES. Sans chiffrement, n'importe qui avec accès au téléphone pourrait lire le PIN.

**Q : C'est quoi BiometricPrompt ?**
BiometricPrompt est l'API Android officielle pour l'authentification biométrique (empreinte digitale, reconnaissance faciale). Elle affiche une boîte de dialogue système sécurisée pour valider l'identité.

**Q : C'est quoi AES ?**
AES (Advanced Encryption Standard) est un algorithme de chiffrement symétrique très sécurisé. Dans notre app, on l'utilise pour chiffrer les données sensibles stockées localement sur le téléphone.

**Q : C'est quoi un QR Code dans votre projet ?**
Le QR Code encode les informations de paiement (numéro de compte, montant). L'app peut en générer (pour recevoir un paiement) et en scanner (pour envoyer). On utilise la lib ZXing pour ça.

**Q : C'est quoi MPAndroidChart ?**
MPAndroidChart est une bibliothèque Android pour afficher des graphiques. On l'utilise pour afficher les dépenses par catégorie sous forme de graphique (camembert, barres, courbes).

**Q : C'est quoi Gradle ?**
Gradle est l'outil de build d'Android. Il compile le code, gère les dépendances (déclarées dans `build.gradle`) et génère l'APK. L'équivalent de Composer côté Laravel.

**Q : C'est quoi un APK signé ?**
Un APK (Android Package) est le fichier d'installation d'une app Android. "Signé" signifie qu'il est signé avec un certificat qui prouve que c'est bien vous qui l'avez créé. Obligatoire pour publier sur le Play Store.

**Q : C'est quoi une notification push ?**
Une notification push est un message envoyé depuis le serveur vers l'app même quand elle est en arrière-plan. Dans notre projet, on envoie une alerte instantanée à chaque transaction (via Firebase FCM).

**Q : C'est quoi une carte virtuelle dans votre projet ?**
Une carte bancaire qui existe uniquement sous forme numérique (pas de carte physique). Elle a un numéro, un CVV et une date d'expiration générés par le serveur. On peut la bloquer/débloquer depuis l'app.

**Q : C'est quoi LiveData / StateFlow ?**
Ce sont des observables Kotlin/Android. Le ViewModel expose des données via LiveData ou StateFlow, et la Vue s'y abonne automatiquement pour se mettre à jour quand les données changent (sans `refresh` manuel).

**Q : C'est quoi un Repository dans l'architecture MVVM ?**
Le Repository est une couche intermédiaire entre le ViewModel et les sources de données (API, base locale). Il abstrait l'origine des données : le ViewModel ne sait pas si les données viennent du réseau ou du cache.

---

## Sécurité

**Q : C'est quoi une attaque man-in-the-middle ?**
C'est quand un attaquant se positionne entre le client et le serveur pour intercepter ou modifier les communications. HTTPS + certificate pinning protège contre ça.

**Q : C'est quoi le gel de compte (gel temporaire) ?**
Si le système détecte une activité suspecte (ex : 5 tentatives de connexion échouées, virement inhabituel), le compte est temporairement bloqué pour protéger l'utilisateur.

**Q : C'est quoi la détection d'anomalies dans votre projet ?**
Le backend analyse les patterns de transactions : même montant répété plusieurs fois, virement à une heure inhabituelle, etc. Si une anomalie est détectée, une alerte est générée et le compte peut être gelé.

---

## Questions transversales

**Q : Comment l'app Android communique avec Laravel ?**
L'app envoie des requêtes HTTP via Retrofit en HTTPS. Elle inclut le token Sanctum dans le header. Laravel reçoit la requête, vérifie le token via le middleware Sanctum, traite la demande et renvoie du JSON.

**Q : Quel est le flux d'un virement ?**
1. L'utilisateur remplit le formulaire dans l'app
2. Kotlin envoie `POST /api/transfers` avec les données
3. Laravel valide (solde suffisant, bénéficiaire valide)
4. Si montant élevé → envoi d'OTP par email → l'utilisateur valide
5. Laravel débite le compte source, crédite le compte destination
6. Laravel journalise l'opération dans l'audit
7. Une notification push est envoyée sur le téléphone
8. Kotlin reçoit la réponse et met à jour l'affichage

**Q : Pourquoi avoir choisi MVVM ?**
MVVM est l'architecture recommandée par Google pour Android. Elle sépare la logique de l'interface, facilite les tests unitaires, et évite les problèmes de rotation d'écran (le ViewModel survit aux changements de configuration).

**Q : Pourquoi Laravel et pas Node.js ou Django ?**
Laravel est mature, sécurisé par défaut (CSRF, SQL injection protégés), dispose d'un écosystème riche (Sanctum, Eloquent, DomPDF) et est très utilisé en entreprise pour les APIs RESTful.
