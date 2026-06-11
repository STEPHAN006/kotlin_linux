<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Code OTP SCpay</title>
    <style>
        body { margin: 0; padding: 0; background: #f4f5f7; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }
        .wrapper { max-width: 520px; margin: 40px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 24px rgba(0,0,0,0.08); }
        .header { background: linear-gradient(135deg, #B80035, #D92C55); padding: 32px 40px; text-align: center; }
        .header h1 { margin: 0; color: #fff; font-size: 24px; font-weight: 700; letter-spacing: -0.5px; }
        .header p  { margin: 6px 0 0; color: rgba(255,255,255,0.75); font-size: 14px; }
        .body { padding: 36px 40px; }
        .body p { margin: 0 0 16px; color: #4b5563; font-size: 15px; line-height: 1.6; }
        .otp-box { background: #f9fafb; border: 2px solid #e5e7eb; border-radius: 12px; padding: 24px; text-align: center; margin: 28px 0; }
        .otp-code { font-size: 42px; font-weight: 800; letter-spacing: 14px; color: #D92C55; font-variant-numeric: tabular-nums; }
        .otp-label { font-size: 12px; color: #9ca3af; margin-top: 8px; text-transform: uppercase; letter-spacing: 1px; }
        .info-row { background: #fffbeb; border: 1px solid #fde68a; border-radius: 10px; padding: 14px 18px; display: flex; align-items: flex-start; gap: 10px; margin-bottom: 20px; }
        .info-row .icon { font-size: 18px; line-height: 1; }
        .info-row p { margin: 0; color: #92400e; font-size: 13px; line-height: 1.5; }
        .details { border-top: 1px solid #f3f4f6; padding-top: 20px; }
        .details dl { margin: 0; display: grid; grid-template-columns: 1fr 1fr; gap: 8px 16px; }
        .details dt { font-size: 11px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.5px; }
        .details dd { font-size: 13px; color: #1f2937; font-weight: 600; margin: 0 0 4px; }
        .footer { background: #f9fafb; border-top: 1px solid #f3f4f6; padding: 20px 40px; text-align: center; }
        .footer p { margin: 0; color: #9ca3af; font-size: 12px; line-height: 1.6; }
        .footer a { color: #D92C55; text-decoration: none; }
    </style>
</head>
<body>
<div class="wrapper">
    <div class="header">
        <h1>SCpay</h1>
        <p>Banque Digitale Madagascar</p>
    </div>

    <div class="body">
        <p>Bonjour,</p>
        <p>Vous avez initié un virement sécurisé. Voici votre code de vérification à usage unique :</p>

        <div class="otp-box">
            <div class="otp-code">{{ $otp }}</div>
            <div class="otp-label">Valable 10 minutes</div>
        </div>

        <div class="info-row">
            <span class="icon">⚠️</span>
            <p>Ne communiquez jamais ce code à personne. SCpay ne vous demandera jamais votre code OTP par téléphone ou email.</p>
        </div>

        <div class="details">
            <dl>
                <dt>Référence</dt>
                <dd>{{ $transfer->reference }}</dd>

                <dt>Montant</dt>
                <dd>{{ number_format((float) $transfer->amount, 0, ',', ' ') }} MGA</dd>

                <dt>Date</dt>
                <dd>{{ now()->format('d/m/Y H:i') }}</dd>

                <dt>Expire dans</dt>
                <dd>10 minutes</dd>
            </dl>
        </div>
    </div>

    <div class="footer">
        <p>
            Si vous n'êtes pas à l'origine de cette demande, ignorez cet email et
            <a href="mailto:support@scpay.mg">contactez notre support</a> immédiatement.
        </p>
        <p style="margin-top:8px;">© {{ date('Y') }} SCpay — Tous droits réservés</p>
    </div>
</div>
</body>
</html>
