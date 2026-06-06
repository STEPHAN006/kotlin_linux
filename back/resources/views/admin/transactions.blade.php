<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Transactions — SCpay Admin</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #0f1117; color: #e5e7eb; min-height: 100vh; }
        .nav { background: #1a1d27; border-bottom: 1px solid #2a2d3a; padding: 0 24px; display: flex; align-items: center; height: 60px; gap: 16px; }
        .nav-brand { color: #d92c55; font-size: 20px; font-weight: 800; flex: 1; }
        .nav a { color: #9ca3af; font-size: 14px; text-decoration: none; padding: 6px 14px; border-radius: 8px; border: 1px solid #374151; }
        .nav a.active, .nav a:hover { background: #d92c55; color: #fff; border-color: #d92c55; }
        .container { max-width: 1200px; margin: 0 auto; padding: 32px 24px; }
        h1 { font-size: 24px; font-weight: 700; margin-bottom: 6px; }
        .subtitle { color: #6b7280; font-size: 14px; margin-bottom: 24px; }
        .filters { background: #1a1d27; border: 1px solid #2a2d3a; border-radius: 12px; padding: 18px 20px; margin-bottom: 24px; display: flex; gap: 12px; flex-wrap: wrap; align-items: flex-end; }
        .filter-group { display: flex; flex-direction: column; gap: 6px; }
        label { font-size: 12px; color: #6b7280; font-weight: 500; }
        input, select { background: #111827; border: 1px solid #374151; border-radius: 8px; color: #e5e7eb; font-size: 13px; padding: 8px 12px; outline: none; }
        input:focus, select:focus { border-color: #d92c55; }
        .btn { padding: 9px 20px; background: #d92c55; color: #fff; border: none; border-radius: 8px; font-size: 13px; font-weight: 700; cursor: pointer; }
        .btn:hover { background: #b82447; }
        .btn-outline { background: transparent; border: 1px solid #374151; color: #9ca3af; }
        table { width: 100%; border-collapse: collapse; background: #1a1d27; border: 1px solid #2a2d3a; border-radius: 12px; overflow: hidden; }
        th { text-align: left; padding: 10px 16px; color: #6b7280; font-size: 11px; font-weight: 600; text-transform: uppercase; border-bottom: 1px solid #2a2d3a; }
        td { padding: 12px 16px; border-bottom: 1px solid #1f2937; font-size: 13px; vertical-align: middle; }
        tr:hover td { background: #111827; }
        .badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 600; }
        .badge-credit { background: #052e16; color: #4ade80; border: 1px solid #166534; }
        .badge-debit { background: #2d1515; color: #fca5a5; border: 1px solid #7f1d1d; }
        .amount-credit { color: #4ade80; font-weight: 700; }
        .amount-debit { color: #fca5a5; font-weight: 700; }
        .pagination { display: flex; gap: 8px; margin-top: 20px; justify-content: center; }
        .pagination a, .pagination span { padding: 6px 12px; border-radius: 8px; border: 1px solid #374151; color: #9ca3af; text-decoration: none; font-size: 13px; }
        .pagination .active { background: #d92c55; color: #fff; border-color: #d92c55; }
    </style>
</head>
<body>
<nav class="nav">
    <div class="nav-brand">SCpay Admin</div>
    <a href="{{ route('admin.dashboard') }}">Dashboard</a>
    <a href="{{ route('admin.support.index') }}">Support</a>
    <a href="{{ route('admin.transactions') }}" class="active">Transactions</a>
    <a href="{{ route('admin.fraud') }}">Fraudes</a>
    <a href="{{ route('admin.logout') }}" onclick="return confirm('Se déconnecter ?')">Quitter</a>
</nav>
<div class="container">
    <h1>Toutes les transactions</h1>
    <p class="subtitle">{{ $transactions->total() }} transactions au total</p>

    <form method="GET" action="{{ route('admin.transactions') }}">
        <div class="filters">
            <div class="filter-group">
                <label>Type</label>
                <select name="type">
                    <option value="">Tous</option>
                    <option value="credit" {{ request('type') === 'credit' ? 'selected' : '' }}>Crédit</option>
                    <option value="debit" {{ request('type') === 'debit' ? 'selected' : '' }}>Débit</option>
                </select>
            </div>
            <div class="filter-group">
                <label>Date début</label>
                <input type="date" name="date_from" value="{{ request('date_from') }}">
            </div>
            <div class="filter-group">
                <label>Date fin</label>
                <input type="date" name="date_to" value="{{ request('date_to') }}">
            </div>
            <div class="filter-group">
                <label>Montant min (MGA)</label>
                <input type="number" name="amount_min" value="{{ request('amount_min') }}" placeholder="0" style="width:130px">
            </div>
            <div class="filter-group">
                <label>Recherche</label>
                <input type="text" name="search" value="{{ request('search') }}" placeholder="Référence, description..." style="width:220px">
            </div>
            <button type="submit" class="btn">Filtrer</button>
            <a href="{{ route('admin.transactions') }}" class="btn btn-outline" style="padding:9px 16px;text-decoration:none">Reset</a>
        </div>
    </form>

    <table>
        <thead>
            <tr>
                <th>Date</th>
                <th>Référence</th>
                <th>Client</th>
                <th>Compte</th>
                <th>Type</th>
                <th>Catégorie</th>
                <th>Montant</th>
                <th>Solde après</th>
                <th>Description</th>
            </tr>
        </thead>
        <tbody>
            @forelse($transactions as $txn)
            <tr>
                <td style="white-space:nowrap;color:#6b7280">{{ $txn->created_at->format('d/m/Y H:i') }}</td>
                <td style="font-family:monospace;font-size:11px;color:#9ca3af">{{ $txn->reference }}</td>
                <td>{{ $txn->account?->user?->name ?? '—' }}</td>
                <td style="font-family:monospace;font-size:11px">****{{ substr($txn->account?->account_number ?? '', -4) }}</td>
                <td><span class="badge badge-{{ $txn->type }}">{{ $txn->type === 'credit' ? 'Crédit' : 'Débit' }}</span></td>
                <td style="color:#6b7280;font-size:12px">{{ $txn->category }}</td>
                <td class="amount-{{ $txn->type }}">
                    {{ $txn->type === 'credit' ? '+' : '-' }}{{ number_format($txn->amount, 0, ',', ' ') }}
                </td>
                <td style="color:#6b7280">{{ number_format($txn->balance_after, 0, ',', ' ') }}</td>
                <td style="color:#9ca3af;max-width:180px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ $txn->description }}</td>
            </tr>
            @empty
            <tr><td colspan="9" style="text-align:center;padding:40px;color:#4b5563">Aucune transaction trouvée.</td></tr>
            @endforelse
        </tbody>
    </table>

    <div class="pagination">
        {{ $transactions->withQueryString()->links('pagination::simple-bootstrap-4') }}
    </div>
</div>
</body>
</html>
