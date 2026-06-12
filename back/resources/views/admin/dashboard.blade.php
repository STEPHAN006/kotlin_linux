@extends('admin.layout')

@section('title', 'Dashboard')
@section('page-title', 'Tableau de bord')

@section('content')
<div class="space-y-6">

    {{-- Stats --}}
    <div class="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <div class="card p-5">
            <p class="text-label-md text-on-surface-variant uppercase tracking-wider mb-2">Clients</p>
            <p class="text-display-lg text-primary">{{ number_format($stats['total_users']) }}</p>
            <p class="text-body-md text-on-surface-variant mt-1">actifs</p>
        </div>
        <div class="card p-5">
            <p class="text-label-md text-on-surface-variant uppercase tracking-wider mb-2">Solde total</p>
            <p class="text-display-lg text-tertiary">{{ number_format($stats['total_balance'] / 1000000, 1) }}M</p>
            <p class="text-body-md text-on-surface-variant mt-1">MGA</p>
        </div>
        <div class="card p-5">
            <p class="text-label-md text-on-surface-variant uppercase tracking-wider mb-2">Transactions</p>
            <p class="text-display-lg text-navy">{{ number_format($stats['total_transactions']) }}</p>
            <p class="text-body-md text-on-surface-variant mt-1">au total</p>
        </div>
        <div class="card p-5">
            <p class="text-label-md text-on-surface-variant uppercase tracking-wider mb-2">Volume</p>
            <p class="text-display-lg text-navy">{{ number_format($stats['total_volume'] / 1000000, 1) }}M</p>
            <p class="text-body-md text-on-surface-variant mt-1">MGA traités</p>
        </div>
        <div class="card p-5">
            <p class="text-label-md text-on-surface-variant uppercase tracking-wider mb-2">Support</p>
            <p class="text-display-lg {{ $openTickets > 0 ? 'text-primary' : 'text-tertiary' }}">{{ $openTickets }}</p>
            <p class="text-body-md text-on-surface-variant mt-1">tickets ouverts</p>
        </div>
        <div class="card p-5">
            <p class="text-label-md text-on-surface-variant uppercase tracking-wider mb-2">Fraude</p>
            <p class="text-display-lg {{ $fraudAlerts['alert_count'] > 0 ? 'text-error' : 'text-tertiary' }}">{{ $fraudAlerts['alert_count'] }}</p>
            <p class="text-body-md text-on-surface-variant mt-1">alertes actives</p>
        </div>
    </div>

    {{-- Main grid --}}
    <div class="grid grid-cols-1 xl:grid-cols-3 gap-6">

        {{-- Recent transactions --}}
        <div class="card xl:col-span-2">
            <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
                <h2 class="text-headline-sm text-on-surface flex items-center gap-2">
                    <span class="material-symbols-outlined text-primary">swap_horiz</span>
                    Transactions récentes
                </h2>
                <a href="{{ route('admin.transactions') }}"
                   class="text-primary text-body-md font-medium hover:underline flex items-center gap-1">
                    Tout voir
                    <span class="material-symbols-outlined text-[16px]">arrow_forward</span>
                </a>
            </div>
            <div class="overflow-x-auto">
                <table class="w-full">
                    <thead>
                        <tr class="bg-gray-50">
                            <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Date</th>
                            <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Client</th>
                            <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Type</th>
                            <th class="px-6 py-3 text-right text-label-md text-on-surface-variant">Montant</th>
                            <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Description</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-50">
                        @foreach($recentTransactions as $txn)
                        <tr class="hover:bg-surface-container-low transition-colors">
                            <td class="px-6 py-3 text-body-md text-on-surface-variant whitespace-nowrap">
                                {{ $txn->created_at->format('d/m H:i') }}
                            </td>
                            <td class="px-6 py-3 text-body-md text-on-surface font-medium">
                                {{ $txn->account?->user?->name ?? '—' }}
                            </td>
                            <td class="px-6 py-3">
                                <span class="badge badge-{{ $txn->type }}">
                                    {{ $txn->type === 'credit' ? 'Crédit' : 'Débit' }}
                                </span>
                            </td>
                            <td class="px-6 py-3 text-right tnum text-body-md font-semibold text-on-surface">
                                {{ number_format($txn->amount, 0, ',', ' ') }} MGA
                            </td>
                            <td class="px-6 py-3 text-body-md text-on-surface-variant max-w-[200px] truncate">
                                {{ $txn->description }}
                            </td>
                        </tr>
                        @endforeach
                    </tbody>
                </table>
            </div>
        </div>

        {{-- Fraud alerts --}}
        <div class="card">
            <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
                <h2 class="text-headline-sm text-on-surface flex items-center gap-2">
                    <span class="material-symbols-outlined text-error">security</span>
                    Alertes fraude
                </h2>
                <a href="{{ route('admin.fraud') }}"
                   class="text-primary text-body-md font-medium hover:underline flex items-center gap-1">
                    Tout voir
                    <span class="material-symbols-outlined text-[16px]">arrow_forward</span>
                </a>
            </div>
            <div class="divide-y divide-gray-50 px-6">
                @forelse($fraudAlerts['alerts'] as $alert)
                <div class="py-4">
                    <div class="flex items-start gap-3">
                        <div class="w-2 h-2 rounded-full mt-1.5 flex-shrink-0
                            {{ $alert['severity'] === 'critical' ? 'bg-error' : 'bg-amber-400' }}"></div>
                        <div class="flex-1 min-w-0">
                            <p class="text-body-md font-semibold text-on-surface">
                                {{ number_format($alert['amount'], 0, ',', ' ') }} MGA
                            </p>
                            <p class="text-body-md text-on-surface-variant truncate">{{ $alert['account_holder'] }}</p>
                            <p class="text-body-md text-on-surface-variant">{{ $alert['type'] }}</p>
                            <span class="badge {{ $alert['severity'] === 'critical' ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700' }} mt-1">
                                {{ strtoupper($alert['severity']) }}
                            </span>
                        </div>
                    </div>
                </div>
                @empty
                <div class="py-12 text-center">
                    <span class="material-symbols-outlined text-[40px] text-tertiary">verified_user</span>
                    <p class="text-body-md text-on-surface-variant mt-2">Aucune alerte active</p>
                </div>
                @endforelse
            </div>
        </div>

    </div>
</div>
@endsection
