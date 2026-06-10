@extends('admin.layout')
@section('title', 'KYC — ' . $user->name)
@section('page-title', 'Vérification KYC — ' . $user->name)

@section('content')
<div class="w-full space-y-5">

    <!-- Header card -->
    <div class="card p-5">
        <div class="flex items-start justify-between gap-4 flex-wrap">
            <div class="flex items-center gap-4">
                @if($user->avatar_url)
                    <img src="{{ $user->avatar_url }}" class="w-14 h-14 rounded-full object-cover flex-shrink-0" alt="">
                @else
                    <div class="w-14 h-14 rounded-full bg-surface-container-high flex items-center justify-center text-base font-bold text-on-surface flex-shrink-0">
                        {{ strtoupper(substr($user->name, 0, 2)) }}
                    </div>
                @endif
                <div>
                    <p class="font-semibold text-on-surface text-headline-sm">{{ $user->name }}</p>
                    <p class="text-label-md text-on-surface-variant">{{ $user->email }} · {{ $user->phone ?? 'Pas de téléphone' }}</p>
                    <p class="text-label-md text-on-surface-variant mt-0.5">Inscrit le {{ $user->created_at->format('d/m/Y') }}</p>
                </div>
            </div>
            <div class="flex items-center gap-3 flex-wrap">
                <div class="text-center px-4 py-2 bg-gray-50 rounded-lg border border-gray-100">
                    <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Statut KYC</p>
                    @if($user->kyc_status === 'pending')
                        <span class="badge bg-amber-100 text-amber-700 mt-1 inline-flex">En attente</span>
                    @elseif($user->kyc_status === 'approved')
                        <span class="badge bg-emerald-100 text-emerald-700 mt-1 inline-flex">Approuvé</span>
                    @elseif($user->kyc_status === 'rejected')
                        <span class="badge bg-red-100 text-red-600 mt-1 inline-flex">Refusé</span>
                    @endif
                </div>
                <div class="text-center px-4 py-2 bg-gray-50 rounded-lg border border-gray-100">
                    <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Soumis le</p>
                    <p class="text-body-md font-medium text-on-surface mt-1">{{ $user->kyc_submitted_at?->format('d/m/Y H:i') ?? '—' }}</p>
                </div>
                @if($user->kyc_reviewed_at)
                <div class="text-center px-4 py-2 bg-gray-50 rounded-lg border border-gray-100">
                    <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Vérifié le</p>
                    <p class="text-body-md font-medium text-on-surface mt-1">{{ $user->kyc_reviewed_at->format('d/m/Y H:i') }}</p>
                </div>
                @endif
                <a href="{{ route('admin.kyc') }}"
                   class="btn-secondary px-4 py-2 text-body-md flex items-center gap-1.5 h-10">
                    <span class="material-symbols-outlined text-[16px]">arrow_back</span> Retour
                </a>
            </div>
        </div>

        @if($user->kyc_rejection_reason)
        <div class="mt-4 pt-4 border-t border-gray-100">
            <p class="text-label-md text-on-surface-variant uppercase tracking-wide mb-1">Motif de refus</p>
            <p class="text-body-md text-red-600 font-medium bg-red-50 px-4 py-2.5 rounded-lg border border-red-100">
                {{ $user->kyc_rejection_reason }}
            </p>
        </div>
        @endif
    </div>

    <!-- CIN Name -->
    <div class="card p-5">
        <h2 class="text-headline-sm font-semibold text-on-surface mb-4 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary-container">badge</span>
            Informations déclarées
        </h2>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
                <p class="text-label-md text-on-surface-variant uppercase tracking-wide mb-1">Nom complet (CIN)</p>
                <p class="text-body-lg font-semibold text-on-surface">{{ $user->cin_full_name ?? '—' }}</p>
            </div>
            <div>
                <p class="text-label-md text-on-surface-variant uppercase tracking-wide mb-1">Nom du compte</p>
                <p class="text-body-lg font-semibold text-on-surface">{{ $user->name }}</p>
            </div>
        </div>
    </div>

    <!-- CIN Documents -->
    <div class="card p-5">
        <h2 class="text-headline-sm font-semibold text-on-surface mb-4 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary-container">photo_id</span>
            Documents CIN
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Recto -->
            <div>
                <p class="text-label-md text-on-surface-variant uppercase tracking-wide mb-3">CIN Recto</p>
                @if($user->cin_recto_url)
                    <a href="{{ $user->cin_recto_url }}" target="_blank" class="block group relative rounded-xl overflow-hidden border border-gray-200 hover:border-primary-container transition-all">
                        <img src="{{ $user->cin_recto_url }}" alt="CIN Recto"
                             class="w-full object-cover max-h-64 bg-gray-50">
                        <div class="absolute inset-0 bg-black/0 group-hover:bg-black/10 transition-all flex items-center justify-center">
                            <span class="material-symbols-outlined text-white opacity-0 group-hover:opacity-100 text-[32px] transition-all">open_in_new</span>
                        </div>
                    </a>
                    <p class="text-label-md text-on-surface-variant mt-2 flex items-center gap-1">
                        <span class="material-symbols-outlined text-[14px]">zoom_in</span>
                        Cliquez pour agrandir
                    </p>
                @else
                    <div class="w-full h-40 border-2 border-dashed border-gray-200 rounded-xl flex items-center justify-center">
                        <p class="text-on-surface-variant text-body-md">Aucun document</p>
                    </div>
                @endif
            </div>

            <!-- Verso -->
            <div>
                <p class="text-label-md text-on-surface-variant uppercase tracking-wide mb-3">CIN Verso</p>
                @if($user->cin_verso_url)
                    <a href="{{ $user->cin_verso_url }}" target="_blank" class="block group relative rounded-xl overflow-hidden border border-gray-200 hover:border-primary-container transition-all">
                        <img src="{{ $user->cin_verso_url }}" alt="CIN Verso"
                             class="w-full object-cover max-h-64 bg-gray-50">
                        <div class="absolute inset-0 bg-black/0 group-hover:bg-black/10 transition-all flex items-center justify-center">
                            <span class="material-symbols-outlined text-white opacity-0 group-hover:opacity-100 text-[32px] transition-all">open_in_new</span>
                        </div>
                    </a>
                    <p class="text-label-md text-on-surface-variant mt-2 flex items-center gap-1">
                        <span class="material-symbols-outlined text-[14px]">zoom_in</span>
                        Cliquez pour agrandir
                    </p>
                @else
                    <div class="w-full h-40 border-2 border-dashed border-gray-200 rounded-xl flex items-center justify-center">
                        <p class="text-on-surface-variant text-body-md">Aucun document</p>
                    </div>
                @endif
            </div>
        </div>
    </div>

    <!-- Actions -->
    @if(in_array($user->kyc_status, ['pending', 'rejected', 'approved']))
    <div class="card p-5" x-data="{ showRejectForm: false, modal: false }">
        <h2 class="text-headline-sm font-semibold text-on-surface mb-4 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary-container">gavel</span>
            Décision
        </h2>

        {{-- Hidden approve form --}}
        @if($user->kyc_status !== 'approved')
        <form id="form-kyc-approve" action="{{ route('admin.kyc.approve', $user) }}" method="POST" class="hidden">
            @csrf
        </form>
        @endif

        <div class="flex flex-wrap gap-3">
            @if($user->kyc_status !== 'approved')
            <button type="button" @click="modal = true"
                    class="btn-primary px-5 py-2.5 text-body-md flex items-center gap-2">
                <span class="material-symbols-outlined text-[18px]">verified_user</span>
                Approuver
            </button>
            @endif

            @if($user->kyc_status !== 'rejected')
            <button @click="showRejectForm = !showRejectForm"
                    class="border border-red-200 bg-red-50 text-red-600 hover:bg-red-100 font-medium rounded-lg px-5 py-2.5 text-body-md flex items-center gap-2 transition-all">
                <span class="material-symbols-outlined text-[18px]">gpp_bad</span>
                Refuser
            </button>
            @endif
        </div>

        <!-- Reject form -->
        <div x-show="showRejectForm" x-cloak x-transition class="mt-4 pt-4 border-t border-gray-100">
            <form action="{{ route('admin.kyc.reject', $user) }}" method="POST" class="space-y-3">
                @csrf
                <div>
                    <label class="text-label-md text-on-surface-variant uppercase tracking-wide block mb-1">
                        Motif du refus <span class="text-red-500">*</span>
                    </label>
                    <textarea name="reason" rows="3" required
                              placeholder="Ex : Photo floue, document expiré, nom ne correspond pas..."
                              class="input w-full h-auto py-2.5 resize-none"></textarea>
                </div>
                <button type="submit"
                        class="border border-red-300 bg-red-600 hover:bg-red-700 text-white font-medium rounded-lg px-5 py-2.5 text-body-md flex items-center gap-2 transition-all">
                    <span class="material-symbols-outlined text-[18px]">send</span>
                    Confirmer le refus
                </button>
            </form>
        </div>

        {{-- Approve confirmation modal --}}
        <div x-show="modal" x-cloak
             class="fixed inset-0 z-50 flex items-center justify-center p-4"
             @keydown.escape.window="modal = false"
             x-transition:enter="transition ease-out duration-200"
             x-transition:enter-start="opacity-0"
             x-transition:enter-end="opacity-100"
             x-transition:leave="transition ease-in duration-150"
             x-transition:leave-start="opacity-100"
             x-transition:leave-end="opacity-0">
            <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="modal = false"></div>
            <div class="relative bg-white rounded-2xl shadow-xl w-full max-w-sm p-6 flex flex-col gap-4 z-10"
                 x-transition:enter="transition ease-out duration-200"
                 x-transition:enter-start="opacity-0 scale-95"
                 x-transition:enter-end="opacity-100 scale-100">
                <div class="w-14 h-14 rounded-full bg-emerald-50 flex items-center justify-center mx-auto">
                    <span class="material-symbols-outlined text-[28px] text-emerald-600">verified_user</span>
                </div>
                <div class="text-center space-y-1.5">
                    <h3 class="text-headline-sm text-on-surface font-semibold">Approuver la vérification</h3>
                    <p class="text-body-md text-on-surface-variant">
                        Le KYC de <strong>{{ $user->name }}</strong> sera marqué comme approuvé. Cette action accordera l'accès complet à l'application.
                    </p>
                </div>
                <div class="flex gap-3 mt-1">
                    <button type="button" @click="modal = false"
                            class="btn-secondary flex-1 py-2.5 text-body-md flex items-center justify-center">
                        Annuler
                    </button>
                    <button type="button"
                            @click="document.getElementById('form-kyc-approve').submit()"
                            class="flex-1 py-2.5 text-body-md font-semibold rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white transition-all flex items-center justify-center gap-2">
                        <span class="material-symbols-outlined text-[16px]">verified_user</span>
                        Approuver
                    </button>
                </div>
            </div>
        </div>
    </div>
    @endif

</div>
@endsection
