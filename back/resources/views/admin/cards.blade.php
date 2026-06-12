@extends('admin.layout')
@section('title', 'Cartes')
@section('page-title', 'Gestion des Cartes')

@section('content')

<!-- Stats -->
<div class="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-5">
    <div class="card p-4 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-surface-container flex items-center justify-center flex-shrink-0">
            <span class="material-symbols-outlined text-on-surface-variant">credit_card</span>
        </div>
        <div>
            <p class="text-display-lg font-bold text-on-surface tnum">{{ $stats['total'] }}</p>
            <p class="text-label-md text-on-surface-variant">Total</p>
        </div>
    </div>
    <div class="card p-4 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center flex-shrink-0">
            <span class="material-symbols-outlined text-emerald-500">check_circle</span>
        </div>
        <div>
            <p class="text-display-lg font-bold text-emerald-600 tnum">{{ $stats['active'] }}</p>
            <p class="text-label-md text-on-surface-variant">Actives</p>
        </div>
    </div>
    <div class="card p-4 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center flex-shrink-0">
            <span class="material-symbols-outlined text-amber-500">block</span>
        </div>
        <div>
            <p class="text-display-lg font-bold text-amber-600 tnum">{{ $stats['blocked'] }}</p>
            <p class="text-label-md text-on-surface-variant">Bloquées</p>
        </div>
    </div>
    <div class="card p-4 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-purple-50 flex items-center justify-center flex-shrink-0">
            <span class="material-symbols-outlined text-purple-500">smartphone</span>
        </div>
        <div>
            <p class="text-display-lg font-bold text-purple-600 tnum">{{ $stats['virtual'] }}</p>
            <p class="text-label-md text-on-surface-variant">Virtuelles</p>
        </div>
    </div>
</div>

<!-- Filters -->
<div class="card p-4 mb-5">
    <form method="GET" action="{{ route('admin.cards') }}" class="flex flex-wrap gap-3 items-end">
        <div class="flex flex-col gap-1 flex-1 min-w-[220px]">
            <label class="text-label-md text-on-surface-variant uppercase tracking-wide">Recherche</label>
            <div class="relative">
                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-[18px]">search</span>
                <input type="text" name="search" value="{{ request('search') }}" placeholder="Nom ou email du titulaire…"
                       class="input pl-9 w-full">
            </div>
        </div>
        <div class="flex flex-col gap-1">
            <label class="text-label-md text-on-surface-variant uppercase tracking-wide">Statut</label>
            <select name="status" class="input pr-8">
                <option value="">Tous</option>
                <option value="active"  {{ request('status') === 'active'  ? 'selected' : '' }}>Active</option>
                <option value="blocked" {{ request('status') === 'blocked' ? 'selected' : '' }}>Bloquée</option>
            </select>
        </div>
        <div class="flex flex-col gap-1">
            <label class="text-label-md text-on-surface-variant uppercase tracking-wide">Type</label>
            <select name="type" class="input pr-8">
                <option value="">Tous</option>
                <option value="physical" {{ request('type') === 'physical' ? 'selected' : '' }}>Physique</option>
                <option value="virtual"  {{ request('type') === 'virtual'  ? 'selected' : '' }}>Virtuelle</option>
            </select>
        </div>
        <div class="flex items-end gap-2">
            <button type="submit" class="btn-primary px-4 py-2 text-body-md flex items-center gap-1.5 h-10">
                <span class="material-symbols-outlined text-[16px]">search</span> Rechercher
            </button>
            <a href="{{ route('admin.cards') }}" class="btn-secondary px-4 py-2 text-body-md h-10 flex items-center">Réinitialiser</a>
        </div>
    </form>
</div>

<!-- Cards table -->
<div class="card overflow-hidden">
    <div class="overflow-x-auto">
        <table class="w-full">
            <thead>
                <tr class="bg-gray-50 border-b border-gray-100">
                    <th class="px-5 py-3 text-left text-label-md text-on-surface-variant uppercase tracking-wide">Carte</th>
                    <th class="px-5 py-3 text-left text-label-md text-on-surface-variant uppercase tracking-wide">Titulaire</th>
                    <th class="px-5 py-3 text-left text-label-md text-on-surface-variant uppercase tracking-wide">Type</th>
                    <th class="px-5 py-3 text-left text-label-md text-on-surface-variant uppercase tracking-wide">Expiration</th>
                    <th class="px-5 py-3 text-right text-label-md text-on-surface-variant uppercase tracking-wide">Limite / jour</th>
                    <th class="px-5 py-3 text-left text-label-md text-on-surface-variant uppercase tracking-wide">Statut</th>
                    <th class="px-5 py-3 text-center text-label-md text-on-surface-variant uppercase tracking-wide">Actions</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
                @forelse($cards as $card)
                <tr class="hover:bg-gray-50/60 transition-colors" x-data="{ editing: false }">
                    <td class="px-5 py-4">
                        <div class="flex items-center gap-2">
                            <span class="material-symbols-outlined text-on-surface-variant text-[18px]">credit_card</span>
                            <span class="text-body-md font-medium text-on-surface tnum">{{ $card->masked_number }}</span>
                        </div>
                    </td>
                    <td class="px-5 py-4">
                        @if($card->account?->user)
                        <a href="{{ route('admin.users.show', $card->account->user->id) }}"
                           class="text-body-md text-on-surface hover:text-primary-container transition-colors font-medium">
                            {{ $card->account->user->name }}
                        </a>
                        <p class="text-label-md text-on-surface-variant">{{ $card->account->user->email }}</p>
                        @else
                        <span class="text-on-surface-variant">—</span>
                        @endif
                    </td>
                    <td class="px-5 py-4">
                        <span class="badge {{ $card->type === 'virtual' ? 'bg-purple-100 text-purple-700' : 'bg-surface-container text-on-surface-variant' }}">
                            {{ $card->type === 'virtual' ? 'Virtuelle' : 'Physique' }}
                        </span>
                    </td>
                    <td class="px-5 py-4 text-body-md text-on-surface-variant tnum">
                        {{ $card->expiry_date ? $card->expiry_date->format('m/Y') : '—' }}
                    </td>
                    <td class="px-5 py-4 text-right">
                        <div x-show="!editing">
                            <span class="text-body-md font-medium text-on-surface tnum">
                                {{ number_format($card->daily_limit, 0, ',', ' ') }} MGA
                            </span>
                            <button @click="editing = true" class="ml-2 text-on-surface-variant hover:text-primary-container transition-colors">
                                <span class="material-symbols-outlined text-[14px]">edit</span>
                            </button>
                        </div>
                        <form x-show="!editing ? false : true" x-cloak method="POST"
                              action="{{ route('admin.cards.limit', $card->id) }}"
                              class="flex items-center justify-end gap-1.5">
                            @csrf @method('PATCH')
                            <input type="number" name="daily_limit" value="{{ $card->daily_limit }}"
                                   min="0" step="1000" class="input w-32 text-right tnum text-sm py-1 h-8">
                            <button type="submit" class="btn-primary px-2 h-8 text-xs">OK</button>
                            <button type="button" @click="editing = false" class="btn-secondary px-2 h-8 text-xs">✕</button>
                        </form>
                    </td>
                    <td class="px-5 py-4">
                        <span class="badge {{ $card->is_blocked ? 'badge-frozen' : 'badge-active' }}">
                            {{ $card->is_blocked ? 'Bloquée' : 'Active' }}
                        </span>
                    </td>
                    <td class="px-5 py-4 text-center">
                        <form method="POST" action="{{ route('admin.cards.toggle', $card->id) }}" class="inline">
                            @csrf @method('PATCH')
                            <button class="text-body-md font-medium px-3 py-1.5 rounded-lg border transition-all
                                           {{ $card->is_blocked
                                               ? 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border-emerald-200'
                                               : 'bg-amber-50 text-amber-700 hover:bg-amber-100 border-amber-200' }}">
                                {{ $card->is_blocked ? 'Débloquer' : 'Bloquer' }}
                            </button>
                        </form>
                    </td>
                </tr>
                @empty
                <tr>
                    <td colspan="7" class="px-5 py-12 text-center">
                        <span class="material-symbols-outlined text-4xl text-gray-300 block mb-2">credit_card_off</span>
                        <p class="text-body-md text-on-surface-variant">Aucune carte trouvée</p>
                    </td>
                </tr>
                @endforelse
            </tbody>
        </table>
    </div>
    @if($cards->hasPages())
    <div class="px-5 py-4 border-t border-gray-100 flex items-center justify-between bg-gray-50/50">
        <p class="text-body-md text-on-surface-variant">
            {{ $cards->firstItem() }}–{{ $cards->lastItem() }} sur
            <strong class="text-on-surface tnum">{{ number_format($cards->total()) }}</strong>
        </p>
        {{ $cards->appends(request()->query())->links() }}
    </div>
    @endif
</div>

@endsection
