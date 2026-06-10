@extends('admin.layout')
@section('title', 'Vérification d\'Identité')
@section('page-title', 'Vérification d\'Identité (KYC)')

@section('content')

<!-- Stats -->
<div class="grid grid-cols-3 gap-4 mb-5">
    <div class="card p-4 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center flex-shrink-0">
            <span class="material-symbols-outlined text-amber-500">hourglass_empty</span>
        </div>
        <div>
            <p class="text-display-lg font-bold text-amber-600 tnum">{{ $stats['pending'] }}</p>
            <p class="text-label-md text-on-surface-variant">En attente</p>
        </div>
    </div>
    <div class="card p-4 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center flex-shrink-0">
            <span class="material-symbols-outlined text-emerald-500">verified_user</span>
        </div>
        <div>
            <p class="text-display-lg font-bold text-emerald-600 tnum">{{ $stats['approved'] }}</p>
            <p class="text-label-md text-on-surface-variant">Approuvés</p>
        </div>
    </div>
    <div class="card p-4 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-red-50 flex items-center justify-center flex-shrink-0">
            <span class="material-symbols-outlined text-red-500">gpp_bad</span>
        </div>
        <div>
            <p class="text-display-lg font-bold text-red-600 tnum">{{ $stats['rejected'] }}</p>
            <p class="text-label-md text-on-surface-variant">Refusés</p>
        </div>
    </div>
</div>

<!-- Filter -->
<div class="card p-4 mb-5">
    <form method="GET" action="{{ route('admin.kyc') }}" class="flex flex-wrap gap-3 items-end">
        <div class="flex flex-col gap-1">
            <label class="text-label-md text-on-surface-variant uppercase tracking-wide">Statut</label>
            <select name="status" class="input pr-8">
                <option value="">Tous</option>
                <option value="pending"  {{ request('status') === 'pending'  ? 'selected' : '' }}>En attente</option>
                <option value="approved" {{ request('status') === 'approved' ? 'selected' : '' }}>Approuvés</option>
                <option value="rejected" {{ request('status') === 'rejected' ? 'selected' : '' }}>Refusés</option>
            </select>
        </div>
        <button type="submit" class="btn-primary px-4 h-10 text-body-md flex items-center gap-1.5">
            <span class="material-symbols-outlined text-[16px]">filter_list</span> Filtrer
        </button>
        @if(request('status'))
        <a href="{{ route('admin.kyc') }}" class="btn-secondary px-4 h-10 text-body-md flex items-center gap-1.5">
            <span class="material-symbols-outlined text-[16px]">close</span> Réinitialiser
        </a>
        @endif
    </form>
</div>

<!-- Table -->
<div class="card overflow-hidden">
    <table class="w-full text-body-md">
        <thead>
            <tr class="border-b border-gray-100 bg-surface-container-lowest">
                <th class="text-left px-5 py-3 text-label-md text-on-surface-variant uppercase tracking-wide font-medium">Utilisateur</th>
                <th class="text-left px-5 py-3 text-label-md text-on-surface-variant uppercase tracking-wide font-medium">Nom CIN</th>
                <th class="text-left px-5 py-3 text-label-md text-on-surface-variant uppercase tracking-wide font-medium">Soumis le</th>
                <th class="text-left px-5 py-3 text-label-md text-on-surface-variant uppercase tracking-wide font-medium">Statut</th>
                <th class="text-right px-5 py-3 text-label-md text-on-surface-variant uppercase tracking-wide font-medium">Action</th>
            </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
            @forelse($users as $user)
            <tr class="hover:bg-surface-container-lowest transition-colors">
                <td class="px-5 py-3.5">
                    <div class="flex items-center gap-3">
                        @if($user->avatar_url)
                            <img src="{{ $user->avatar_url }}" class="w-9 h-9 rounded-full object-cover flex-shrink-0" alt="">
                        @else
                            <div class="w-9 h-9 rounded-full bg-surface-container-high flex items-center justify-center text-xs font-bold text-on-surface flex-shrink-0">
                                {{ strtoupper(substr($user->name, 0, 2)) }}
                            </div>
                        @endif
                        <div>
                            <p class="font-medium text-on-surface">{{ $user->name }}</p>
                            <p class="text-label-md text-on-surface-variant">{{ $user->email }}</p>
                        </div>
                    </div>
                </td>
                <td class="px-5 py-3.5 text-on-surface font-medium">
                    {{ $user->cin_full_name ?? '—' }}
                </td>
                <td class="px-5 py-3.5 text-on-surface-variant tnum">
                    {{ $user->kyc_submitted_at?->format('d/m/Y H:i') ?? '—' }}
                </td>
                <td class="px-5 py-3.5">
                    @if($user->kyc_status === 'pending')
                        <span class="badge bg-amber-100 text-amber-700">En attente</span>
                    @elseif($user->kyc_status === 'approved')
                        <span class="badge bg-emerald-100 text-emerald-700">Approuvé</span>
                    @elseif($user->kyc_status === 'rejected')
                        <span class="badge bg-red-100 text-red-600">Refusé</span>
                    @endif
                </td>
                <td class="px-5 py-3.5 text-right">
                    <a href="{{ route('admin.kyc.show', $user) }}"
                       class="btn-secondary px-3 py-1.5 text-body-md inline-flex items-center gap-1.5">
                        <span class="material-symbols-outlined text-[16px]">open_in_new</span> Vérifier
                    </a>
                </td>
            </tr>
            @empty
            <tr>
                <td colspan="5" class="px-5 py-12 text-center text-on-surface-variant">
                    <span class="material-symbols-outlined text-[40px] block mx-auto mb-2 text-outline">verified_user</span>
                    Aucune vérification d'identité à traiter.
                </td>
            </tr>
            @endforelse
        </tbody>
    </table>
    @if($users->hasPages())
    <div class="px-5 py-4 border-t border-gray-100">
        {{ $users->links() }}
    </div>
    @endif
</div>

@endsection
