@extends('admin.layout')

@section('title', 'Transactions')
@section('page-title', 'Transactions')

@section('content')
<div class="space-y-6">

    {{-- Filters --}}
    <div class="card p-5">
        <form method="GET" action="{{ route('admin.transactions') }}" class="flex flex-wrap gap-4 items-end">
            <div class="flex flex-col gap-1.5">
                <label class="text-label-md text-on-surface-variant">Type</label>
                <select name="type" class="input">
                    <option value="">Tous</option>
                    <option value="credit" {{ request('type') === 'credit' ? 'selected' : '' }}>Crédit</option>
                    <option value="debit"  {{ request('type') === 'debit'  ? 'selected' : '' }}>Débit</option>
                </select>
            </div>
            <div class="flex flex-col gap-1.5">
                <label class="text-label-md text-on-surface-variant">Date début</label>
                <input type="date" name="date_from" value="{{ request('date_from') }}" class="input">
            </div>
            <div class="flex flex-col gap-1.5">
                <label class="text-label-md text-on-surface-variant">Date fin</label>
                <input type="date" name="date_to" value="{{ request('date_to') }}" class="input">
            </div>
            <div class="flex flex-col gap-1.5">
                <label class="text-label-md text-on-surface-variant">Montant min (MGA)</label>
                <input type="number" name="amount_min" value="{{ request('amount_min') }}" placeholder="0" class="input w-32">
            </div>
            <div class="flex flex-col gap-1.5">
                <label class="text-label-md text-on-surface-variant">Recherche</label>
                <input type="text" name="search" value="{{ request('search') }}" placeholder="Référence, description…" class="input w-56">
            </div>
            <button type="submit" class="btn-primary px-5 py-2.5 text-body-md rounded-lg">
                <span class="material-symbols-outlined text-[16px] align-middle mr-1">filter_list</span>Filtrer
            </button>
            <a href="{{ route('admin.transactions') }}" class="btn-secondary px-5 py-2.5 text-body-md rounded-lg inline-flex items-center">
                <span class="material-symbols-outlined text-[16px] align-middle mr-1">close</span>Reset
            </a>
        </form>
    </div>

    {{-- Table --}}
    <div class="card overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
            <h2 class="text-headline-sm text-on-surface">
                {{ $transactions->total() }} transaction{{ $transactions->total() > 1 ? 's' : '' }}
            </h2>
        </div>
        <div class="overflow-x-auto">
            <table class="w-full">
                <thead>
                    <tr class="bg-gray-50">
                        <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Date</th>
                        <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Référence</th>
                        <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Client</th>
                        <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Compte</th>
                        <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Type</th>
                        <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Catégorie</th>
                        <th class="px-6 py-3 text-right text-label-md text-on-surface-variant">Montant</th>
                        <th class="px-6 py-3 text-right text-label-md text-on-surface-variant">Solde après</th>
                        <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Description</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-gray-50">
                    @forelse($transactions as $txn)
                    <tr class="hover:bg-surface-container-low transition-colors">
                        <td class="px-6 py-3 text-body-md text-on-surface-variant whitespace-nowrap">
                            {{ $txn->created_at->format('d/m/Y H:i') }}
                        </td>
                        <td class="px-6 py-3 font-mono text-[11px] text-on-surface-variant">
                            {{ $txn->reference }}
                        </td>
                        <td class="px-6 py-3 text-body-md font-medium text-on-surface">
                            {{ $txn->account?->user?->name ?? '—' }}
                        </td>
                        <td class="px-6 py-3 font-mono text-[11px] text-on-surface-variant">
                            ****{{ substr($txn->account?->account_number ?? '', -4) }}
                        </td>
                        <td class="px-6 py-3">
                            <span class="badge badge-{{ $txn->type }}">
                                {{ $txn->type === 'credit' ? 'Crédit' : 'Débit' }}
                            </span>
                        </td>
                        <td class="px-6 py-3 text-body-md text-on-surface-variant">{{ $txn->category }}</td>
                        <td class="px-6 py-3 text-right tnum font-semibold {{ $txn->type === 'credit' ? 'text-tertiary' : 'text-error' }}">
                            {{ $txn->type === 'credit' ? '+' : '-' }}{{ number_format($txn->amount, 0, ',', ' ') }}
                        </td>
                        <td class="px-6 py-3 text-right tnum text-body-md text-on-surface-variant">
                            {{ number_format($txn->balance_after, 0, ',', ' ') }}
                        </td>
                        <td class="px-6 py-3 text-body-md text-on-surface-variant max-w-[180px] truncate">
                            {{ $txn->description }}
                        </td>
                    </tr>
                    @empty
                    <tr>
                        <td colspan="9" class="px-6 py-16 text-center text-on-surface-variant">
                            <span class="material-symbols-outlined text-[40px] block mb-2">receipt_long</span>
                            Aucune transaction trouvée.
                        </td>
                    </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
        @if($transactions->hasPages())
        <div class="px-6 py-4 border-t border-gray-100">
            {{ $transactions->withQueryString()->links() }}
        </div>
        @endif
    </div>

</div>
@endsection
