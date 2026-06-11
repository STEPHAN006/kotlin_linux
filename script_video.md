# Script vidéo — SCpay (10–15 min)

## Répartition

| Rôle | Appareil | Tâche |
|------|----------|-------|
| **Stephan** | Tel 1 + PC admin | Compte principal, panel admin |
| **Richie** | Tel 2 | Compte destinataire, réactions |

> **Convention dans ce script :**
> - **[S]** → Stephan parle / fait l'action
> - **[R]** → Richie parle / fait l'action
> - *italique* → instruction de tournage

---

## Préparation avant tournage

```bash
# Remettre la base de données propre avec des données de démo
cd back && php artisan migrate:fresh --seed
php artisan serve
```

- Tel 1 : connecté avec `rakoto@scpay.mg`
- Tel 2 : connecté avec `rasoa@scpay.mg`
- PC : panel admin ouvert sur `http://localhost:8000/admin`
- Les deux téléphones en mode **Ne pas déranger**

---

## PARTIE 1 — Introduction (30 sec)

*Vue d'ensemble : les deux téléphones côte à côte, PC en arrière-plan*

**[S]** "Bonjour à tous. Aujourd'hui on vous présente **SCpay**, une application bancaire mobile qu'on a développée pour Madagascar."

**[R]** "L'app tourne sur Android, avec un serveur Laravel côté backend, et un panel d'administration accessible depuis le navigateur. On va tout vous montrer."

---

## PARTIE 2 — Connexion et sécurité (2 min)

*Tel 1 — écran d'accueil avec animation en fond*

**[S]** "On ouvre l'app. L'écran d'accueil affiche le nom de l'application avec une animation en fond. Deux options : créer un compte ou se connecter."

*Appuyer sur "Créer un compte"*

**[S]** "Le formulaire remonte depuis le bas. On entre le nom, l'email, le numéro de téléphone, et le mot de passe."

*Remplir le formulaire et valider*

**[S]** "L'app nous demande de choisir un **code PIN à 4 chiffres**. C'est ce code qui protège l'accès à chaque ouverture."

*Entrer le PIN → arriver sur le Dashboard*

**[R]** "Et si vous avez un téléphone compatible, vous pouvez aussi activer la **reconnaissance d'empreinte digitale** depuis les paramètres, pour déverrouiller encore plus vite."

*Tel 2 — se connecter rapidement*

**[R]** "Moi je me connecte sur le deuxième téléphone avec un autre compte. C'est celui-là qui va recevoir les virements."

---

## PARTIE 3 — Tableau de bord (1 min)

*Tel 1 — Dashboard*

**[S]** "Voilà le tableau de bord. En haut, le **solde total** du compte. Ce petit bouton œil permet de le cacher si on est dans un endroit public."

*Appuyer sur le toggle MGA/EUR*

**[S]** "Et là on peut basculer entre ariary et euros — la conversion se fait en temps réel."

*Scroll vers le bas*

**[R]** "En dessous il y a les actions rapides — Dépôt, Crypto, Convertir, Retrait. Et les dernières transactions. On peut tirer l'écran vers le bas pour actualiser."

---

## PARTIE 4 — Transfert d'argent (2 min)

*Tel 1 — onglet "Envoyer"*

**[S]** "On passe à l'onglet **Envoyer** pour faire un virement. On entre le numéro de compte du destinataire, le montant, et une note."

*Entrer un montant de 100 000 MGA*

**[S]** "Petit montant — 100 000 ariary. On appuie sur exécuter."

*Transaction réussie*

**[R]** *(montrer Tel 2)* "Et direct, j'ai reçu une notification sur mon téléphone. L'argent est arrivé."

*Tel 1 — faire un virement de 600 000 MGA*

**[S]** "Maintenant on essaie avec 600 000 ariary. L'app voit que c'est un gros montant et elle demande une **validation OTP** — un code envoyé par email."

*Montrer le code reçu, le saisir*

**[S]** "On entre le code et le virement passe. C'est une sécurité supplémentaire pour les grosses opérations."

**[R]** "Et moi j'ai encore reçu la notification de réception."

---

## PARTIE 5 — QR Code (1 min 30)

*Tel 2 — icône QR en haut du dashboard*

**[R]** "Je vais générer un QR code pour recevoir de l'argent. Je rentre le montant — 50 000 ariary — et voilà mon QR."

*Tel 1 — scanner le QR*

**[S]** "Je scanne avec mon téléphone. L'app lit le code, affiche le destinataire et le montant automatiquement. Je confirme et c'est envoyé."

**[R]** "Reçu ! C'est pratique pour les paiements rapides entre amis sans avoir à taper un numéro de compte."

---

## PARTIE 6 — Dépôt et retrait (1 min 30)

*Tel 1 — bouton Dépôt sur le dashboard*

**[S]** "Le dépôt permet de recharger son compte depuis un **mobile money** — MVola, Orange Money, ou Airtel Money."

*Sélectionner MVola*

**[S]** "On choisit MVola. L'app génère le code à composer. On entre le montant et on peut appuyer sur 'Appeler pour envoyer' qui ouvre directement le téléphone."

**[R]** "Une fois le paiement mobile fait, le solde est crédité automatiquement. L'app reste en attente de confirmation."

*Bouton Retrait*

**[S]** "Pour le retrait, on choisit un bénéficiaire enregistré, on entre le montant. Et il y a aussi la possibilité de faire des **retraits automatiques récurrents** — par exemple tous les 15 jours vers le même compte."

---

## PARTIE 7 — Cartes virtuelles (1 min 30)

*Tel 1 — onglet "Les Atouts"*

**[R]** "L'onglet **Les Atouts** c'est la gestion des cartes virtuelles. On voit la carte avec son design. Les petits points en bas permettent de naviguer entre plusieurs cartes."

**[S]** "Les actions disponibles : **Voir** affiche le numéro complet, le CVV et la date d'expiration — pour payer en ligne. **Bloquer** désactive la carte instantanément."

*Appuyer sur Voir*

**[S]** "Voilà les détails complets."

*Appuyer sur Supprimer*

**[R]** "Et **Supprimer** efface la carte avec une confirmation d'abord. On peut en créer une nouvelle avec le bouton + en haut."

*Appuyer sur + pour créer une carte*

**[S]** "Nouvelle carte créée, avec une autre couleur."

---

## PARTIE 8 — Crypto et actifs (1 min 30)

*Tel 1 — onglet "Actifs"*

**[S]** "L'onglet **Actifs** combine les comptes bancaires et les cryptomonnaies. En haut, la valeur totale de tout ce qu'on possède."

*Scroll vers les wallets*

**[R]** "On voit les portefeuilles crypto — Bitcoin, Ethereum, et d'autres. Les prix sont en direct. Chaque coin montre le prix actuel et la variation sur 24 heures."

*Cliquer sur Bitcoin*

**[S]** "En cliquant, on ouvre le détail. On peut **acheter, vendre ou envoyer** depuis là. L'app calcule l'équivalent en ariary ou en euros automatiquement."

---

## PARTIE 9 — Notifications et support (1 min)

*Tel 1 — icône cloche*

**[R]** "La cloche en haut, c'est les **notifications**. On voit les alertes — virements reçus, confirmations. On peut tout marquer comme lu d'un coup."

*Icône support chat*

**[S]** "Et là c'est le **support client**. On peut envoyer un message directement à l'équipe, avec possibilité de joindre une image. L'historique de la conversation est gardé."

---

## PARTIE 10 — Profil et paramètres (1 min)

*Tel 1 — onglet Profil*

**[R]** "Dans l'onglet **Profil**, on voit les infos du compte, les bénéficiaires enregistrés, et les cartes avec le bouton de suppression."

*Ouvrir les paramètres*

**[S]** "Dans les paramètres : modifier le profil, changer le PIN, activer l'empreinte, gérer les notifications. Et on peut passer en **mode sombre**."

*Activer le mode sombre*

**[R]** "Toute l'app change de thème instantanément."

---

## PARTIE 11 — KYC vérification d'identité (45 sec)

*Tel 1 — bannière KYC*

**[S]** "La vérification d'identité — KYC — c'est obligatoire pour débloquer toutes les fonctionnalités. On soumet son nom, son numéro de CIN, et les photos des documents."

**[R]** "Une fois envoyé, le dossier passe en revue côté admin. L'app affiche l'état : en attente, approuvé, ou refusé avec la raison."

---

## PARTIE 12 — Panel admin sur PC (3 min)

*Passer sur le PC — `localhost:8000/admin`*

**[S]** "On passe côté **administration**, accessible depuis n'importe quel navigateur. On se connecte avec le compte admin."

### Dashboard admin

**[R]** "Le tableau de bord résume tout : nombre de clients, solde total de la plateforme, volume de transactions, tickets ouverts, et alertes de fraude."

### Transactions

*Cliquer sur Transactions*

**[S]** "La section **transactions** liste tous les mouvements. On peut filtrer par type, par date, par montant, ou chercher par référence et par client."

### KYC

*Cliquer sur KYC*

**[R]** "La section **KYC** montre tous les dossiers en attente. On ouvre un dossier."

*Ouvrir un dossier*

**[R]** "On voit les photos des documents, les infos du client. On peut **approuver ou refuser** avec une raison. La décision s'affiche instantanément sur l'app mobile."

### Support

*Cliquer sur Support*

**[S]** "Le **support** liste tous les tickets clients. On ouvre une conversation."

*Taper une réponse et envoyer*

**[S]** "On répond depuis ici."

*Montrer Tel 1 ou Tel 2 — la réponse apparaît*

**[R]** *(montrer le téléphone)* "Et le message arrive directement dans l'app."

### Fraude

*Cliquer sur Fraude*

**[S]** "La section **fraude** surveille les comportements suspects automatiquement — grosses transactions inhabituelles, horaires atypiques, montants répétés. Chaque alerte est classée par niveau de gravité."

---

## PARTIE 13 — Paiement e-commerce (1 min)

*PC — ouvrir `localhost:8000/shop`*

**[R]** "Pour finir, une fonctionnalité avancée : le **paiement par carte sur un site**. On a une boutique de démo. On ajoute un article, on passe à la caisse."

*Sélectionner paiement par carte SCpay*

**[R]** "On choisit le paiement SCpay."

*Tel 1 — notification de paiement*

**[S]** *(montrer le téléphone)* "Sur mon téléphone, une notification arrive avec le nom du marchand et le montant. Je peux **confirmer ou refuser**."

*Confirmer sur le téléphone*

**[S]** "Je confirme."

**[R]** *(côté PC)* "Et côté boutique, le paiement est validé instantanément. Sans entrer de numéro de carte."

---

## PARTIE 14 — Conclusion (30 sec)

*Vue d'ensemble — les deux téléphones + PC*

**[S]** "Voilà l'essentiel de **SCpay**. Gestion de comptes, virements sécurisés, cartes virtuelles, crypto, dépôts mobile money, vérification d'identité, et un panel admin complet."

**[R]** "Le projet est développé en **Kotlin Jetpack Compose** côté Android et **Laravel** côté serveur."

**[S + R]** "Merci d'avoir regardé."

---

## Résumé du timing

| # | Scène | Qui parle | Durée |
|---|-------|-----------|-------|
| 1 | Intro | S + R | 30 sec |
| 2 | Connexion + PIN | S + R | 2 min |
| 3 | Dashboard | S + R | 1 min |
| 4 | Transfert + OTP | S + R | 2 min |
| 5 | QR Code | S + R | 1 min 30 |
| 6 | Dépôt / Retrait | S + R | 1 min 30 |
| 7 | Cartes virtuelles | S + R | 1 min 30 |
| 8 | Crypto | S + R | 1 min 30 |
| 9 | Notifs + Support | S + R | 1 min |
| 10 | Profil + Paramètres | S + R | 1 min |
| 11 | KYC | S + R | 45 sec |
| 12 | Admin panel | S + R | 3 min |
| 13 | E-commerce | S + R | 1 min |
| 14 | Conclusion | S + R | 30 sec |
| | **Total** | | **~17 min** |

> Couper au montage pour rester entre 10 et 15 min.
> Les parties 9, 10 et 11 peuvent être raccourcies si besoin.
