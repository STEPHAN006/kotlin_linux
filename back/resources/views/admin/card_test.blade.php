@extends('admin.layout')
@section('title', 'Test paiement carte')
@section('page-title', 'Simulateur de paiement par carte')

@section('content')

@php
    $merchants = [
        ['name' => 'Shoprite Ankorondrano',  'category' => 'groceries',     'icon' => 'shopping_cart',    'amount' => 85000],
        ['name' => 'Shell Ivandry',           'category' => 'transport',     'icon' => 'local_gas_station','amount' => 120000],
        ['name' => 'Netflix',                 'category' => 'subscription',  'icon' => 'play_circle',      'amount' => 55000],
        ['name' => 'Hôtel Colbert Tana',      'category' => 'hotel',         'icon' => 'hotel',            'amount' => 980000],
        ['name' => 'Jumia Madagascar',         'category' => 'ecommerce',     'icon' => 'storefront',       'amount' => 250000],
        ['name' => 'Pizza Gasy',              'category' => 'restaurant',    'icon' => 'restaurant',       'amount' => 45000],
        ['name' => 'Taxibe',                  'category' => 'transport',     'icon' => 'directions_bus',   'amount' => 5000],
        ['name' => 'Air Madagascar',          'category' => 'travel',        'icon' => 'flight',           'amount' => 1200000],
    ];

    $categories = [
        'groceries'    => 'Courses',
        'transport'    => 'Transport',
        'restaurant'   => 'Restaurant',
        'ecommerce'    => 'E-commerce',
        'subscription' => 'Abonnement',
        'hotel'        => 'Hôtel',
        'travel'       => 'Voyage',
        'other'        => 'Autre',
    ];

    $result = session('test_result');
@endphp

<div class="grid grid-cols-1 lg:grid-cols-3 gap-6" x-data="{
    selectedCard: '{{ $virtualCards->first()?->id ?? '' }}',
    merchant: '',
    category: 'other',
    amount: '',
    setMerchant(name, category, amount) {
        this.merchant = name;
        this.category = category;
        this.amount = amount;
    }
}">

    {{-- ── LEFT: form ──────────────────────────────────────────────────────── --}}
    <div class="lg:col-span-2 space-y-5">

        {{-- Result banner --}}
        @if($result)
        <div class="card p-5 border-2 {{ $result['status'] === 'approved' ? 'border-emerald-300 bg-emerald-50' : 'border-red-300 bg-red-50' }}">
            <div class="flex items-start gap-4">
                <div class="w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0
                            {{ $result['status'] === 'approved' ? 'bg-emerald-100' : 'bg-red-100' }}">
                    <span class="material-symbols-outlined text-2xl {{ $result['status'] === 'approved' ? 'text-emerald-600' : 'text-red-600' }}">
                        {{ $result['status'] === 'approved' ? 'check_circle' : 'cancel' }}
                    </span>
                </div>
                <div class="flex-1">
                    <p class="font-bold text-headline-sm {{ $result['status'] === 'approved' ? 'text-emerald-700' : 'text-red-700' }}">
                        {{ $result['status'] === 'approved' ? 'Paiement accepté' : 'Paiement refusé' }}
                    </p>
                    <p class="text-body-md text-on-surface-variant mt-0.5">{{ $result['reason'] }}</p>

                    {{-- Receipt --}}
                    <div class="mt-4 bg-white rounded-lg border border-gray-200 p-4 grid grid-cols-2 gap-x-6 gap-y-2 text-body-md">
                        <div>
                            <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Commerçant</p>
                            <p class="font-medium text-on-surface">{{ $result['merchant'] }}</p>
                        </div>
                        <div>
                            <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Montant</p>
                            <p class="font-bold text-on-surface tnum">{{ number_format($result['amount'], 0, ',', ' ') }} MGA</p>
                        </div>
                        <div>
                            <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Carte</p>
                            <p class="font-medium text-on-surface tnum">{{ $result['card_masked'] }}</p>
                        </div>
                        <div>
                            <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Titulaire</p>
                            <p class="font-medium text-on-surface">{{ $result['holder'] }}</p>
                        </div>
                        <div>
                            <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Code réponse</p>
                            <p class="font-mono font-bold {{ $result['status'] === 'approved' ? 'text-emerald-600' : 'text-red-600' }}">
                                {{ $result['code'] }}
                            </p>
                        </div>
                        @if($result['auth_code'])
                        <div>
                            <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Code d'autorisation</p>
                            <p class="font-mono font-bold text-emerald-600">{{ $result['auth_code'] }}</p>
                        </div>
                        @endif
                        <div class="col-span-2">
                            <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Horodatage</p>
                            <p class="text-on-surface tnum">{{ $result['at'] }}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        @endif

        {{-- Quick-pick merchants --}}
        <div class="card p-5">
            <p class="text-label-md text-on-surface-variant uppercase tracking-wide mb-3">Commerçants rapides</p>
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-2">
                @foreach($merchants as $m)
                <button type="button"
                        @click="setMerchant('{{ $m['name'] }}', '{{ $m['category'] }}', {{ $m['amount'] }})"
                        class="flex flex-col items-center gap-1.5 p-3 rounded-xl border border-gray-100 hover:border-primary-container hover:bg-surface-container-low transition-all text-center group">
                    <span class="material-symbols-outlined text-2xl text-on-surface-variant group-hover:text-primary-container transition-colors">
                        {{ $m['icon'] }}
                    </span>
                    <span class="text-[11px] font-medium text-on-surface leading-tight">{{ $m['name'] }}</span>
                    <span class="text-[10px] text-on-surface-variant tnum">{{ number_format($m['amount'], 0, ',', ' ') }} MGA</span>
                </button>
                @endforeach
            </div>
        </div>

        {{-- Payment form --}}
        <div class="card p-5">
            <p class="text-label-md text-on-surface-variant uppercase tracking-wide mb-4">Détails du paiement</p>
            <form method="POST" action="{{ route('admin.cards.test.process') }}" class="space-y-4">
                @csrf

                {{-- Card selector --}}
                <div>
                    <label class="text-label-md text-on-surface-variant uppercase tracking-wide block mb-1.5">
                        Carte virtuelle
                    </label>
                    @if($virtualCards->isEmpty())
                        <p class="text-body-md text-amber-600 bg-amber-50 rounded-lg px-4 py-3 border border-amber-200">
                            <span class="material-symbols-outlined text-[16px] align-middle">warning</span>
                            Aucune carte virtuelle dans le système.
                        </p>
                    @else
                        <select name="card_id" x-model="selectedCard" required
                                class="input w-full">
                            @foreach($virtualCards as $card)
                            <option value="{{ $card->id }}">
                                {{ $card->masked_number }}
                                @if($card->account?->user) — {{ $card->account->user->name }} @endif
                                @if($card->is_blocked) [BLOQUÉE] @endif
                                — Limite {{ number_format($card->daily_limit, 0, ',', ' ') }} MGA/j
                            </option>
                            @endforeach
                        </select>
                    @endif
                    @error('card_id') <p class="text-red-500 text-body-md mt-1">{{ $message }}</p> @enderror
                </div>

                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {{-- Merchant --}}
                    <div>
                        <label class="text-label-md text-on-surface-variant uppercase tracking-wide block mb-1.5">
                            Nom du commerçant
                        </label>
                        <input type="text" name="merchant" x-model="merchant"
                               placeholder="ex : Amazon, Shell…"
                               required maxlength="100"
                               class="input w-full">
                        @error('merchant') <p class="text-red-500 text-body-md mt-1">{{ $message }}</p> @enderror
                    </div>

                    {{-- Category --}}
                    <div>
                        <label class="text-label-md text-on-surface-variant uppercase tracking-wide block mb-1.5">
                            Catégorie
                        </label>
                        <select name="category" x-model="category" required class="input w-full">
                            @foreach($categories as $val => $label)
                            <option value="{{ $val }}">{{ $label }}</option>
                            @endforeach
                        </select>
                        @error('category') <p class="text-red-500 text-body-md mt-1">{{ $message }}</p> @enderror
                    </div>
                </div>

                {{-- Amount --}}
                <div>
                    <label class="text-label-md text-on-surface-variant uppercase tracking-wide block mb-1.5">
                        Montant (MGA)
                    </label>
                    <div class="relative">
                        <input type="number" name="amount" x-model="amount"
                               min="100" max="50000000" step="100"
                               placeholder="0"
                               required
                               class="input w-full pr-16 tnum">
                        <span class="absolute right-3 top-1/2 -translate-y-1/2 text-label-md text-on-surface-variant font-semibold">MGA</span>
                    </div>
                    @error('amount') <p class="text-red-500 text-body-md mt-1">{{ $message }}</p> @enderror
                </div>

                <button type="submit"
                        class="btn-primary w-full py-3 text-body-md font-semibold flex items-center justify-center gap-2"
                        :disabled="!selectedCard || !merchant || !amount">
                    <span class="material-symbols-outlined text-[18px]">contactless</span>
                    Simuler le paiement
                </button>
            </form>
        </div>
    </div>

    {{-- ── RIGHT: history + selected card info ────────────────────────────── --}}
    <div class="space-y-5">

        {{-- Selected card preview --}}
        <div class="card p-5" x-show="selectedCard">
            <p class="text-label-md text-on-surface-variant uppercase tracking-wide mb-3">Carte sélectionnée</p>
            @foreach($virtualCards as $card)
            <div x-show="selectedCard == '{{ $card->id }}'" x-cloak>
                {{-- Mini card visual --}}
                <div class="rounded-xl p-4 text-white text-sm font-medium mb-3"
                     style="background: linear-gradient(135deg, #0D0D0D, #3B1F6E, #00BFA5)">
                    <div class="flex justify-between items-center mb-4">
                        <span class="text-xs opacity-70">SCpay Virtual</span>
                        <span class="text-xs opacity-70">{{ $card->type === 'virtual' ? 'Virtuelle' : 'Physique' }}</span>
                    </div>
                    <p class="font-mono tracking-widest text-base">{{ $card->masked_number }}</p>
                    <div class="flex justify-between items-end mt-4">
                        <div>
                            <p class="text-[10px] opacity-60 uppercase">Expiration</p>
                            <p class="font-mono">{{ $card->expiry_date ? $card->expiry_date->format('m/Y') : '—' }}</p>
                        </div>
                        <span class="text-xl font-black italic opacity-80">VISA</span>
                    </div>
                </div>

                <div class="space-y-2 text-body-md">
                    <div class="flex justify-between">
                        <span class="text-on-surface-variant">Titulaire</span>
                        <span class="font-medium text-on-surface">{{ $card->account?->user?->name ?? '—' }}</span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-on-surface-variant">Limite / jour</span>
                        <span class="font-medium text-on-surface tnum">{{ number_format($card->daily_limit, 0, ',', ' ') }} MGA</span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-on-surface-variant">Statut</span>
                        <span class="badge {{ $card->is_blocked ? 'badge-frozen' : 'badge-active' }}">
                            {{ $card->is_blocked ? 'Bloquée' : 'Active' }}
                        </span>
                    </div>
                </div>
            </div>
            @endforeach
        </div>

        {{-- History --}}
        <div class="card overflow-hidden">
            <div class="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
                <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Historique de session</p>
                @if(count($history) > 0)
                <form method="POST" action="{{ route('admin.cards.test.process') }}">
                    @csrf
                    <input type="hidden" name="_clear_history" value="1">
                    {{-- handled in controller if needed --}}
                </form>
                @endif
            </div>

            @forelse($history as $h)
            <div class="px-5 py-3 border-b border-gray-50 last:border-0">
                <div class="flex items-center justify-between gap-2">
                    <div class="flex items-center gap-2 min-w-0">
                        <span class="material-symbols-outlined text-[18px] flex-shrink-0 {{ $h['status'] === 'approved' ? 'text-emerald-500' : 'text-red-400' }}">
                            {{ $h['status'] === 'approved' ? 'check_circle' : 'cancel' }}
                        </span>
                        <div class="min-w-0">
                            <p class="text-body-md font-medium text-on-surface truncate">{{ $h['merchant'] }}</p>
                            <p class="text-[11px] text-on-surface-variant tnum">{{ $h['card_masked'] }}</p>
                        </div>
                    </div>
                    <div class="text-right flex-shrink-0">
                        <p class="text-body-md font-bold tnum {{ $h['status'] === 'approved' ? 'text-on-surface' : 'text-red-400 line-through' }}">
                            {{ number_format($h['amount'], 0, ',', ' ') }}
                        </p>
                        <p class="text-[11px] text-on-surface-variant">{{ $h['at'] }}</p>
                    </div>
                </div>
                @if($h['status'] === 'declined')
                <p class="text-[11px] text-red-500 mt-1 ml-7">{{ $h['reason'] }}</p>
                @endif
            </div>
            @empty
            <div class="px-5 py-8 text-center">
                <span class="material-symbols-outlined text-3xl text-gray-300 block mb-2">history</span>
                <p class="text-body-md text-on-surface-variant">Aucun test effectué</p>
            </div>
            @endforelse
        </div>
    </div>
</div>

@endsection
