<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Alertes Fraude — SCpay Admin</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #0f1117; color: #e5e7eb; min-height: 100vh; }
        .nav { background: #1a1d27; border-bottom: 1px solid #2a2d3a; padding: 0 24px; display: flex; align-items: center; height: 60px; gap: 16px; }
        .nav-brand { color: #d92c55; font-size: 20px; font-weight: 800; flex: 1; }
        .nav a { color: #9ca3af; font-size: 14px; text-decoration: none; padding: 6px 14px; border-radius: 8px; border: 1px solid #374151; }
        .nav a.active, .nav a:hover { background: #d92c55; color: #fff; border-color: #d92c55; }
        .container { max-width: 1100px; margin: 0 auto; padding: 32px 24px; }
        h1 { font-size: 24px; font-weight: 700; margin-bottom: 6px; }
        .subtitle { color: #6b7280; font-size: 14px; margin-bottom: 28px; }
        .status-bar { background: #1a1d27; border: 1px solid #2a2d3a; border-radius: 12px; padding: 16px 20px; margin-bottom: 24px; display: flex; align-items: center; gap: 16px; }
        .status-dot { width: 10px; height: 10px; border-radius: 50%; background: #4ade80; box-shadow: 0 0 6px #4ade80; animation: pulse 2s infinite; }
        @keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.5} }
        .status-text { font-size: 14px; color: #9ca3af; }
        .status-text strong { color: #4ade80; }
        .alert-count { margin-left: auto; font-size: 22px; font-weight: 800; color: #d92c55; }
        .alerts-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px; }
        .alert-card { background: #1a1d27; border-radius: 14px; padding: 20px; border-left: 4px solid; }
        .alert-card.critical { border-color: #ef4444; }
        .alert-card.warning { border-color: #f59e0b; }
        .alert-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
        .alert-amount { font-size: 22px; font-weight: 800; color: #e5e7eb; }
        .badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 700; text-transform: uppercase; }
        .badge-crit { background: #2d1515; color: #f87171; border: 1px solid #7f1d1d; }
        .badge-warn { background: #451a03; color: #fbbf24; border: 1px solid #92400e; }
        .alert-field { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 13px; }
        .alert-field .key { color: #6b7280; }
        .alert-field .val { color: #e5e7eb; font-weight: 500; text-align: right; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
        .empty { text-align: center; padding: 80px; color: #4b5563; }
        .empty-icon { font-size: 48px; margin-bottom: 16px; }
        .legend { background: #1a1d27; border: 1px solid #2a2d3a; border-radius: 12px; padding: 20px; margin-bottom: 24px; }
        .legend h3 { font-size: 14px; font-weight: 700; margin-bottom: 12px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.05em; }
        .legend-items { display: flex; gap: 24px; flex-wrap: wrap; }
        .legend-item { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #6b7280; }
        .legend-dot { width: 8px; height: 8px; border-radius: 50%; }
    </style>
</head>
<body>
<nav class="nav">
    <div class="nav-brand">SCpay Admin</div>
    <a href="{{ route('admin.dashboard') }}">Dashboard</a>
    <a href="{{ route('admin.support.index') }}">Support</a>
    <a href="{{ route('admin.transactions') }}">Transactions</a>
    <a href="{{ route('admin.fraud') }}" class="active">Fraudes</a>
    <a href="{{ route('admin.logout') }}" onclick="return confirm('Se déconnecter ?')">Quitter</a>
</nav>
<div class="container">
    <h1>Détection d'anomalies</h1>
    <p class="subtitle">Surveillance temps réel — transactions suspectes automatiquement détectées</p>

    <div class="status-bar">
        <div class="status-dot"></div>
        <div class="status-text">Système de surveillance <strong>actif</strong> · Dernière analyse : {{ now()->format('H:i:s') }}</div>
        <div class="alert-count">{{ $alerts['alert_count'] }} alerte{{ $alerts['alert_count'] > 1 ? 's' : '' }}</div>
    </div>

    <div class="legend">
        <h3>Règles de détection</h3>
        <div class="legend-items">
            <div class="legend-item"><div class="legend-dot" style="background:#ef4444"></div> Montant > 10 000 000 MGA (critique)</div>
            <div class="legend-item"><div class="legend-dot" style="background:#f59e0b"></div> Même montant répété ≥ 3 fois / 24h</div>
            <div class="legend-item"><div class="legend-dot" style="background:#8b5cf6"></div> Transaction entre 00h00 et 05h00</div>
            <div class="legend-item"><div class="legend-dot" style="background:#6b7280"></div> ≥ 5 débits en 10 minutes</div>
        </div>
    </div>

    @if(empty($alerts['alerts']))
    <div class="empty">
        <div class="empty-icon">✅</div>
        <p style="font-size:18px;font-weight:600;color:#e5e7eb;margin-bottom:8px">Aucune anomalie détectée</p>
        <p>Toutes les transactions semblent normales.</p>
    </div>
    @else
    <div class="alerts-grid">
        @foreach($alerts['alerts'] as $alert)
        <div class="alert-card {{ $alert['severity'] }}">
            <div class="alert-header">
                <div class="alert-amount">{{ number_format($alert['amount'], 0, ',', ' ') }} MGA</div>
                <span class="badge badge-{{ $alert['severity'] === 'critical' ? 'crit' : 'warn' }}">{{ $alert['severity'] }}</span>
            </div>
            <div class="alert-field">
                <span class="key">Client</span>
                <span class="val">{{ $alert['account_holder'] }}</span>
            </div>
            <div class="alert-field">
                <span class="key">Type</span>
                <span class="val">{{ str_replace('_', ' ', $alert['type']) }}</span>
            </div>
            <div class="alert-field">
                <span class="key">Référence</span>
                <span class="val" style="font-family:monospace;font-size:11px">{{ $alert['transaction_reference'] }}</span>
            </div>
            <div class="alert-field">
                <span class="key">Description</span>
                <span class="val">{{ $alert['description'] }}</span>
            </div>
            <div class="alert-field">
                <span class="key">Heure</span>
                <span class="val">{{ \Carbon\Carbon::parse($alert['timestamp'])->format('d/m/Y H:i') }}</span>
            </div>
        </div>
        @endforeach
    </div>
    @endif
</div>
</body>
</html>
