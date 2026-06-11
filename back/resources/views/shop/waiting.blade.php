<!DOCTYPE html>
<html lang="fr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Confirmation en cours — SCpay Shop</title>
<script src="https://cdn.tailwindcss.com"></script>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@400,0..1" rel="stylesheet">
<style>
  body { font-family: 'Inter', sans-serif; }
  .material-symbols-outlined { font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24; display: inline-block; vertical-align: middle; }
  @keyframes ping-slow { 0%,100%{opacity:1;transform:scale(1)} 50%{opacity:.4;transform:scale(1.15)} }
  .ping-slow { animation: ping-slow 1.8s ease-in-out infinite; }
  @keyframes spin-smooth { to{transform:rotate(360deg)} }
  .spin { animation: spin-smooth 1.2s linear infinite; }
  @keyframes fade-in { from{opacity:0;transform:translateY(12px)} to{opacity:1;transform:translateY(0)} }
  .fade-in { animation: fade-in .4s ease forwards; }
</style>
</head>
<body class="bg-gray-50 min-h-screen flex flex-col">

<header class="bg-white border-b border-gray-200 shadow-sm">
  <div class="max-w-xl mx-auto px-4 h-16 flex items-center justify-between">
    <div class="flex items-center gap-2">
      <div class="w-8 h-8 bg-rose-600 rounded-lg flex items-center justify-center">
        <span class="material-symbols-outlined text-white text-[16px]">shield</span>
      </div>
      <span class="font-bold text-gray-900 text-lg">SCpay <span class="text-gray-400 font-normal">Shop</span></span>
    </div>
  </div>
</header>

<main class="flex-1 flex items-center justify-center p-4">
  <div class="max-w-md w-full">

    <!-- Pending state -->
    <div id="state-pending" class="fade-in text-center">
      <div class="relative w-24 h-24 mx-auto mb-8">
        <div class="absolute inset-0 bg-amber-100 rounded-full ping-slow"></div>
        <div class="relative w-24 h-24 bg-white border-2 border-amber-300 rounded-full flex items-center justify-center shadow-lg">
          <span class="material-symbols-outlined text-amber-500 text-4xl">smartphone</span>
        </div>
      </div>

      <h1 class="text-2xl font-bold text-gray-900 mb-2">Confirmez sur votre téléphone</h1>
      <p class="text-gray-500 mb-8">Une notification a été envoyée à l'application SCpay. Ouvrez-la et confirmez le paiement.</p>

      <!-- Order summary -->
      <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5 mb-6 text-left">
        <p class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">Récapitulatif</p>
        <div class="flex justify-between items-center mb-2">
          <span class="text-gray-600">Article</span>
          <span class="font-semibold text-gray-900">{{ $payment->product }}</span>
        </div>
        <div class="flex justify-between items-center mb-2">
          <span class="text-gray-600">Marchand</span>
          <span class="font-semibold text-gray-900">{{ $payment->merchant }}</span>
        </div>
        <div class="flex justify-between items-center border-t border-gray-100 pt-3 mt-3">
          <span class="text-gray-600 font-medium">Total</span>
          <span class="text-xl font-bold text-gray-900 tabular-nums">{{ number_format((float)$payment->amount, 0, ',', ' ') }} MGA</span>
        </div>
      </div>

      <!-- Timer -->
      <div class="flex items-center justify-center gap-2 text-sm text-gray-400 mb-6">
        <svg class="spin w-4 h-4 text-rose-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Expiration dans <span id="timer" class="font-semibold tabular-nums text-gray-600">5:00</span>
      </div>

      <a href="{{ route('shop.index') }}" class="text-sm text-gray-400 hover:text-gray-600 transition-colors">
        ← Annuler et retourner à la boutique
      </a>
    </div>

    <!-- Approved state -->
    <div id="state-approved" class="fade-in text-center hidden">
      <div class="w-24 h-24 bg-emerald-100 rounded-full flex items-center justify-center mx-auto mb-8 shadow-lg">
        <span class="material-symbols-outlined text-emerald-500 text-5xl" style="font-variation-settings: 'FILL' 1">check_circle</span>
      </div>
      <h1 class="text-2xl font-bold text-gray-900 mb-2">Paiement confirmé !</h1>
      <p class="text-gray-500 mb-6">Le montant a été débité de votre compte SCpay.</p>
      <div class="bg-emerald-50 border border-emerald-200 rounded-2xl p-4 text-left mb-8">
        <div class="flex justify-between items-center mb-1">
          <span class="text-gray-600 text-sm">Article</span>
          <span class="font-semibold text-gray-900 text-sm" id="approved-product">—</span>
        </div>
        <div class="flex justify-between items-center">
          <span class="text-gray-600 text-sm">Montant débité</span>
          <span class="font-bold text-emerald-700 tabular-nums" id="approved-amount">—</span>
        </div>
      </div>
      <a href="{{ route('shop.index') }}"
         class="inline-flex items-center gap-2 bg-rose-600 hover:bg-rose-700 text-white font-semibold px-6 py-3 rounded-xl transition-all">
        <span class="material-symbols-outlined text-[18px]">storefront</span>
        Retourner à la boutique
      </a>
    </div>

    <!-- Declined state -->
    <div id="state-declined" class="fade-in text-center hidden">
      <div class="w-24 h-24 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-8 shadow-lg">
        <span class="material-symbols-outlined text-red-500 text-5xl" style="font-variation-settings: 'FILL' 1">cancel</span>
      </div>
      <h1 class="text-2xl font-bold text-gray-900 mb-2">Paiement refusé</h1>
      <p class="text-gray-500 mb-8">Vous avez refusé ce paiement depuis l'application mobile.</p>
      <a href="{{ route('shop.index') }}"
         class="inline-flex items-center gap-2 border border-gray-300 text-gray-700 hover:bg-gray-100 font-semibold px-6 py-3 rounded-xl transition-all">
        ← Retourner à la boutique
      </a>
    </div>

    <!-- Expired state -->
    <div id="state-expired" class="fade-in text-center hidden">
      <div class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-8">
        <span class="material-symbols-outlined text-gray-400 text-5xl">timer_off</span>
      </div>
      <h1 class="text-2xl font-bold text-gray-900 mb-2">Délai expiré</h1>
      <p class="text-gray-500 mb-8">La session de paiement a expiré (5 minutes). Aucun débit n'a été effectué.</p>
      <a href="{{ route('shop.index') }}"
         class="inline-flex items-center gap-2 bg-rose-600 hover:bg-rose-700 text-white font-semibold px-6 py-3 rounded-xl transition-all">
        Réessayer
      </a>
    </div>

  </div>
</main>

<script>
const reference = "{{ $payment->reference }}";
const statusUrl  = "{{ route('shop.status', $payment->reference) }}";
const expiresAt  = new Date("{{ $payment->expires_at->toISOString() }}");

function show(state) {
  ['pending','approved','declined','expired'].forEach(s => {
    document.getElementById('state-' + s).classList.toggle('hidden', s !== state);
  });
}

// Timer countdown
function updateTimer() {
  const left = Math.max(0, Math.floor((expiresAt - Date.now()) / 1000));
  const m = Math.floor(left / 60);
  const s = left % 60;
  document.getElementById('timer').textContent = m + ':' + String(s).padStart(2,'0');
  if (left === 0) show('expired');
}
updateTimer();
setInterval(updateTimer, 1000);

// Polling every 3 seconds
let stopped = false;
async function poll() {
  if (stopped) return;
  try {
    const res  = await fetch(statusUrl);
    const data = await res.json();
    if (data.status === 'approved') {
      stopped = true;
      document.getElementById('approved-product').textContent = data.product;
      document.getElementById('approved-amount').textContent =
        new Intl.NumberFormat('fr-FR').format(data.amount) + ' MGA';
      show('approved');
    } else if (data.status === 'declined') {
      stopped = true;
      show('declined');
    } else if (data.status === 'expired') {
      stopped = true;
      show('expired');
    }
  } catch (_) {}
  if (!stopped) setTimeout(poll, 3000);
}
setTimeout(poll, 3000);
</script>
</body>
</html>
