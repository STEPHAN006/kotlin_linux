@extends('admin.layout')
@section('title', 'Export')
@section('page-title', 'Export des Données')

@section('content')
<div class="w-full max-w-4xl space-y-5">

    <!-- Export CSV Transactions -->
    <div class="card p-6">
        <div class="flex items-start gap-4">
            <div class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center flex-shrink-0 border border-emerald-100">
                <span class="material-symbols-outlined text-emerald-600">table_chart</span>
            </div>
            <div class="flex-1">
                <h3 class="font-semibold text-on-surface text-headline-sm">Transactions — CSV</h3>
                <p class="text-body-md text-on-surface-variant mt-1 mb-4">
                    Exportez toutes les transactions dans un fichier CSV compatible Excel pour la comptabilité et les audits.
                </p>
                <form action="{{ route('admin.export.download') }}" method="GET" class="flex flex-wrap gap-3 items-end">
                    <input type="hidden" name="format" value="csv">
                    <div class="flex flex-col gap-1">
                        <label class="text-label-md text-on-surface-variant uppercase tracking-wide">Du</label>
                        <input type="date" name="date_from" value="{{ now()->startOfMonth()->format('Y-m-d') }}" class="input">
                    </div>
                    <div class="flex flex-col gap-1">
                        <label class="text-label-md text-on-surface-variant uppercase tracking-wide">Au</label>
                        <input type="date" name="date_to" value="{{ now()->format('Y-m-d') }}" class="input">
                    </div>
                    <button type="submit"
                            class="h-10 px-4 bg-emerald-600 hover:bg-emerald-700 text-white font-medium rounded-lg text-body-md
                                   flex items-center gap-1.5 transition-all active:scale-[0.98]">
                        <span class="material-symbols-outlined text-[16px]">download</span>
                        Télécharger CSV
                    </button>
                </form>
            </div>
        </div>
    </div>

    <!-- Export SWIFT MT940 -->
    <div class="card p-6">
        <div class="flex items-start gap-4">
            <div class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center flex-shrink-0 border border-blue-100">
                <span class="material-symbols-outlined text-blue-600">account_balance</span>
            </div>
            <div class="flex-1">
                <h3 class="font-semibold text-on-surface text-headline-sm">Relevés — SWIFT MT940</h3>
                <p class="text-body-md text-on-surface-variant mt-1 mb-4">
                    Exportez les relevés bancaires au format SWIFT MT940 pour l'interopérabilité interbancaire.
                </p>
                <form action="{{ route('admin.export.download') }}" method="GET" class="flex flex-wrap gap-3 items-end">
                    <input type="hidden" name="format" value="swift">
                    <div class="flex flex-col gap-1">
                        <label class="text-label-md text-on-surface-variant uppercase tracking-wide">Du</label>
                        <input type="date" name="date_from" value="{{ now()->startOfMonth()->format('Y-m-d') }}" class="input">
                    </div>
                    <div class="flex flex-col gap-1">
                        <label class="text-label-md text-on-surface-variant uppercase tracking-wide">Au</label>
                        <input type="date" name="date_to" value="{{ now()->format('Y-m-d') }}" class="input">
                    </div>
                    <button type="submit"
                            class="h-10 px-4 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg text-body-md
                                   flex items-center gap-1.5 transition-all active:scale-[0.98]">
                        <span class="material-symbols-outlined text-[16px]">download</span>
                        Télécharger SWIFT
                    </button>
                </form>
            </div>
        </div>
    </div>

    <!-- Export Users CSV -->
    <div class="card p-6">
        <div class="flex items-start gap-4">
            <div class="w-12 h-12 bg-violet-50 rounded-xl flex items-center justify-center flex-shrink-0 border border-violet-100">
                <span class="material-symbols-outlined text-violet-600">people</span>
            </div>
            <div class="flex-1">
                <h3 class="font-semibold text-on-surface text-headline-sm">Utilisateurs — CSV</h3>
                <p class="text-body-md text-on-surface-variant mt-1 mb-4">
                    Exportez la liste complète des utilisateurs avec leurs informations, statuts et soldes de compte.
                </p>
                <a href="{{ route('admin.export.download') }}?format=users"
                   class="inline-flex items-center gap-1.5 h-10 px-4 bg-violet-600 hover:bg-violet-700 text-white font-medium rounded-lg
                          text-body-md transition-all active:scale-[0.98]">
                    <span class="material-symbols-outlined text-[16px]">download</span>
                    Télécharger CSV Utilisateurs
                </a>
            </div>
        </div>
    </div>

    <!-- Info note -->
    <div class="flex items-start gap-3 px-4 py-3 bg-surface-container-low rounded-lg border border-outline-variant">
        <span class="material-symbols-outlined text-on-surface-variant text-[18px] flex-shrink-0 mt-0.5">info</span>
        <p class="text-body-md text-on-surface-variant">
            Les exports sont générés en temps réel depuis la base de données. Les fichiers CSV sont encodés en UTF-8 avec BOM pour une compatibilité optimale avec Microsoft Excel.
        </p>
    </div>
</div>
@endsection
