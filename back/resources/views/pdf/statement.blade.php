<!DOCTYPE html>
<html lang="fr">
<head>
<meta charset="UTF-8">
<style>
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body { font-family: DejaVu Sans, sans-serif; font-size: 12px; color: #1a1a2e; background: #fff; }
  .header { background: linear-gradient(135deg, #1a1a2e 0%, #D92C55 100%); color: white; padding: 28px 32px; }
  .header h1 { font-size: 26px; font-weight: 700; letter-spacing: 1px; }
  .header p { font-size: 11px; opacity: 0.8; margin-top: 4px; }
  .meta { display: flex; justify-content: space-between; padding: 20px 32px; background: #f8f9fc; border-bottom: 2px solid #eee; }
  .meta-block label { font-size: 10px; text-transform: uppercase; color: #999; letter-spacing: 0.5px; }
  .meta-block p { font-size: 13px; font-weight: 600; color: #1a1a2e; margin-top: 2px; }
  .summary { display: flex; gap: 0; margin: 20px 32px; border-radius: 10px; overflow: hidden; border: 1px solid #e8e8e8; }
  .summary-item { flex: 1; padding: 16px 20px; text-align: center; }
  .summary-item:not(:last-child) { border-right: 1px solid #e8e8e8; }
  .summary-item label { font-size: 10px; text-transform: uppercase; color: #999; letter-spacing: 0.5px; }
  .summary-item .val { font-size: 16px; font-weight: 700; margin-top: 4px; }
  .credit { color: #22c55e; }
  .debit { color: #D92C55; }
  .neutral { color: #1a1a2e; }
  .section-title { margin: 20px 32px 10px; font-size: 13px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; color: #666; }
  table { width: calc(100% - 64px); margin: 0 32px; border-collapse: collapse; font-size: 11px; }
  thead tr { background: #1a1a2e; color: white; }
  thead th { padding: 10px 12px; text-align: left; font-weight: 600; font-size: 10px; text-transform: uppercase; letter-spacing: 0.5px; }
  tbody tr { border-bottom: 1px solid #f0f0f0; }
  tbody tr:nth-child(even) { background: #fafafa; }
  tbody td { padding: 9px 12px; vertical-align: middle; }
  .badge { display: inline-block; padding: 2px 8px; border-radius: 20px; font-size: 10px; font-weight: 600; }
  .badge-credit { background: #dcfce7; color: #16a34a; }
  .badge-debit { background: #ffe4e6; color: #D92C55; }
  .footer { margin: 24px 32px 0; padding: 16px 0; border-top: 1px solid #eee; display: flex; justify-content: space-between; font-size: 10px; color: #aaa; }
  .empty { text-align: center; padding: 40px; color: #999; font-size: 13px; }
</style>
</head>
<body>

<div class="header">
  <h1>SCpay — Relevé de Compte</h1>
  <p>Banque privée mobile · Généré le {{ now()->format('d/m/Y à H:i') }}</p>
</div>

<div class="meta">
  <div class="meta-block">
    <label>Titulaire</label>
    <p>{{ $user->name }}</p>
  </div>
  <div class="meta-block">
    <label>N° de compte</label>
    <p>{{ $account->account_number }}</p>
  </div>
  <div class="meta-block">
    <label>Période</label>
    <p>{{ \Carbon\Carbon::createFromFormat('Y-m', $month)->translatedFormat('F Y') }}</p>
  </div>
  <div class="meta-block">
    <label>Devise</label>
    <p>{{ $account->currency }}</p>
  </div>
</div>

<div class="summary">
  <div class="summary-item">
    <label>Solde actuel</label>
    <div class="val neutral">{{ number_format($account->balance, 0, ',', ' ') }} MGA</div>
  </div>
  <div class="summary-item">
    <label>Total crédits</label>
    <div class="val credit">+ {{ number_format($totalCredit, 0, ',', ' ') }} MGA</div>
  </div>
  <div class="summary-item">
    <label>Total débits</label>
    <div class="val debit">− {{ number_format($totalDebit, 0, ',', ' ') }} MGA</div>
  </div>
  <div class="summary-item">
    <label>Transactions</label>
    <div class="val neutral">{{ $transactions->count() }}</div>
  </div>
</div>

<div class="section-title">Détail des opérations</div>

@if($transactions->isEmpty())
  <div class="empty">Aucune transaction pour cette période.</div>
@else
<table>
  <thead>
    <tr>
      <th>Date</th>
      <th>Description</th>
      <th>Référence</th>
      <th>Type</th>
      <th style="text-align:right">Montant (MGA)</th>
      <th style="text-align:right">Solde après</th>
    </tr>
  </thead>
  <tbody>
    @foreach($transactions as $txn)
    <tr>
      <td>{{ $txn->created_at->format('d/m/Y') }}</td>
      <td>{{ Str::limit($txn->description ?? '—', 35) }}</td>
      <td style="font-size:9px;color:#999">{{ $txn->reference }}</td>
      <td>
        <span class="badge badge-{{ $txn->type }}">
          {{ $txn->type === 'credit' ? 'Crédit' : 'Débit' }}
        </span>
      </td>
      <td style="text-align:right;font-weight:600" class="{{ $txn->type }}">
        {{ $txn->type === 'credit' ? '+' : '−' }} {{ number_format($txn->amount, 0, ',', ' ') }}
      </td>
      <td style="text-align:right;color:#666">
        {{ number_format($txn->balance_after ?? 0, 0, ',', ' ') }}
      </td>
    </tr>
    @endforeach
  </tbody>
</table>
@endif

<div class="footer">
  <span>SCpay — Banque privée mobile · Relevé officiel</span>
  <span>Confidentiel — Document généré automatiquement</span>
</div>

</body>
</html>
