@extends('admin.layout')

@section('title', 'Alertes Fraude')
@section('page-title', 'Détection d\'anomalies')

@section('content')
<div class="space-y-6">

    {{-- Status bar --}}
    <div class="card p-5 flex items-center gap-4">
        <div class="relative flex h-3 w-3">
            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-tertiary opacity-75"></span>
            <span class="relative inline-flex rounded-full h-3 w-3 bg-tertiary"></span>
        </div>
        <p class="text-body-md text-on-surface-variant flex-1">
            Système de surveillance <span class="font-semibold text-tertiary">actif</span>
            · Dernière analyse : {{ now()->format('H:i:s') }}
        </p>
        <span class="text-display-lg {{ $alerts['alert_count'] > 0 ? 'text-error' : 'text-tertiary' }} font-bold">
            {{ $alerts['alert_count'] }} alerte{{ $alerts['alert_count'] > 1 ? 's' : '' }}
        </span>
    </div>

    {{-- Legend --}}
    <div class="card p-5">
        <h3 class="text-label-md text-on-surface-variant uppercase tracking-wider mb-3">Règles de détection</h3>
        <div class="flex flex-wrap gap-6">
            <div class="flex items-center gap-2 text-body-md text-on-surface-variant">
                <span class="w-2.5 h-2.5 rounded-full bg-error flex-shrink-0"></span>
                Montant &gt; 10 000 000 MGA (critique)
            </div>
            <div class="flex items-center gap-2 text-body-md text-on-surface-variant">
                <span class="w-2.5 h-2.5 rounded-full bg-amber-400 flex-shrink-0"></span>
                Même montant répété ≥ 3 fois / 24h
            </div>
            <div class="flex items-center gap-2 text-body-md text-on-surface-variant">
                <span class="w-2.5 h-2.5 rounded-full bg-purple-400 flex-shrink-0"></span>
                Transaction entre 00h00 et 05h00
            </div>
            <div class="flex items-center gap-2 text-body-md text-on-surface-variant">
                <span class="w-2.5 h-2.5 rounded-full bg-gray-400 flex-shrink-0"></span>
                ≥ 5 débits en 10 minutes
            </div>
        </div>
    </div>

    {{-- Alerts --}}
    @if(empty($alerts['alerts']))
    <div class="card py-20 text-center">
        <span class="material-symbols-outlined text-[56px] text-tertiary">verified_user</span>
        <p class="text-headline-sm text-on-surface mt-4">Aucune anomalie détectée</p>
        <p class="text-body-md text-on-surface-variant mt-2">Toutes les transactions semblent normales.</p>
    </div>
    @else
    <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4">
        @foreach($alerts['alerts'] as $alert)
        <div class="card overflow-hidden border-l-4 {{ $alert['severity'] === 'critical' ? 'border-l-error' : 'border-l-amber-400' }}">
            <div class="p-5">
                <div class="flex items-start justify-between mb-4">
                    <p class="text-headline-md text-on-surface font-bold tnum">
                        {{ number_format($alert['amount'], 0, ',', ' ') }} MGA
                    </p>
                    <span class="badge {{ $alert['severity'] === 'critical' ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700' }}">
                        {{ strtoupper($alert['severity']) }}
                    </span>
                </div>
                <dl class="space-y-2">
                    <div class="flex justify-between text-body-md">
                        <dt class="text-on-surface-variant">Client</dt>
                        <dd class="font-medium text-on-surface truncate max-w-[180px]">{{ $alert['account_holder'] }}</dd>
                    </div>
                    <div class="flex justify-between text-body-md">
                        <dt class="text-on-surface-variant">Type</dt>
                        <dd class="text-on-surface">{{ str_replace('_', ' ', $alert['type']) }}</dd>
                    </div>
                    <div class="flex justify-between text-body-md">
                        <dt class="text-on-surface-variant">Référence</dt>
                        <dd class="font-mono text-[11px] text-on-surface-variant">{{ $alert['transaction_reference'] }}</dd>
                    </div>
                    <div class="flex justify-between text-body-md">
                        <dt class="text-on-surface-variant">Description</dt>
                        <dd class="text-on-surface truncate max-w-[180px]">{{ $alert['description'] }}</dd>
                    </div>
                    <div class="flex justify-between text-body-md">
                        <dt class="text-on-surface-variant">Heure</dt>
                        <dd class="text-on-surface">{{ \Carbon\Carbon::parse($alert['timestamp'])->format('d/m/Y H:i') }}</dd>
                    </div>
                </dl>
            </div>
        </div>
        @endforeach
    </div>
    @endif

</div>
@endsection
