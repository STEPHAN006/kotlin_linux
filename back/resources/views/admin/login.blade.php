<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SCpay Admin — Connexion</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #0f1117; min-height: 100vh; display: flex; align-items: center; justify-content: center; }
        .card { background: #1a1d27; border: 1px solid #2a2d3a; border-radius: 16px; padding: 40px; width: 100%; max-width: 400px; }
        .logo { text-align: center; margin-bottom: 32px; }
        .logo h1 { color: #d92c55; font-size: 28px; font-weight: 800; letter-spacing: -0.5px; }
        .logo p { color: #6b7280; font-size: 14px; margin-top: 6px; }
        .form-group { margin-bottom: 20px; }
        label { display: block; color: #9ca3af; font-size: 13px; font-weight: 500; margin-bottom: 8px; }
        input { width: 100%; padding: 12px 16px; background: #111827; border: 1px solid #374151; border-radius: 10px; color: #fff; font-size: 15px; outline: none; transition: border-color 0.2s; }
        input:focus { border-color: #d92c55; }
        .btn { width: 100%; padding: 14px; background: #d92c55; color: #fff; border: none; border-radius: 10px; font-size: 16px; font-weight: 700; cursor: pointer; transition: background 0.2s; margin-top: 8px; }
        .btn:hover { background: #b82447; }
        .error { background: #2d1515; border: 1px solid #7f1d1d; border-radius: 8px; padding: 12px 16px; color: #fca5a5; font-size: 14px; margin-bottom: 20px; }
    </style>
</head>
<body>
    <div class="card">
        <div class="logo">
            <h1>SCpay</h1>
            <p>Panneau d'administration — Support</p>
        </div>

        @if($errors->any())
            <div class="error">{{ $errors->first() }}</div>
        @endif

        <form method="POST" action="{{ route('admin.login.post') }}">
            @csrf
            <div class="form-group">
                <label for="email">Email administrateur</label>
                <input type="email" id="email" name="email" value="{{ old('email') }}" placeholder="admin@scpay.mg" required autofocus>
            </div>
            <div class="form-group">
                <label for="password">Mot de passe</label>
                <input type="password" id="password" name="password" placeholder="••••••••" required>
            </div>
            <button type="submit" class="btn">Se connecter</button>
        </form>
    </div>
</body>
</html>
