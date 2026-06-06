<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ticket #{{ $ticket->id }} — SCpay Admin</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #0f1117; color: #e5e7eb; min-height: 100vh; }
        .nav { background: #1a1d27; border-bottom: 1px solid #2a2d3a; padding: 0 24px; display: flex; align-items: center; height: 60px; gap: 16px; }
        .nav-brand { color: #d92c55; font-size: 20px; font-weight: 800; }
        .nav a { color: #9ca3af; font-size: 14px; text-decoration: none; padding: 6px 14px; border-radius: 8px; border: 1px solid #374151; }
        .nav a:hover { background: #1f2937; color: #fff; }
        .nav-sep { color: #374151; }
        .nav-title { flex: 1; color: #e5e7eb; font-size: 15px; }
        .container { max-width: 800px; margin: 0 auto; padding: 32px 24px; }
        .ticket-header { background: #1a1d27; border: 1px solid #2a2d3a; border-radius: 14px; padding: 22px 24px; margin-bottom: 24px; display: flex; align-items: center; gap: 16px; }
        .avatar { width: 48px; height: 48px; border-radius: 50%; background: linear-gradient(135deg, #d92c55, #7c3aed); display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: 800; color: #fff; flex-shrink: 0; }
        .ticket-info h2 { font-size: 17px; font-weight: 700; }
        .ticket-info p { color: #6b7280; font-size: 13px; margin-top: 3px; }
        .badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
        .badge-open { background: #052e16; color: #4ade80; border: 1px solid #166534; }
        .badge-closed { background: #1f2937; color: #6b7280; border: 1px solid #374151; }
        .messages { display: flex; flex-direction: column; gap: 16px; margin-bottom: 28px; }
        .msg { max-width: 78%; }
        .msg-user { align-self: flex-end; }
        .msg-admin { align-self: flex-start; }
        .msg-bubble { padding: 14px 18px; border-radius: 20px; font-size: 15px; line-height: 1.55; white-space: pre-wrap; word-break: break-word; }
        .msg-user .msg-bubble { background: #d92c55; color: #fff; border-bottom-right-radius: 4px; }
        .msg-admin .msg-bubble { background: #1f2937; color: #e5e7eb; border-bottom-left-radius: 4px; }
        .msg-meta { font-size: 11px; color: #4b5563; margin-top: 5px; padding: 0 4px; }
        .msg-user .msg-meta { text-align: right; }
        .msg-label { font-weight: 600; color: #9ca3af; }
        .reply-box { background: #1a1d27; border: 1px solid #2a2d3a; border-radius: 14px; padding: 22px 24px; }
        .reply-box h3 { font-size: 15px; font-weight: 700; margin-bottom: 16px; }
        textarea { width: 100%; background: #111827; border: 1px solid #374151; border-radius: 10px; color: #e5e7eb; font-size: 15px; padding: 14px 16px; resize: vertical; min-height: 100px; outline: none; font-family: inherit; transition: border-color 0.2s; }
        textarea:focus { border-color: #d92c55; }
        .actions { display: flex; gap: 12px; margin-top: 14px; }
        .btn { padding: 12px 24px; border: none; border-radius: 10px; font-size: 15px; font-weight: 700; cursor: pointer; text-decoration: none; display: inline-block; transition: opacity 0.2s; }
        .btn:hover { opacity: 0.85; }
        .btn-primary { background: #d92c55; color: #fff; }
        .btn-secondary { background: #1f2937; color: #9ca3af; border: 1px solid #374151; }
        .btn-danger { background: #7f1d1d; color: #fca5a5; border: 1px solid #991b1b; }
        .alert { background: #1a2d1a; border: 1px solid #166534; border-radius: 8px; padding: 12px 16px; color: #86efac; font-size: 14px; margin-bottom: 20px; }
        .closed-notice { background: #1f2937; border: 1px solid #374151; border-radius: 10px; padding: 16px; text-align: center; color: #6b7280; margin-top: 20px; }
    </style>
</head>
<body>
    <nav class="nav">
        <div class="nav-brand">SCpay</div>
        <span class="nav-sep">/</span>
        <a href="{{ route('admin.support.index') }}">Support</a>
        <span class="nav-sep">/</span>
        <div class="nav-title">Ticket #{{ $ticket->id }}</div>
        @if($ticket->status === 'open')
        <form method="POST" action="{{ route('admin.support.close', $ticket->id) }}" style="display:inline">
            @csrf
            <button type="submit" class="btn btn-danger" onclick="return confirm('Fermer ce ticket ?')" style="padding:6px 14px;font-size:13px">Fermer le ticket</button>
        </form>
        @endif
    </nav>
    <div class="container">

        @if(session('success'))
            <div class="alert">{{ session('success') }}</div>
        @endif

        <div class="ticket-header">
            <div class="avatar">{{ strtoupper(substr($ticket->user->name ?? 'U', 0, 1)) }}</div>
            <div class="ticket-info">
                <h2>{{ $ticket->user->name ?? 'Client inconnu' }}</h2>
                <p>{{ $ticket->user->email ?? '' }} · {{ $ticket->user->phone ?? '' }}</p>
            </div>
            <span class="badge badge-{{ $ticket->status }}">{{ $ticket->status === 'open' ? 'Ouvert' : 'Fermé' }}</span>
        </div>

        <div class="messages">
            @foreach($ticket->messages as $msg)
            <div class="msg msg-{{ $msg->sender }}">
                <div class="msg-bubble">{{ $msg->message }}</div>
                <div class="msg-meta">
                    <span class="msg-label">{{ $msg->sender === 'admin' ? 'SCpay Admin' : $ticket->user->name }}</span>
                    · {{ $msg->created_at->format('d/m/Y H:i') }}
                </div>
            </div>
            @endforeach
        </div>

        @if($ticket->status === 'open')
        <div class="reply-box">
            <h3>Répondre au client</h3>
            <form method="POST" action="{{ route('admin.support.reply', $ticket->id) }}">
                @csrf
                <textarea name="message" placeholder="Tapez votre réponse ici..." required autofocus>{{ old('message') }}</textarea>
                @error('message')
                    <p style="color:#fca5a5;font-size:13px;margin-top:8px">{{ $message }}</p>
                @enderror
                <div class="actions">
                    <button type="submit" class="btn btn-primary">Envoyer la réponse</button>
                    <a href="{{ route('admin.support.index') }}" class="btn btn-secondary">Retour à la liste</a>
                </div>
            </form>
        </div>
        @else
        <div class="closed-notice">
            Ce ticket est fermé. <a href="{{ route('admin.support.index') }}" style="color:#d92c55">Retour à la liste</a>
        </div>
        @endif
    </div>
</body>
</html>
