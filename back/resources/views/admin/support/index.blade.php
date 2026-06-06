<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SCpay Admin — Tickets Support</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #0f1117; color: #e5e7eb; min-height: 100vh; }
        .nav { background: #1a1d27; border-bottom: 1px solid #2a2d3a; padding: 0 24px; display: flex; align-items: center; height: 60px; gap: 16px; }
        .nav-brand { color: #d92c55; font-size: 20px; font-weight: 800; flex: 1; }
        .nav-info { color: #6b7280; font-size: 13px; }
        .nav a { color: #9ca3af; font-size: 14px; text-decoration: none; padding: 6px 14px; border-radius: 8px; border: 1px solid #374151; }
        .nav a:hover { background: #1f2937; color: #fff; }
        .container { max-width: 1100px; margin: 0 auto; padding: 32px 24px; }
        h1 { font-size: 24px; font-weight: 700; margin-bottom: 8px; }
        .subtitle { color: #6b7280; font-size: 14px; margin-bottom: 28px; }
        .stats { display: flex; gap: 16px; margin-bottom: 32px; }
        .stat { background: #1a1d27; border: 1px solid #2a2d3a; border-radius: 12px; padding: 18px 24px; flex: 1; }
        .stat-val { font-size: 28px; font-weight: 800; color: #d92c55; }
        .stat-label { color: #6b7280; font-size: 13px; margin-top: 4px; }
        .alert { background: #1a2d1a; border: 1px solid #166534; border-radius: 8px; padding: 12px 16px; color: #86efac; font-size: 14px; margin-bottom: 24px; }
        table { width: 100%; border-collapse: collapse; }
        th { text-align: left; padding: 10px 16px; color: #6b7280; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid #2a2d3a; }
        td { padding: 14px 16px; border-bottom: 1px solid #1f2937; font-size: 14px; vertical-align: middle; }
        tr:hover td { background: #1a1d27; }
        .badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-open { background: #052e16; color: #4ade80; border: 1px solid #166534; }
        .badge-closed { background: #1f2937; color: #6b7280; border: 1px solid #374151; }
        .btn-view { background: #d92c55; color: #fff; border: none; padding: 7px 18px; border-radius: 8px; font-size: 13px; font-weight: 600; cursor: pointer; text-decoration: none; }
        .btn-view:hover { background: #b82447; }
        .empty { text-align: center; padding: 60px; color: #4b5563; }
        .empty-icon { font-size: 48px; margin-bottom: 16px; }
        .msg-preview { color: #9ca3af; max-width: 300px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
        .unread-dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; background: #d92c55; margin-right: 6px; }
    </style>
</head>
<body>
    <nav class="nav">
        <div class="nav-brand">SCpay Admin</div>
        <div class="nav-info">Connecté en tant que {{ session('admin_name') }}</div>
        <a href="{{ route('admin.logout') }}" onclick="return confirm('Se déconnecter ?')">Déconnexion</a>
    </nav>
    <div class="container">
        <h1>Tickets de Support</h1>
        <p class="subtitle">Gérez les demandes des clients SCpay</p>

        @if(session('success'))
            <div class="alert">{{ session('success') }}</div>
        @endif

        <div class="stats">
            <div class="stat">
                <div class="stat-val">{{ $tickets->where('status','open')->count() }}</div>
                <div class="stat-label">Tickets ouverts</div>
            </div>
            <div class="stat">
                <div class="stat-val">{{ $tickets->where('status','closed')->count() }}</div>
                <div class="stat-label">Tickets fermés</div>
            </div>
            <div class="stat">
                <div class="stat-val">{{ $tickets->count() }}</div>
                <div class="stat-label">Total</div>
            </div>
        </div>

        @if($tickets->isEmpty())
            <div class="empty">
                <div class="empty-icon">💬</div>
                <p>Aucun ticket de support pour l'instant.</p>
            </div>
        @else
            <table>
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Client</th>
                        <th>Sujet</th>
                        <th>Dernier message</th>
                        <th>Statut</th>
                        <th>Date</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    @foreach($tickets as $ticket)
                    @php
                        $lastMsg = $ticket->messages->last();
                        $hasUnread = $lastMsg && $lastMsg->sender === 'user';
                    @endphp
                    <tr>
                        <td style="color:#6b7280">#{{ $ticket->id }}</td>
                        <td>
                            @if($hasUnread)<span class="unread-dot"></span>@endif
                            <strong>{{ $ticket->user->name ?? 'Inconnu' }}</strong><br>
                            <small style="color:#6b7280">{{ $ticket->user->email ?? '' }}</small>
                        </td>
                        <td>{{ $ticket->subject }}</td>
                        <td class="msg-preview">
                            @if($lastMsg)
                                <span style="color:{{ $lastMsg->sender === 'user' ? '#fbbf24' : '#6b7280' }}">
                                    [{{ $lastMsg->sender === 'user' ? 'Client' : 'Admin' }}]
                                </span>
                                {{ $lastMsg->message }}
                            @else
                                <em style="color:#4b5563">Aucun message</em>
                            @endif
                        </td>
                        <td><span class="badge badge-{{ $ticket->status }}">{{ $ticket->status === 'open' ? 'Ouvert' : 'Fermé' }}</span></td>
                        <td style="color:#6b7280;font-size:12px">{{ $ticket->updated_at->format('d/m H:i') }}</td>
                        <td><a href="{{ route('admin.support.show', $ticket->id) }}" class="btn-view">Répondre</a></td>
                    </tr>
                    @endforeach
                </tbody>
            </table>
        @endif
    </div>
</body>
</html>
