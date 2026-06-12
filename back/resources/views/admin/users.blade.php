@extends('admin.layout')
@section('title', 'Utilisateurs')
@section('page-title', 'Gestion des Utilisateurs')

@section('content')

<!-- Intro -->
<div class="mb-6">
    <p class="text-body-md text-on-surface-variant">Gérez vos clients, surveillez les comptes et configurez les accès.</p>
</div>

<!-- Stat cards bento grid -->
<div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
    <div class="bg-white p-5 rounded-xl border border-gray-100 shadow-card flex flex-col gap-2 hover:shadow-md transition-all">
        <div class="flex justify-between items-start">
            <div class="w-10 h-10 bg-red-50 rounded-lg flex items-center justify-center flex-shrink-0">
                <span class="material-symbols-outlined text-primary">group</span>
            </div>
            <span class="text-emerald-600 text-label-md flex items-center gap-0.5">+12% <span class="material-symbols-outlined text-[14px]">trending_up</span></span>
        </div>
        <p class="text-label-md text-on-surface-variant mt-1">Total Utilisateurs</p>
        <p class="text-display-lg text-on-surface tnum">{{ $stats['total'] }}</p>
    </div>
    <div class="bg-white p-5 rounded-xl border border-gray-100 shadow-card flex flex-col gap-2 hover:shadow-md transition-all">
        <div class="flex justify-between items-start">
            <div class="w-10 h-10 bg-emerald-50 rounded-lg flex items-center justify-center flex-shrink-0">
                <span class="material-symbols-outlined text-emerald-600">verified_user</span>
            </div>
            <span class="text-emerald-600 text-label-md">Actifs</span>
        </div>
        <p class="text-label-md text-on-surface-variant mt-1">Utilisateurs Actifs</p>
        <p class="text-display-lg text-on-surface tnum">{{ $stats['active'] }}</p>
    </div>
    <div class="bg-white p-5 rounded-xl border border-gray-100 shadow-card flex flex-col gap-2 hover:shadow-md transition-all">
        <div class="flex justify-between items-start">
            <div class="w-10 h-10 bg-blue-50 rounded-lg flex items-center justify-center flex-shrink-0">
                <span class="material-symbols-outlined text-blue-600">ac_unit</span>
            </div>
            <span class="text-blue-400 text-label-md">Temporaire</span>
        </div>
        <p class="text-label-md text-on-surface-variant mt-1">Comptes Gelés</p>
        <p class="text-display-lg text-on-surface tnum">{{ $stats['frozen'] }}</p>
    </div>
    <div class="bg-white p-5 rounded-xl border border-gray-100 shadow-card flex flex-col gap-2 hover:shadow-md transition-all">
        <div class="flex justify-between items-start">
            <div class="w-10 h-10 bg-orange-50 rounded-lg flex items-center justify-center flex-shrink-0">
                <span class="material-symbols-outlined text-orange-600">admin_panel_settings</span>
            </div>
            <span class="text-orange-400 text-label-md">Security</span>
        </div>
        <p class="text-label-md text-on-surface-variant mt-1">Administrateurs</p>
        <p class="text-display-lg text-on-surface tnum">{{ $stats['admin'] }}</p>
    </div>
</div>

<!-- Search + filters + view toggle -->
<div x-data="{ view: localStorage.getItem('usersView') || 'grid' }"
     x-init="$watch('view', v => localStorage.setItem('usersView', v))">

<div class="bg-white p-5 rounded-xl border border-gray-100 shadow-card mb-6">
    <form method="GET" action="{{ route('admin.users') }}" class="flex flex-wrap gap-4 items-end">
        <div class="flex-1 min-w-[220px] space-y-1.5">
            <label class="text-label-md text-on-surface-variant uppercase tracking-wide">Recherche rapide</label>
            <div class="relative">
                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[18px]">search</span>
                <input type="text" name="search" value="{{ request('search') }}"
                       placeholder="Nom, email ou téléphone…" class="input pl-10 w-full">
            </div>
        </div>
        <div class="space-y-1.5">
            <label class="text-label-md text-on-surface-variant uppercase tracking-wide">Statut</label>
            <select name="status" class="input pr-8">
                <option value="">Tous les statuts</option>
                <option value="active"  {{ request('status') === 'active'  ? 'selected' : '' }}>Actif</option>
                <option value="frozen"  {{ request('status') === 'frozen'  ? 'selected' : '' }}>Gelé</option>
            </select>
        </div>
        <div class="flex items-end gap-2">
            <button type="submit" class="btn-primary px-5 py-2.5 text-body-md flex items-center gap-1.5 h-10">
                <span class="material-symbols-outlined text-[16px]">filter_list</span> Filtrer
            </button>
            <a href="{{ route('admin.users') }}" class="btn-secondary px-5 py-2.5 text-body-md h-10 flex items-center">
                Réinitialiser
            </a>
        </div>
        <!-- View toggle -->
        <div class="flex items-end gap-1 ml-auto">
            <button type="button" @click="view = 'grid'"
                    :class="view === 'grid' ? 'bg-primary-container text-white' : 'btn-secondary text-on-surface-variant'"
                    class="w-10 h-10 flex items-center justify-center rounded-lg transition-all">
                <span class="material-symbols-outlined text-[20px]">grid_view</span>
            </button>
            <button type="button" @click="view = 'list'"
                    :class="view === 'list' ? 'bg-primary-container text-white' : 'btn-secondary text-on-surface-variant'"
                    class="w-10 h-10 flex items-center justify-center rounded-lg transition-all">
                <span class="material-symbols-outlined text-[20px]">view_list</span>
            </button>
        </div>
    </form>
</div>

<!-- ── GRID VIEW ── -->
<div x-show="view === 'grid'" x-cloak
     class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-5">
    @forelse($users as $user)
    <div class="bg-white p-6 rounded-xl border border-gray-100 shadow-card hover:shadow-lg hover:-translate-y-1 transition-all duration-200 flex flex-col gap-5">

        <!-- Header: avatar + name + badge -->
        <div class="flex justify-between items-start">
            <div class="flex items-center gap-4">
                @if($user->avatar_url)
                <img src="{{ $user->avatar_url }}" class="w-14 h-14 rounded-full object-cover flex-shrink-0" alt="{{ $user->name }}">
                @else
                <div class="w-14 h-14 bg-red-50 rounded-full flex items-center justify-center text-primary text-xl font-bold flex-shrink-0">
                    {{ strtoupper(substr($user->name, 0, 2)) }}
                </div>
                @endif
                <div>
                    <h4 class="text-headline-sm text-on-surface">{{ $user->name }}</h4>
                    <div class="flex items-center gap-1.5 mt-0.5">
                        <span class="badge {{ $user->is_active ? 'badge-active' : 'badge-frozen' }}">
                            {{ $user->is_active ? 'Actif' : 'Gelé' }}
                        </span>
                        @if($user->role === 'admin')
                        <span class="badge bg-blue-100 text-blue-700">Admin</span>
                        @endif
                    </div>
                </div>
            </div>
            @if($user->role !== 'admin')
            <form method="POST" action="{{ route('admin.users.toggle', $user->id) }}">
                @csrf @method('PATCH')
                <button class="text-on-surface-variant hover:text-primary transition-colors p-1 rounded-lg hover:bg-gray-50"
                        title="{{ $user->is_active ? 'Geler' : 'Activer' }}">
                    <span class="material-symbols-outlined">{{ $user->is_active ? 'person_off' : 'person_check' }}</span>
                </button>
            </form>
            @endif
        </div>

        <!-- Contact info -->
        <div class="space-y-2">
            <div class="flex items-center gap-2.5 text-on-surface-variant">
                <span class="material-symbols-outlined text-[18px]">mail</span>
                <span class="text-body-md truncate">{{ $user->email }}</span>
            </div>
            <div class="flex items-center gap-2.5 text-on-surface-variant">
                <span class="material-symbols-outlined text-[18px]">phone</span>
                <span class="text-body-md">{{ $user->phone ?? '—' }}</span>
            </div>
        </div>

        <!-- Balance box -->
        <div class="bg-gray-50 p-4 rounded-lg flex justify-between items-center">
            <div>
                <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Solde Total</p>
                <p class="text-body-md font-semibold text-on-surface tnum mt-0.5">
                    {{ number_format($user->accounts->sum('balance'), 0, ',', ' ') }} MGA
                </p>
            </div>
            <div class="text-right">
                <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Comptes</p>
                <p class="text-body-md font-semibold text-on-surface mt-0.5">{{ $user->accounts_count }}</p>
            </div>
        </div>

        <!-- Footer row -->
        <div class="flex justify-between items-center">
            <p class="text-label-md text-on-surface-variant">
                Inscrit le: <span class="font-semibold text-on-surface">{{ $user->created_at->format('d/m/Y') }}</span>
            </p>
            <a href="{{ route('admin.users.show', $user->id) }}"
               class="text-primary text-label-md font-semibold flex items-center gap-0.5 hover:underline">
                Voir le profil
                <span class="material-symbols-outlined text-[16px]">chevron_right</span>
            </a>
        </div>
    </div>
    @empty
    <div class="col-span-3 py-16 text-center">
        <span class="material-symbols-outlined text-5xl text-gray-300 block mb-3">group</span>
        <p class="text-body-md text-on-surface-variant">Aucun utilisateur trouvé</p>
    </div>
    @endforelse

    <!-- Add user placeholder -->
    <div class="border-2 border-dashed border-gray-200 rounded-xl flex flex-col items-center justify-center p-8 opacity-60 hover:opacity-100 transition-opacity cursor-pointer">
        <div class="w-12 h-12 rounded-full border-2 border-primary flex items-center justify-center mb-3">
            <span class="material-symbols-outlined text-primary">person_add</span>
        </div>
        <p class="text-label-md font-semibold text-on-surface-variant">Ajouter un nouvel utilisateur</p>
    </div>
</div>

<!-- ── LIST VIEW ── -->
<div x-show="view === 'list'" x-cloak class="bg-white rounded-xl border border-gray-100 shadow-card overflow-hidden">
    <div class="overflow-x-auto">
        <table class="w-full text-left">
            <thead class="border-b border-gray-100 bg-gray-50/60">
                <tr>
                    <th class="px-5 py-3 text-label-md text-on-surface-variant uppercase tracking-wide">Utilisateur</th>
                    <th class="px-5 py-3 text-label-md text-on-surface-variant uppercase tracking-wide">Téléphone</th>
                    <th class="px-5 py-3 text-label-md text-on-surface-variant uppercase tracking-wide">Rôle</th>
                    <th class="px-5 py-3 text-label-md text-on-surface-variant uppercase tracking-wide">Statut</th>
                    <th class="px-5 py-3 text-right text-label-md text-on-surface-variant uppercase tracking-wide">Solde</th>
                    <th class="px-5 py-3 text-label-md text-on-surface-variant uppercase tracking-wide">Inscrit</th>
                    <th class="px-5 py-3 text-center text-label-md text-on-surface-variant uppercase tracking-wide">Actions</th>
                </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
                @forelse($users as $user)
                <tr class="hover:bg-gray-50/60 transition-colors">
                    <td class="px-5 py-3.5">
                        <div class="flex items-center gap-3">
                            @if($user->avatar_url)
                            <img src="{{ $user->avatar_url }}" class="w-9 h-9 rounded-full object-cover flex-shrink-0" alt="{{ $user->name }}">
                            @else
                            <div class="w-9 h-9 rounded-full bg-red-50 flex items-center justify-center text-xs font-bold text-primary flex-shrink-0">
                                {{ strtoupper(substr($user->name, 0, 2)) }}
                            </div>
                            @endif
                            <div>
                                <p class="text-body-md font-semibold text-on-surface">{{ $user->name }}</p>
                                <p class="text-label-md text-on-surface-variant">{{ $user->email }}</p>
                            </div>
                        </div>
                    </td>
                    <td class="px-5 py-3.5 text-body-md text-on-surface-variant">{{ $user->phone ?? '—' }}</td>
                    <td class="px-5 py-3.5">
                        <span class="badge {{ $user->role === 'admin' ? 'bg-blue-100 text-blue-700' : 'bg-surface-container text-on-surface-variant' }}">
                            {{ $user->role === 'admin' ? 'Admin' : 'Utilisateur' }}
                        </span>
                    </td>
                    <td class="px-5 py-3.5">
                        <span class="badge {{ $user->is_active ? 'badge-active' : 'badge-frozen' }}">
                            {{ $user->is_active ? 'Actif' : 'Gelé' }}
                        </span>
                    </td>
                    <td class="px-5 py-3.5 text-right tnum font-medium text-body-md text-on-surface">
                        {{ number_format($user->accounts->sum('balance'), 0, ',', ' ') }} MGA
                    </td>
                    <td class="px-5 py-3.5 text-label-md text-on-surface-variant">{{ $user->created_at->format('d/m/Y') }}</td>
                    <td class="px-5 py-3.5 text-center">
                        <div class="flex items-center justify-center gap-2">
                            <a href="{{ route('admin.users.show', $user->id) }}"
                               class="btn-secondary px-3 py-1.5 text-body-md h-8 flex items-center gap-1">
                                <span class="material-symbols-outlined text-[14px]">visibility</span> Voir
                            </a>
                            @if($user->role !== 'admin')
                            <form method="POST" action="{{ route('admin.users.toggle', $user->id) }}" class="inline">
                                @csrf @method('PATCH')
                                <button class="text-body-md font-medium px-3 py-1.5 rounded-lg h-8 border transition-all
                                               {{ $user->is_active
                                                   ? 'bg-amber-50 text-amber-700 hover:bg-amber-100 border-amber-200'
                                                   : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border-emerald-200' }}">
                                    {{ $user->is_active ? 'Geler' : 'Activer' }}
                                </button>
                            </form>
                            @endif
                        </div>
                    </td>
                </tr>
                @empty
                <tr>
                    <td colspan="7" class="px-5 py-12 text-center">
                        <span class="material-symbols-outlined text-4xl text-gray-300 block mb-2">group</span>
                        <p class="text-body-md text-on-surface-variant">Aucun utilisateur trouvé</p>
                    </td>
                </tr>
                @endforelse
            </tbody>
        </table>
    </div>
    @if($users->hasPages())
    <div class="px-5 py-4 border-t border-gray-100 flex items-center justify-between bg-gray-50/50">
        <p class="text-body-md text-on-surface-variant">
            {{ $users->firstItem() }}–{{ $users->lastItem() }} sur
            <strong class="text-on-surface tnum">{{ number_format($users->total()) }}</strong>
        </p>
        {{ $users->appends(request()->query())->links() }}
    </div>
    @endif
</div>

<!-- Grid pagination -->
<div x-show="view === 'grid'" x-cloak>
    @if($users->hasPages())
    <div class="mt-5 flex items-center justify-between">
        <p class="text-body-md text-on-surface-variant">
            {{ $users->firstItem() }}–{{ $users->lastItem() }} sur
            <strong class="text-on-surface tnum">{{ number_format($users->total()) }}</strong>
        </p>
        {{ $users->appends(request()->query())->links() }}
    </div>
    @endif
</div>

</div>{{-- end x-data --}}

@endsection
