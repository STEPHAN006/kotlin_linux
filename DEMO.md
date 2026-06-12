# SCpay — Script de démo (15 min)

> **S** = Stephan → Téléphone 1 (compte rasoa@example.com)
> **A** = Arovana → Téléphone 2 (compte rakoto@example.com) + PC panel admin
> `[ action ]` = ce qu'on fait à l'écran

---

## Comptes de démo

| Qui | Email | Mot de passe | Solde | Crypto |
|---|---|---|---|---|
| **S** — Tél 1 | rasoa@example.com | Password@123 | 12 750 000 MGA | BTC 0.05 · ETH 1.2 · USDT 500 |
| **A** — Tél 2 | rakoto@example.com | Password@123 | 4 850 000 MGA | BTC 0.02 · ETH 0.8 |
| **A** — PC admin | admin@bankingapp.mg | Admin@2026 | — | — |

---

## Lancement avant démo

```bash
cd back
php artisan serve
```

**OTP pendant la démo :**
```bash
grep "OTP transfer" storage/logs/laravel.log | tail -1
# → "otp": "123456"
```

---

## Script

---

### MIN 0–1 — Introduction & connexion simultanée

`[ S et A tiennent chacun leur téléphone. PC ouvert sur le panel admin ]`

**S :** "Bonjour à tous. Je suis Stephan, voici Arovana. On va vous présenter SCpay — une application bancaire mobile complète développée de A à Z. Backend Laravel 11, app Android Kotlin Jetpack Compose."

**A :** "On a deux téléphones aujourd'hui pour vous montrer les interactions en temps réel entre deux utilisateurs — virements, QR, crypto, notifications. Et le panel d'administration sur PC."

`[ S et A se connectent en même temps sur leur téléphone respectif ]`

**S :** "Je me connecte sur mon compte."

**A :** "Moi sur le mien."

**S :** "L'authentification utilise des tokens Sanctum côté serveur. Localement, le token est stocké dans un SharedPreferences chiffré AES256. Avec option PIN et biométrie."

---

### MIN 1–2 — Dashboard

`[ S montre son dashboard ]`

**S :** "Mon dashboard — solde 12 750 000 MGA. Je peux masquer le montant d'un tap."

`[ S clique l'œil ]`

`[ A montre son dashboard ]`

**A :** "Le mien — 4 850 000 MGA. Les données sont en temps réel depuis l'API Laravel, et mises en cache localement avec Room pour fonctionner hors connexion."

**S :** "Je tap sur une transaction pour voir le détail."

`[ S ouvre une transaction → sheet de détail ]`

---

### MIN 2–4 — Virement en direct entre les deux téléphones

`[ S va sur l'onglet Envoyer. A garde son dashboard visible ]`

**S :** "Je vais faire un virement vers le compte d'Arovana. Compte destinataire : 1, montant : 600 000 MGA."

**A :** "Je suis sur mon dashboard, je ne touche rien."

`[ S appuie sur Exécuter → spinner → overlay OTP ]`

**S :** "Le montant dépasse 500 000 MGA — notre seuil de sécurité. Une double validation OTP est exigée. Le backend envoie un code par email."

**A :** "Je le récupère dans les logs Laravel."

`[ A tape dans le terminal du PC : grep "OTP transfer" storage/logs/laravel.log | tail -1 ]`

**A :** "Code : `[lire le code]`."

`[ S saisit le code → transfert exécuté ]`

**S :** "Envoyé."

`[ A fait un pull-to-refresh sur son téléphone → solde mis à jour ]`

**A :** "Je reçois la notification — et mon solde vient de monter. Le virement est instantané côté serveur."

`[ A montre la notification sur son téléphone ]`

---

### MIN 4–5 — Dépôt MVola

`[ S navigue dans Profil → Dépôt ]`

**S :** "Pour alimenter son compte, l'utilisateur peut déposer via MVola, Orange Money ou Airtel — les trois opérateurs malgaches. Je choisis MVola, je saisis un numéro et un montant."

`[ S appuie sur Initier ]`

**S :** "L'app génère le code USSD à composer. Une fois le paiement mobile fait, j'appuie Confirmer — le solde est crédité."

**A :** "Le backend crée la transaction en statut pending, puis la confirme. Toute la traçabilité est en base de données."

---

### MIN 5–7 — Cartes bancaires

`[ S va sur l'onglet Les atouts ]`

**S :** "La gestion des cartes. Ma carte virtuelle en grand format avec le dégradé."

`[ S appuie Révéler ]`

**S :** "Je révèle le numéro complet — ça fait un appel sécurisé au backend. Le numéro n'est jamais stocké en clair sur le téléphone."

`[ S toggle la carte active/inactive ]`

**S :** "Toggle activer / désactiver en un tap. Je modifie aussi le plafond de dépenses."

`[ A montre ses cartes sur son téléphone ]`

**A :** "Sur mon téléphone, même interface. Et depuis le PC admin, on voit toutes les cartes de tous les utilisateurs."

`[ A montre rapidement la section cartes dans l'admin ]`

**S :** "Je crée une nouvelle carte virtuelle."

`[ S crée une carte ]`

---

### MIN 7–9 — Crypto en temps réel entre les deux téléphones

`[ S va sur l'onglet Actifs ]`

**S :** "Le portefeuille crypto. Prix en temps réel depuis CoinGecko. Je tap sur Bitcoin."

`[ S ouvre la fiche BTC ]`

**S :** "Graphique de cours, mon solde, les actions. J'achète du BTC avec des MGA."

`[ S fait un achat — modal loading inline → succès ]`

**S :** "La modal de chargement reste à l'intérieur du panneau — pas de popup parasite. Achat réussi."

`[ S ouvre SwapModal → choisit ETH ]`

**S :** "Le swap : j'échange directement BTC contre ETH au cours du marché. Taux calculé automatiquement."

`[ S confirme le swap ]`

**S :** "Maintenant l'envoi de crypto. Je vais envoyer du Bitcoin à Arovana."

`[ A affiche son adresse BTC wallet depuis Actifs → BTC → Recevoir ]`

**A :** "Mon adresse wallet Bitcoin est là, avec le QR."

`[ S ouvre SendModal → scanne le QR d'A ]`

**S :** "Je scanne le QR d'Arovana. Regardez ces boutons — 25%, 50%, 75%, 100% de mon solde, avec le slider. Je choisit 25%."

`[ S confirme l'envoi ]`

**S :** "Envoyé."

`[ A reçoit une notification sur son téléphone ]`

**A :** "J'ai reçu la notification — du Bitcoin vient d'arriver sur mon wallet. Le backend a détecté que l'adresse destination correspond à un compte SCpay et a crédité automatiquement."

`[ A montre l'historique crypto : nouvelle ligne "Reçu" en vert ]`

---

### MIN 9–10 — QR & paiement e-commerce

`[ A génère un QR de paiement sur son téléphone depuis le Dashboard ]`

**A :** "Je génère mon QR pour recevoir un paiement."

`[ S ouvre le scanner QR depuis son Dashboard ]`

**S :** "Je scanne."

`[ S scanne le QR d'A → écran de confirmation paiement ]`

**S :** "L'écran de confirmation s'affiche — montant, destinataire. Je confirme."

**A :** "Et maintenant le paiement e-commerce."

`[ A ouvre http://localhost:8000/shop sur le PC ]`

**A :** "On a une page marchande de démo. Je choisis un article — un QR de paiement est généré."

`[ S scanne le QR depuis l'app ]`

**S :** "Je scanne — écran de confirmation carte. Je confirme ou je décline."

---

### MIN 10–11 — Support client en direct

`[ S ouvre le support sur son téléphone ]`

**S :** "Le support intégré. Un ticket est créé automatiquement. J'envoie un message — je peux aussi joindre une photo."

`[ S envoie un message texte + une photo ]`

`[ A ouvre le panel admin sur le PC → support ]`

**A :** "Je vois le message arriver en temps réel dans l'admin. Je réponds."

`[ A répond depuis l'admin ]`

`[ Notification arrive sur le téléphone de S ]`

**S :** "La notif est là — badge sur la cloche. La réponse s'affiche dans le chat."

**A :** "Je peux aussi clôturer le ticket depuis l'admin avec ce bouton rouge."

`[ S clôture depuis son téléphone avec le bouton cadenas ]`

**S :** "Ou moi depuis mon téléphone. Confirmation, clôturé. Le bouton Nouveau ticket apparaît."

---

### MIN 11–12 — KYC

`[ S va dans Profil → Vérification d'identité ]`

**S :** "La vérification d'identité. Je uploade ma photo de CIN recto et verso."

`[ S sélectionne des photos et envoie ]`

**S :** "Statut : En attente."

`[ A va sur http://localhost:8000/admin/kyc sur le PC ]`

**A :** "Le dossier arrive dans l'admin. Je peux voir les documents soumis. J'approuve."

`[ Notification arrive sur le téléphone de S ]`

**S :** "Notification reçue — KYC approuvé. Mon statut est mis à jour."

---

### MIN 12–13 — Profil & relevé PDF

`[ S va dans Profil ]`

**S :** "Dans le profil, je modifie mes informations — nom, email, téléphone."

`[ S modifie et sauvegarde ]`

**A :** "On peut aussi télécharger le relevé bancaire mensuel en PDF."

`[ A télécharge le relevé depuis son téléphone ]`

**A :** "Généré côté serveur avec DomPDF, il s'ouvre directement dans le viewer système."

---

### MIN 13–14 — Admin panel complet

`[ A navigue dans le panel admin sur le PC ]`

**A :** "Je vous montre rapidement le panel admin. Dashboard avec les stats en temps réel — utilisateurs actifs, volume des transactions du jour, tickets ouverts."

`[ A ouvre Transactions ]`

**A :** "Toutes les transactions sont là, filtrables. On voit les virements qu'on vient de faire."

`[ A ouvre Alertes fraude ]`

**A :** "Les alertes fraude sont détectées automatiquement — transactions entre minuit et 5h du matin, montants répétés identiques, gros montants inhabituels. Zéro configuration manuelle."

`[ A ouvre Export ]`

**A :** "Export CSV de toutes les transactions en un clic."

**S :** "56 routes API au total. 44 tests automatisés qui passent."

---

### MIN 14–15 — Dark mode & déconnexion

`[ S va dans Paramètres ]`

**S :** "Dernier point — le dark mode."

`[ S et A togglent le dark mode en même temps sur leurs deux téléphones ]`

**S :** "Toute l'interface bascule instantanément sur les deux téléphones."

`[ S montre le champ URL serveur ]`

**S :** "Ce champ URL serveur permet de changer l'adresse IP sans recompiler l'APK — l'app redémarre toute seule. Pratique en démo ou lors du passage en production."

`[ S se déconnecte ]`

**S :** "La déconnexion invalide le token côté serveur ET efface les données locales. Retour à l'écran d'accueil."

**A :** "Pour résumer — backend Laravel 11, 56 routes REST, auth Sanctum, OTP, push Firebase, détection fraude, panel admin. App Android Kotlin Jetpack Compose, MVVM, cache offline Room, biométrie, crypto live, QR, support chat. Merci."

**S :** "On est dispo pour les questions."

---

## Points de secours

| Problème | Solution |
|---|---|
| OTP introuvable | `grep "OTP transfer" storage/logs/laravel.log \| tail -1` |
| Notification pas reçue sur Tél 2 | Faire un pull-to-refresh sur le dashboard |
| Swap plante | `php artisan migrate:status` → swap migration doit être "Ran" |
| Crypto prix absents | Connexion internet requise (CoinGecko public) |
| QR shop ne scanne pas | Les deux téléphones et le PC sur le même réseau Wi-Fi |
| Solde insuffisant | `php artisan migrate:fresh --seed` remet tout à zéro |

---

## Vérification 10 min avant

```bash
cd back
php artisan migrate:status    # tout "Ran"
php artisan serve             # serveur lancé
php artisan test              # 44 tests passent
```
