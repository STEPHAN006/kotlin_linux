<!DOCTYPE html>
<html lang="fr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>SCpay Shop</title>
<script src="https://cdn.tailwindcss.com"></script>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@400,0" rel="stylesheet">
<style>
  body { font-family: 'Inter', sans-serif; }
  .material-symbols-outlined { font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24; display: inline-block; vertical-align: middle; }
  input::-webkit-outer-spin-button, input::-webkit-inner-spin-button { -webkit-appearance: none; }
  .card-input { letter-spacing: 0.15em; font-family: 'Courier New', monospace; }
</style>
</head>
<body class="bg-gray-50 min-h-screen">

<!-- Header -->
<header class="bg-white border-b border-gray-200 sticky top-0 z-20 shadow-sm">
  <div class="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
    <div class="flex items-center gap-2">
      <div class="w-8 h-8 bg-rose-600 rounded-lg flex items-center justify-center">
        <span class="material-symbols-outlined text-white text-[16px]">shield</span>
      </div>
      <span class="font-bold text-gray-900 text-lg">SCpay <span class="text-gray-400 font-normal">Shop</span></span>
    </div>
    <div class="flex items-center gap-1.5 text-sm text-gray-500">
      <span class="material-symbols-outlined text-emerald-500 text-[18px]">lock</span>
      Paiement sécurisé
    </div>
  </div>
</header>

<div class="max-w-6xl mx-auto px-4 py-10">
  <div class="grid grid-cols-1 lg:grid-cols-2 gap-10">

    <!-- Products -->
    <div>
      <h2 class="text-2xl font-bold text-gray-900 mb-6">Choisissez un article</h2>

      @if($errors->has('card_number') || $errors->has('expiry') || $errors->has('cvv'))
      <div class="mb-4 bg-red-50 border border-red-200 rounded-xl p-4 flex items-start gap-3">
        <span class="material-symbols-outlined text-red-500 text-[20px] flex-shrink-0 mt-0.5">error</span>
        <div>
          @foreach(['card_number','expiry','cvv'] as $field)
            @error($field)<p class="text-sm text-red-700">{{ $message }}</p>@enderror
          @endforeach
        </div>
      </div>
      @endif

      <div class="space-y-3" id="product-list">
        @foreach($products as $p)
        <label class="block cursor-pointer">
          <input type="radio" name="selected_product" value="{{ $p['id'] }}" class="sr-only peer"
                 {{ (old('product_id', 1) == $p['id']) ? 'checked' : '' }}>
          <div class="border-2 border-gray-200 peer-checked:border-rose-500 peer-checked:bg-rose-50 rounded-2xl p-4 flex items-center gap-4 transition-all hover:border-gray-300">
            <div class="w-12 h-12 bg-gray-100 peer-checked:bg-rose-100 rounded-xl flex items-center justify-center flex-shrink-0 transition-colors">
              <span class="material-symbols-outlined text-gray-400 text-2xl">{{ $p['image'] }}</span>
            </div>
            <div class="flex-1 min-w-0">
              <p class="font-semibold text-gray-900">{{ $p['name'] }}</p>
              <p class="text-sm text-gray-500 mt-0.5">{{ $p['description'] }}</p>
            </div>
            <div class="text-right flex-shrink-0">
              <p class="font-bold text-gray-900 tabular-nums">{{ number_format($p['price'], 0, ',', ' ') }}</p>
              <p class="text-xs text-gray-400">MGA</p>
            </div>
          </div>
        </label>
        @endforeach
      </div>
    </div>

    <!-- Checkout form -->
    <div>
      <h2 class="text-2xl font-bold text-gray-900 mb-6">Payer par carte virtuelle</h2>

      <form method="POST" action="{{ route('shop.checkout') }}" id="checkout-form" class="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 space-y-5">
        @csrf
        <input type="hidden" name="product_id" id="product_id_input" value="{{ old('product_id', 1) }}">

        <!-- Card visual preview -->
        <div class="rounded-2xl p-5 text-white relative overflow-hidden"
             style="background: linear-gradient(135deg, #0D0D0D 0%, #3B1F6E 60%, #00BFA5 100%);">
          <div class="absolute top-0 right-0 w-40 h-40 rounded-full opacity-5 bg-white -translate-y-10 translate-x-10"></div>
          <div class="flex justify-between items-start mb-6">
            <div>
              <p class="text-xs opacity-60 uppercase tracking-widest">SCpay Virtual</p>
            </div>
            <span class="text-xl font-black italic opacity-80">VISA</span>
          </div>
          <p class="font-mono text-lg tracking-[0.2em] mb-5" id="card-preview">•••• •••• •••• ••••</p>
          <div class="flex justify-between text-sm">
            <div>
              <p class="text-xs opacity-60 uppercase tracking-wide">Expiration</p>
              <p class="font-mono" id="expiry-preview">MM/AAAA</p>
            </div>
            <div class="text-right">
              <p class="text-xs opacity-60 uppercase tracking-wide">CVV</p>
              <p class="font-mono" id="cvv-preview">•••</p>
            </div>
          </div>
        </div>

        <!-- Card number -->
        <div>
          <label class="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">Numéro de carte</label>
          <input type="text" name="card_number" id="card-number-input"
                 placeholder="1234 5678 9012 3456"
                 maxlength="19"
                 class="card-input w-full border border-gray-300 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-rose-500 focus:border-transparent transition"
                 value="{{ old('card_number') }}"
                 autocomplete="cc-number">
        </div>

        <div class="grid grid-cols-2 gap-4">
          <!-- Expiry -->
          <div>
            <label class="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">Expiration</label>
            <input type="text" name="expiry" id="expiry-input"
                   placeholder="MM/AAAA"
                   maxlength="7"
                   class="card-input w-full border border-gray-300 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-rose-500 focus:border-transparent transition"
                   value="{{ old('expiry') }}"
                   autocomplete="cc-exp">
          </div>
          <!-- CVV -->
          <div>
            <label class="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">CVV</label>
            <input type="text" name="cvv" id="cvv-input"
                   placeholder="•••"
                   maxlength="4"
                   class="card-input w-full border border-gray-300 rounded-xl px-4 py-3 text-gray-900 focus:outline-none focus:ring-2 focus:ring-rose-500 focus:border-transparent transition"
                   autocomplete="cc-csc">
          </div>
        </div>

        <!-- Summary -->
        <div class="bg-gray-50 rounded-xl p-4 flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">Total à payer</p>
            <p class="text-2xl font-bold text-gray-900 tabular-nums" id="total-display">
              {{ number_format($products[0]['price'], 0, ',', ' ') }} MGA
            </p>
          </div>
          <button type="submit"
                  class="bg-rose-600 hover:bg-rose-700 active:scale-95 text-white font-semibold px-6 py-3 rounded-xl transition-all flex items-center gap-2 shadow-sm hover:shadow-md">
            <span class="material-symbols-outlined text-[18px]">contactless</span>
            Payer
          </button>
        </div>

        <p class="text-xs text-center text-gray-400 flex items-center justify-center gap-1">
          <span class="material-symbols-outlined text-[14px]">lock</span>
          Paiement sécurisé — confirmation requise sur votre application mobile
        </p>
      </form>
    </div>
  </div>
</div>

<script>
const prices = @json(collect($products)->pluck('price', 'id'));

// Sync product selection → hidden input + price display
document.querySelectorAll('input[name="selected_product"]').forEach(radio => {
  radio.addEventListener('change', () => {
    document.getElementById('product_id_input').value = radio.value;
    const price = prices[radio.value];
    document.getElementById('total-display').textContent =
      new Intl.NumberFormat('fr-FR').format(price) + ' MGA';
  });
});

// Card number formatting
document.getElementById('card-number-input').addEventListener('input', function() {
  let v = this.value.replace(/\D/g, '').substring(0, 16);
  this.value = v.replace(/(.{4})/g, '$1 ').trim();
  const masked = (v + '????????????????').substring(0, 16).replace(/(.{4})/g, '$1 ').trim();
  document.getElementById('card-preview').textContent = masked.replace(/[0-9]/g, '•');
  // Show last 4 live
  if (v.length >= 4) {
    const preview = '•••• •••• •••• ' + v.slice(-4);
    document.getElementById('card-preview').textContent = preview;
  }
});

// Expiry formatting (accepts MM/YY or MM/YYYY)
document.getElementById('expiry-input').addEventListener('input', function() {
  let v = this.value.replace(/\D/g, '').substring(0, 6);
  if (v.length >= 3) v = v.substring(0, 2) + '/' + v.substring(2);
  this.value = v;
  document.getElementById('expiry-preview').textContent = v || 'MM/AAAA';
});

// CVV
document.getElementById('cvv-input').addEventListener('input', function() {
  this.value = this.value.replace(/\D/g, '').substring(0, 4);
  document.getElementById('cvv-preview').textContent = '•'.repeat(this.value.length) || '•••';
});
</script>
</body>
</html>
