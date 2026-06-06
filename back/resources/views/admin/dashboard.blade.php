<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SCpay Admin — Dashboard</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #0f1117; color: #e5e7eb; min-height: 100vh; }
        .nav { background: #1a1d27; border-bottom: 1px solid #2a2d3a; padding: 0 24px; display: flex; align-items: center; height: 60px; gap: 16px; }
        .nav-brand { color: #d92c55; font-size: 20px; font-weight: 800; flex: 1; }
        .nav a { color: #9ca3af; font-size: 14px; text-decoration: none; padding: 6px 14px; border-radius: 8px; border: 1px solid #374151; }
        .nav a.active, .nav a:hover { background: #d92c55; color: #fff; border-color: #d92c55; }
        .nav-user { color: #6b7280; font-size: 13px; }
        .container { max-width: 1200px; margin: 0 auto; padding: 32px 24px; }
        h1 { font-size: 24px; font-weight: 700; margin-bottom: 6px; }
        .subtitle { color: #6b7280; font-size: 14px; margin-bottom: 28px; }
        .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 32px; }
        .stat { background: #1a1d27; border: 1px solid #2a2d3a; border-radius: 12px; padding: 20px 24px; }
        .stat-val { font-size: 28px; font-weight: 800; color: #d92c55; margin-bottom: 4px; }
        .stat-label { color: #6b7280; font-size: 13px; }
        .stat.green .stat-val { color: #4ade80; }
        .stat.blue .stat-val { color: #60a5fa; }
        .stat.yellow .stat-val { color: #fbbf24; }
        .grid2 { display: grid; grid-template-columns: 2fr 1fr; gap: 24px; }
        .card { background: #1a1d27; border: 1px solid #2a2d3a; border-radius: 14px; padding: 22px 24px; }
        .card h2 { font-size: 16px; font-weight: 700; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }
        .badge { display: inline-block; padding: 2px 9px; border-radius: 20px; font-size: 11px; font-weight: 600; }
        .badge-credit { background: #052e16; color: #4ade80; border: 1px solid #166534; }
        .badge-debit { background: #2d1515; color: #fca5a5; border: 1px solid #7f1d1d; }
        .badge-warn { background: #451a03; color: #fbbf24; border: 1px solid #92400e; }
        .badge-crit { background: #2d1515; color: #f87171; border: 1px solid #7f1d1d; }
        table { width: 100%; border-collapse: collapse; }
        th { text-align: left; padding: 8px 12px; color: #6b7280; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid #2a2d3a; }
        td { padding: 10px 12px; border-bottom: 1px solid #1f2937; font-size: 13px; }
        tr:hover td { background: #111827; }
        .alert-item { padding: 12px 0; border-bottom: 1px solid #1f2937; }
        .alert-item:last-child { border-bottom: none; }
        .alert-title { font-size: 14px; font-weight: 600; margin-bottom: 4px; }
        .alert-meta { font-size: 12px; color: #6b7280; }
        .dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 6px; }
        .dot-red { background: #ef4444; }
        .dot-yellow { background: #f59e0b; }
        .support-link { display: block; margin-top: 16px; text-align: center; color: #d92c55; font-size: 14px; font-weight: 600; text-decoration: none; }
        .support-link:hover { text-decoration: underline; }
        @media (max-width: 768px) { .grid2 { grid-template-columns: 1fr; } }
    </style>
</head>
<body>
<nav class="nav">
    <div class="nav-brand">SCpay Admin</div>
    <a href="{{ route('admin.dashboard') }}" class="active">Dashboard</a>
    <a href="{{ route('admin.support.index') }}">Support</a>
    <a href="{{ route('admin.transactions') }}">Transactions</a>
    <a href="{{ route('admin.fraud') }}">Fraudes</a>
    <div class="nav-user">{{ session('admin_name') }}</div>
    <a href="{{ route('admin.logout') }}" onclick="return confirm('Se déconnecter ?')">Quitter</a>
</nav>
<div class="container">
    <h1>Tableau de bord superviseur</h1>
    <p class="subtitle">Dernière mise à jour : {{ now()->format('d/m/Y H:i:s') }}</p>

    <div class="stats">
        <div class="stat">
            <div class="stat-val">{{ number_format($stats['total_users']) }}</div>
            <div class="stat-label">Clients actifs</div>
        </div>
        <div class="stat green">
            <div class="stat-val">{{ number_format($stats['total_balance'] / 1000000, 1) }}M</div>
            <div class="stat-label">Solde total (MGA)</div>
        </div>
        <div class="stat blue">
            <div class="stat-val">{{ number_format($stats['total_transactions']) }}</div>
            <div class="stat-label">Transactions</div>
        </div>
        <div class="stat yellow">
            <div class="stat-val">{{ number_format($stats['total_volume'] / 1000000, 1) }}M</div>
            <div class="stat-label">Volume traité (MGA)</div>
        </div>
        <div class="stat">
            <div class="stat-val">{{ $openTickets }}</div>
            <div class="stat-label">Tickets support ouverts</div>
        </div>
        <div class="stat">
            <div class="stat-val">{{ $fraudAlerts['alert_count'] }}</div>
            <div class="stat-label">Alertes fraude actives</div>
        </div>
    </div>

    <div class="grid2">
        <div class="card">
            <h2>Transactions récentes</h2>
            <table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Client</th>
                        <th>Type</th>
                        <th>Montant</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                    @foreach($recentTransactions as $txn)
                    <tr>
                        <td style="color:#6b7280;white-space:nowrap">{{ $txn->created_at->format('d/m H:i') }}</td>
                        <td>{{ $txn->account?->user?->name ?? '—' }}</td>
                        <td><span class="badge badge-{{ $txn->type }}">{{ $txn->type === 'credit' ? 'Crédit' : 'Débit' }}</span></td>
                        <td style="font-weight:600">{{ number_format($txn->amount, 0, ',', ' ') }} MGA</td>
                        <td style="color:#9ca3af;max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ $txn->description }}</td>
                    </tr>
                    @endforeach
                </tbody>
            </table>
            <a href="{{ route('admin.transactions') }}" class="support-link">Voir toutes les transactions →</a>
        </div>

        <div class="card">
            <h2>Alertes fraude</h2>
            @forelse($fraudAlerts['alerts'] as $alert)
            <div class="alert-item">
                <div class="alert-title">
                    <span class="dot dot-{{ $alert['severity'] === 'critical' ? 'red' : 'yellow' }}"></span>
                    {{ number_format($alert['amount'], 0, ',', ' ') }} MGA
                </div>
                <div class="alert-meta">
                    {{ $alert['account_holder'] }} · {{ $alert['type'] }}<br>
                    <span class="badge badge-{{ $alert['severity'] === 'critical' ? 'crit' : 'warn' }}">{{ strtoupper($alert['severity']) }}</span>
                </div>
            </div>
            @empty
            <p style="color:#4b5563;text-align:center;padding:24px">Aucune alerte active</p>
            @endforelse
            <a href="{{ route('admin.fraud') }}" class="support-link">Voir toutes les alertes →</a>
        </div>
    </div>
</div>
</body>
</html>
