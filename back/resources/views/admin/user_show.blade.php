@extends('admin.layout')
@section('title', $user->name)
@section('page-title', 'Profil Utilisateur')

@push('scripts')
<style>
    .card-flip-scene { perspective: 1200px; }
    .card-flipper {
        position: relative; height: 196px;
        transform-style: preserve-3d;
        transition: transform 0.65s cubic-bezier(0.23, 1, 0.32, 1);
    }
    .card-flipper.is-flipped { transform: rotateY(180deg); }
    .card-face {
        position: absolute; inset: 0;
        backface-visibility: hidden;
        border-radius: 18px;
        overflow: hidden;
    }
    .card-face-back { transform: rotateY(180deg); }
    @keyframes shimmer { 0%,100%{opacity:.9} 50%{opacity:.6} }
    .chip-shimmer { animation: shimmer 2s ease-in-out infinite; }
</style>
@endpush

@section('content')
<div x-data="{ modal: null }">

@php
$gradients = [
    ['#b80035','#ff6b6b'],
    ['#3b82f6','#06b6d4'],
    ['#10b981','#34d399'],
    ['#8b5cf6','#a78bfa'],
    ['#f59e0b','#fcd34d'],
    ['#ec4899','#f9a8d4'],
    ['#6366f1','#818cf8'],
    ['#0ea5e9','#38bdf8'],
];
$gi = ord($user->name[0]) % count($gradients);
$g1 = $gradients[$gi][0]; $g2 = $gradients[$gi][1];

$cardDesigns = [
    0 => ['from' => '#171717', 'to' => '#2D3038', 'label' => 'Classic'],
    1 => ['from' => '#7D56F4', 'mid' => '#28D0A0', 'to' => '#1E3C72', 'label' => 'Solana'],
    2 => ['from' => '#11998E', 'to' => '#38EF7D', 'label' => 'Nature'],
];
@endphp

<!-- Header with back button & actions -->
<div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
    <div class="flex items-center gap-4">
        <a href="{{ route('admin.users') }}"
           class="w-10 h-10 rounded-lg border border-gray-200 flex items-center justify-center hover:bg-gray-50 transition-all flex-shrink-0">
            <span class="material-symbols-outlined text-on-surface">arrow_back</span>
        </a>
        <div>
            <div class="flex flex-wrap items-center gap-2">
                <h2 class="text-headline-md text-on-surface">{{ $user->name }}</h2>
                <span class="badge {{ $user->is_active ? 'badge-active' : 'badge-frozen' }}">
                    {{ $user->is_active ? 'Actif' : 'Gelé' }}
                </span>
                @if($user->role === 'admin')
                <span class="badge bg-blue-100 text-blue-700">Admin</span>
                @endif
            </div>
            <p class="text-label-md text-on-surface-variant">ID Utilisateur: #USR-{{ str_pad($user->id, 6, '0', STR_PAD_LEFT) }}</p>
        </div>
    </div>
    @if($user->role !== 'admin')
    {{-- Hidden forms with IDs — triggered by modal confirm --}}
    <form id="form-toggle" method="POST" action="{{ route('admin.users.toggle', $user->id) }}" class="hidden">
        @csrf @method('PATCH')
    </form>
    <form id="form-reset" method="POST" action="{{ route('admin.users.reset-password', $user->id) }}" class="hidden">
        @csrf
    </form>
    <form id="form-delete" method="POST" action="{{ route('admin.users.destroy', $user->id) }}" class="hidden">
        @csrf @method('DELETE')
    </form>

    <div class="flex items-center gap-2 flex-wrap">
        <button type="button" @click="modal = 'toggle'"
                class="px-4 py-2 rounded-lg border border-gray-200 text-on-surface text-label-md font-semibold hover:bg-gray-50 transition-all flex items-center gap-2 active:scale-95 h-10">
            <span class="material-symbols-outlined text-[18px]">ac_unit</span>
            {{ $user->is_active ? 'Geler' : 'Activer' }}
        </button>
        <button type="button" @click="modal = 'reset'"
                class="px-4 py-2 rounded-lg border border-gray-200 text-on-surface text-label-md font-semibold hover:bg-gray-50 transition-all flex items-center gap-2 active:scale-95 h-10">
            <span class="material-symbols-outlined text-[18px]">lock_reset</span> Réinitialiser MDP
        </button>
        <button type="button" @click="modal = 'delete'"
                class="px-4 py-2 rounded-lg bg-primary text-white text-label-md font-semibold hover:bg-primary-container transition-all flex items-center gap-2 active:scale-95 h-10">
            <span class="material-symbols-outlined text-[18px]">delete</span> Supprimer
        </button>
    </div>
    @endif
</div>

<!-- Profile Overview Card -->
<div class="bg-white border border-gray-100 rounded-xl p-6 flex flex-wrap items-center gap-8 shadow-card mb-6">
    <div class="w-24 h-24 rounded-2xl overflow-hidden flex-shrink-0 flex items-center justify-center text-white text-3xl font-bold"
         style="background: linear-gradient(135deg, {{ $g1 }}, {{ $g2 }})">
        @if($user->avatar_url)
        <img src="{{ $user->avatar_url }}" class="w-full h-full object-cover" alt="{{ $user->name }}">
        @else
        {{ strtoupper(substr($user->name, 0, 2)) }}
        @endif
    </div>
    <div class="flex-1 min-w-[280px] grid grid-cols-2 md:grid-cols-4 gap-6">
        <div>
            <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Email Principal</p>
            <p class="text-body-md font-semibold text-on-surface mt-1 truncate">{{ $user->email }}</p>
        </div>
        <div>
            <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Comptes liés</p>
            <p class="text-body-md font-semibold text-on-surface mt-1">{{ $user->accounts->count() }} Compte(s)</p>
        </div>
        <div>
            <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Cartes Actives</p>
            <p class="text-body-md font-semibold text-on-surface mt-1">
                {{ $user->accounts->sum(fn($a) => $a->cards->count()) }} Carte(s)
            </p>
        </div>
        <div>
            <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Solde Total</p>
            <p class="text-headline-sm text-primary mt-1 tnum font-bold">
                {{ number_format($user->accounts->sum('balance'), 0, ',', ' ') }} MGA
            </p>
        </div>
    </div>
</div>

<!-- 3-column layout -->
<div class="grid grid-cols-1 xl:grid-cols-3 gap-5 mb-6">

    <!-- Left column: Edit form + Infos compte -->
    @if($user->role !== 'admin')
    <div class="xl:col-span-1 space-y-5">

        <!-- Edit Profile Form -->
        <section class="bg-white border border-gray-100 rounded-xl p-6 shadow-card">
            <h3 class="text-label-md text-on-surface-variant uppercase tracking-wide mb-5 flex items-center gap-2">
                <span class="material-symbols-outlined text-[18px]">edit</span> Modifier le profil
            </h3>
            <form method="POST" action="{{ route('admin.users.update', $user->id) }}" class="space-y-4">
                @csrf @method('PATCH')
                <div>
                    <label class="block text-label-md text-on-surface-variant mb-1.5">Nom complet</label>
                    <input type="text" name="name" value="{{ old('name', $user->name) }}" required
                           class="input w-full bg-gray-50 focus:bg-white">
                </div>
                <div>
                    <label class="block text-label-md text-on-surface-variant mb-1.5">Adresse Email</label>
                    <input type="email" name="email" value="{{ old('email', $user->email) }}" required
                           class="input w-full bg-gray-50 focus:bg-white">
                </div>
                <div>
                    <label class="block text-label-md text-on-surface-variant mb-1.5">Téléphone</label>
                    <input type="text" name="phone" value="{{ old('phone', $user->phone) }}"
                           class="input w-full bg-gray-50 focus:bg-white">
                </div>
                <button type="submit" class="btn-primary w-full py-3 text-body-md flex items-center justify-center gap-1.5 mt-1">
                    <span class="material-symbols-outlined text-[16px]">save</span> Sauvegarder les modifications
                </button>
            </form>
        </section>

        <!-- Infos Compte -->
        <section class="bg-white border border-gray-100 rounded-xl p-6 shadow-card">
            <h3 class="text-label-md text-on-surface-variant uppercase tracking-wide mb-5 flex items-center gap-2">
                <span class="material-symbols-outlined text-[18px]">info</span> Infos Compte
            </h3>
            <div class="space-y-1">
                <div class="flex justify-between items-center py-3 border-b border-dashed border-gray-100">
                    <span class="text-body-md text-on-surface-variant">ID Interne</span>
                    <span class="text-label-md font-bold text-on-surface">#{{ str_pad($user->id, 6, '0', STR_PAD_LEFT) }}</span>
                </div>
                <div class="flex justify-between items-center py-3 border-b border-dashed border-gray-100">
                    <span class="text-body-md text-on-surface-variant">Date d'inscription</span>
                    <span class="text-label-md font-bold text-on-surface tnum">{{ $user->created_at->format('d M Y') }}</span>
                </div>
                <div class="flex justify-between items-center py-3">
                    <span class="text-body-md text-on-surface-variant">Dernière MAJ</span>
                    <span class="text-label-md font-bold text-on-surface tnum">{{ $user->updated_at->format('d/m/Y H:i') }}</span>
                </div>
            </div>
        </section>
    </div>
    @endif

    <!-- Right column: Accounts + Transactions -->
    <div class="{{ $user->role !== 'admin' ? 'xl:col-span-2' : 'xl:col-span-3' }} space-y-5">

        <!-- Bank Accounts -->
        <section class="bg-white border border-gray-100 rounded-xl p-6 shadow-card">
            <div class="flex justify-between items-center mb-5">
                <h3 class="text-label-md text-on-surface-variant uppercase tracking-wide flex items-center gap-2">
                    <span class="material-symbols-outlined text-[18px]">account_balance</span> Comptes bancaires
                </h3>
                <span class="text-label-md text-on-surface-variant">{{ $user->accounts->count() }} compte(s)</span>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                @forelse($user->accounts as $index => $account)
                <div class="p-4 rounded-xl border-2 {{ $index === 0 ? 'border-primary bg-red-50/30' : 'border-gray-100 hover:border-gray-200' }} flex flex-col justify-between min-h-[130px] relative overflow-hidden group transition-all">
                    @if($index === 0)
                    <div class="absolute -right-4 -top-4 opacity-10 group-hover:scale-110 transition-transform">
                        <span class="material-symbols-outlined text-8xl text-primary">account_balance_wallet</span>
                    </div>
                    @endif
                    <div class="flex justify-between items-start z-10 relative">
                        <div>
                            <p class="text-label-md font-bold text-on-surface">{{ ucfirst($account->type) }}
                                <span class="font-normal text-on-surface-variant">· {{ $account->currency }}</span>
                            </p>
                            <p class="text-[10px] text-on-surface-variant mt-0.5">{{ $account->number }}</p>
                        </div>
                        <div class="flex items-center gap-1.5">
                            @if($index === 0)
                            <span class="px-2 py-0.5 rounded-full bg-primary text-white text-[9px] font-bold uppercase">Principal</span>
                            @endif
                            <form method="POST" action="{{ route('admin.users.accounts.toggle', [$user->id, $account->id]) }}">
                                @csrf @method('PATCH')
                                <button class="text-[9px] font-bold px-2 py-0.5 rounded-full border transition-all
                                               {{ $account->status === 'active' ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : 'bg-amber-50 text-amber-700 border-amber-200' }}"
                                        title="{{ $account->status === 'active' ? 'Suspendre' : 'Activer' }}">
                                    {{ strtoupper($account->status === 'active' ? 'Actif' : 'Suspendu') }}
                                </button>
                            </form>
                        </div>
                    </div>
                    <div class="z-10 relative mt-3">
                        <p class="text-headline-sm text-on-surface tnum font-bold">
                            {{ number_format($account->balance, 0, ',', ' ') }} MGA
                        </p>
                        <p class="text-[10px] text-on-surface-variant mt-0.5">{{ $account->transactions_count }} transactions</p>
                    </div>
                </div>
                @empty
                <div class="col-span-2 py-8 text-center">
                    <span class="material-symbols-outlined text-3xl text-gray-300 block mb-2">account_balance</span>
                    <p class="text-body-md text-on-surface-variant">Aucun compte</p>
                </div>
                @endforelse
            </div>
        </section>

        <!-- Recent Transactions -->
        <section class="bg-white border border-gray-100 rounded-xl p-6 shadow-card">
            <div class="flex justify-between items-center mb-5">
                <h3 class="text-label-md text-on-surface-variant uppercase tracking-wide flex items-center gap-2">
                    <span class="material-symbols-outlined text-[18px]">history</span> Transactions récentes
                </h3>
                <a href="{{ route('admin.transactions') }}" class="text-label-md text-primary hover:underline">Voir tout</a>
            </div>
            <div class="overflow-x-auto">
                <table class="w-full text-left">
                    <thead class="border-b border-gray-100">
                        <tr>
                            <th class="pb-3 text-label-md text-on-surface-variant uppercase">Détails</th>
                            <th class="pb-3 text-label-md text-on-surface-variant uppercase">Date</th>
                            <th class="pb-3 text-label-md text-on-surface-variant uppercase">Statut</th>
                            <th class="pb-3 text-label-md text-on-surface-variant uppercase text-right">Montant</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-50">
                        @forelse($recentTransactions as $tx)
                        <tr class="hover:bg-gray-50 transition-colors">
                            <td class="py-4">
                                <div class="flex items-center gap-3">
                                    <div class="w-8 h-8 rounded-full {{ $tx->type === 'credit' ? 'bg-emerald-50' : 'bg-red-50' }} flex items-center justify-center flex-shrink-0">
                                        <span class="material-symbols-outlined text-[14px] {{ $tx->type === 'credit' ? 'text-emerald-500' : 'text-primary' }}">
                                            {{ $tx->type === 'credit' ? 'arrow_downward' : 'arrow_upward' }}
                                        </span>
                                    </div>
                                    <div>
                                        <p class="text-label-md font-bold text-on-surface">{{ $tx->description ?? ucfirst($tx->category ?? $tx->type) }}</p>
                                        <p class="text-[10px] text-on-surface-variant">{{ ucfirst($tx->type) }}</p>
                                    </div>
                                </div>
                            </td>
                            <td class="py-4 text-body-md text-on-surface-variant">{{ $tx->created_at->format('d/m/Y H:i') }}</td>
                            <td class="py-4">
                                <span class="badge {{ $tx->type === 'credit' ? 'badge-credit' : 'badge-debit' }}">
                                    {{ $tx->type === 'credit' ? 'Crédit' : 'Débit' }}
                                </span>
                            </td>
                            <td class="py-4 text-right text-label-md font-bold tnum {{ $tx->type === 'credit' ? 'text-emerald-600' : 'text-primary' }}">
                                {{ $tx->type === 'credit' ? '+' : '-' }}{{ number_format($tx->amount, 0, ',', ' ') }} MGA
                            </td>
                        </tr>
                        @empty
                        <tr>
                            <td colspan="4" class="py-8 text-center">
                                <span class="material-symbols-outlined text-3xl text-gray-300 block mb-2">receipt_long</span>
                                <p class="text-body-md text-on-surface-variant">Aucune transaction</p>
                            </td>
                        </tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </section>

    </div>
</div>

<!-- Cards section -->
@php
$allCards = $user->accounts->flatMap(fn($a) => $a->cards);
@endphp

@if($allCards->count())
<section class="space-y-4">
    <div class="flex items-center gap-2">
        <span class="material-symbols-outlined text-on-surface-variant text-[18px]">credit_card</span>
        <h3 class="text-label-md text-on-surface-variant uppercase tracking-wide">
            Cartes de {{ $user->name }}
        </h3>
        <span class="text-label-md text-on-surface-variant font-normal normal-case">· {{ $allCards->count() }} carte(s)</span>
        <span class="ml-auto text-label-md text-on-surface-variant italic font-normal normal-case">Cliquer sur une carte pour la retourner</span>
    </div>

    <div class="flex gap-5 overflow-x-auto pb-4" style="scrollbar-width: thin;">
        @foreach($allCards as $card)
        @php
            $di = $card->id % 3;
            $design = $cardDesigns[$di];
            $gradCss = isset($design['mid'])
                ? "linear-gradient(135deg, {$design['from']}, {$design['mid']}, {$design['to']})"
                : "linear-gradient(135deg, {$design['from']}, {$design['to']})";
        @endphp

        <div class="flex-shrink-0 w-[340px]" x-data="{ flipped: false }">

            <!-- Flip container -->
            <div class="card-flip-scene cursor-pointer select-none" @click="flipped = !flipped">
                <div class="card-flipper w-full" :class="{ 'is-flipped': flipped }">

                    <!-- FRONT -->
                    <div class="card-face w-full h-full p-6 flex flex-col justify-between"
                         style="background: {{ $gradCss }}">
                        <div class="flex items-start justify-between">
                            <div>
                                <p class="text-white/70 text-xs font-semibold uppercase tracking-widest">SCpay</p>
                                <p class="text-white/50 text-[11px] mt-0.5">{{ $card->type === 'virtual' ? 'Virtuelle' : 'Physique' }}</p>
                            </div>
                            @if($card->is_blocked)
                            <span class="bg-black/30 text-white/90 text-[10px] font-bold px-2 py-1 rounded-full uppercase tracking-wide">Bloquée</span>
                            @else
                            <div class="w-8 h-6 rounded bg-amber-400/80 chip-shimmer flex items-center justify-center">
                                <div class="w-5 h-4 rounded-sm border border-amber-300/60 grid grid-cols-2 gap-px p-0.5">
                                    <div class="bg-amber-300/40 rounded-[1px]"></div>
                                    <div class="bg-amber-300/40 rounded-[1px]"></div>
                                    <div class="bg-amber-300/40 rounded-[1px]"></div>
                                    <div class="bg-amber-300/40 rounded-[1px]"></div>
                                </div>
                            </div>
                            @endif
                        </div>
                        <div>
                            <p class="text-white text-xl font-semibold tracking-[0.2em] tnum">{{ $card->masked_number }}</p>
                        </div>
                        <div class="flex items-end justify-between">
                            <div class="space-y-1">
                                <div>
                                    <p class="text-white/60 text-[10px] uppercase tracking-widest">Valide jusqu'au</p>
                                    <p class="text-white font-bold text-sm tnum">{{ $card->expiry_date ? $card->expiry_date->format('m/Y') : '••/••' }}</p>
                                </div>
                                <div>
                                    <p class="text-white/60 text-[10px] uppercase tracking-widest">CVV</p>
                                    <p class="text-white font-bold text-sm">•••</p>
                                </div>
                            </div>
                            <div class="text-right">
                                <p class="text-white/70 text-[10px] uppercase tracking-widest mb-1">Limite / jour</p>
                                <p class="text-white font-semibold text-sm tnum">{{ number_format($card->daily_limit, 0, ',', ' ') }} MGA</p>
                                <p class="text-white font-black text-2xl leading-none mt-1" style="font-style: italic;">VISA</p>
                            </div>
                        </div>
                    </div>

                    <!-- BACK -->
                    <div class="card-face card-face-back w-full h-full flex flex-col"
                         style="background: {{ $gradCss }}">
                        <div class="h-10 bg-black/80 mt-6"></div>
                        <div class="mx-6 mt-5 flex items-center gap-3">
                            <div class="flex-1 h-9 bg-white/90 rounded flex items-center px-3">
                                <span class="text-gray-400 text-xs italic">Signature autorisée</span>
                                <span class="ml-auto text-gray-700 text-sm font-mono font-bold">•••</span>
                            </div>
                        </div>
                        <div class="mt-auto mx-6 mb-5 flex items-end justify-between">
                            <div>
                                <p class="text-white/60 text-[10px] uppercase tracking-widest mb-0.5">Titulaire</p>
                                <p class="text-white font-semibold text-sm uppercase tracking-wide">{{ strtoupper($user->name) }}</p>
                            </div>
                            <p class="text-white font-black text-3xl leading-none" style="font-style: italic;">VISA</p>
                        </div>
                    </div>

                </div>
            </div>

            <!-- Card actions -->
            <div class="mt-3 flex items-center gap-2">
                <form method="POST" action="{{ route('admin.cards.toggle', $card->id) }}" class="flex-1">
                    @csrf @method('PATCH')
                    <button class="w-full py-2 text-body-md font-medium rounded-xl border transition-all flex items-center justify-center gap-1.5
                                   {{ $card->is_blocked
                                       ? 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border-emerald-200'
                                       : 'bg-amber-50 text-amber-700 hover:bg-amber-100 border-amber-200' }}">
                        <span class="material-symbols-outlined text-[16px]">{{ $card->is_blocked ? 'lock_open' : 'lock' }}</span>
                        {{ $card->is_blocked ? 'Débloquer' : 'Bloquer' }}
                    </button>
                </form>
                <div x-data="{ editing: false }" class="relative">
                    <button @click="editing = !editing"
                            class="btn-secondary w-10 h-10 flex items-center justify-center rounded-xl"
                            title="Modifier la limite">
                        <span class="material-symbols-outlined text-[18px]">tune</span>
                    </button>
                    <div x-show="editing" x-cloak @click.outside="editing = false"
                         class="absolute bottom-12 right-0 bg-white rounded-xl shadow-modal border border-gray-100 p-4 w-64 z-20">
                        <p class="text-label-md text-on-surface-variant uppercase tracking-wide mb-2">Limite journalière</p>
                        <form method="POST" action="{{ route('admin.cards.limit', $card->id) }}" class="flex gap-2">
                            @csrf @method('PATCH')
                            <input type="number" name="daily_limit" value="{{ $card->daily_limit }}"
                                   min="0" step="1000" class="input flex-1 text-right tnum">
                            <button type="submit" class="btn-primary px-3 text-body-md">OK</button>
                        </form>
                    </div>
                </div>
            </div>

            <p class="text-center text-label-md text-on-surface-variant mt-2">
                {{ $design['label'] }} · {{ $card->masked_number }}
            </p>
        </div>
        @endforeach
    </div>
</section>
@endif

{{-- ── Confirmation modal ── --}}
<div x-show="modal !== null" x-cloak
     class="fixed inset-0 z-50 flex items-center justify-center p-4"
     @keydown.escape.window="modal = null"
     x-transition:enter="transition ease-out duration-200"
     x-transition:enter-start="opacity-0"
     x-transition:enter-end="opacity-100"
     x-transition:leave="transition ease-in duration-150"
     x-transition:leave-start="opacity-100"
     x-transition:leave-end="opacity-0">

    {{-- Backdrop --}}
    <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="modal = null"></div>

    {{-- Dialog card --}}
    <div class="relative bg-white rounded-2xl shadow-xl w-full max-w-sm p-6 flex flex-col gap-4 z-10"
         x-transition:enter="transition ease-out duration-200"
         x-transition:enter-start="opacity-0 scale-95"
         x-transition:enter-end="opacity-100 scale-100">

        {{-- Icon --}}
        <div class="w-14 h-14 rounded-full flex items-center justify-center mx-auto"
             :class="{
                 'bg-amber-50': modal === 'toggle',
                 'bg-blue-50':  modal === 'reset',
                 'bg-red-50':   modal === 'delete'
             }">
            <span class="material-symbols-outlined text-[28px]"
                  :class="{
                      'text-amber-600': modal === 'toggle',
                      'text-blue-600':  modal === 'reset',
                      'text-red-600':   modal === 'delete'
                  }">
                <template x-if="modal === 'toggle'"><span>ac_unit</span></template>
                <template x-if="modal === 'reset'"><span>lock_reset</span></template>
                <template x-if="modal === 'delete'"><span>delete_forever</span></template>
            </span>
        </div>

        {{-- Title & message --}}
        <div class="text-center space-y-1.5">
            <div x-show="modal === 'toggle'">
                <h3 class="text-headline-sm text-on-surface font-semibold">
                    {{ $user->is_active ? 'Geler le compte' : 'Activer le compte' }}
                </h3>
                <p class="text-body-md text-on-surface-variant">
                    {{ $user->is_active
                        ? 'Le compte de ' . $user->name . ' sera suspendu temporairement.'
                        : 'Le compte de ' . $user->name . ' sera réactivé.' }}
                </p>
            </div>
            <div x-show="modal === 'reset'">
                <h3 class="text-headline-sm text-on-surface font-semibold">Réinitialiser le mot de passe</h3>
                <p class="text-body-md text-on-surface-variant">
                    Un nouveau mot de passe temporaire sera généré pour <strong>{{ $user->name }}</strong>.
                </p>
            </div>
            <div x-show="modal === 'delete'">
                <h3 class="text-headline-sm text-on-surface font-semibold">Supprimer le compte</h3>
                <p class="text-body-md text-on-surface-variant">
                    Cette action est <strong>irréversible</strong>. Le compte de <strong>{{ $user->name }}</strong> et toutes ses données seront supprimés définitivement.
                </p>
            </div>
        </div>

        {{-- Actions --}}
        <div class="flex gap-3 mt-1">
            <button type="button" @click="modal = null"
                    class="btn-secondary flex-1 py-2.5 text-body-md flex items-center justify-center">
                Annuler
            </button>
            <button type="button"
                    @click="document.getElementById('form-' + modal).submit()"
                    class="flex-1 py-2.5 text-body-md font-semibold rounded-lg transition-all flex items-center justify-center gap-2"
                    :class="{
                        'bg-amber-500 hover:bg-amber-600 text-white': modal === 'toggle',
                        'btn-primary': modal === 'reset',
                        'bg-red-600 hover:bg-red-700 text-white': modal === 'delete'
                    }">
                <span class="material-symbols-outlined text-[16px]">
                    <template x-if="modal === 'toggle'"><span>ac_unit</span></template>
                    <template x-if="modal === 'reset'"><span>lock_reset</span></template>
                    <template x-if="modal === 'delete'"><span>delete</span></template>
                </span>
                <span x-text="modal === 'delete' ? 'Supprimer' : (modal === 'reset' ? 'Réinitialiser' : '{{ $user->is_active ? 'Geler' : 'Activer' }}')"></span>
            </button>
        </div>
    </div>
</div>

</div>{{-- end x-data modal --}}

@endsection
