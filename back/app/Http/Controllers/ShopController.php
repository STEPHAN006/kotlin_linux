<?php

namespace App\Http\Controllers;

use App\Models\Card;
use App\Models\CardPayment;
use App\Models\UserNotification;
use Illuminate\Http\Request;
use Illuminate\Support\Str;

class ShopController extends Controller
{
    private static array $products = [
        ['id' => 1, 'name' => 'SCpay Premium 1 mois', 'description' => 'Accès illimité aux virements, QR, et crypto', 'price' => 29900,  'category' => 'subscription', 'image' => 'shield'],
        ['id' => 2, 'name' => 'Casque Sony WH-1000XM5', 'description' => 'Réduction de bruit active, 30h d\'autonomie', 'price' => 850000, 'category' => 'electronics',  'image' => 'headphones'],
        ['id' => 3, 'name' => 'Nike Air Max 270',        'description' => 'Pointure 42 — Noir/Rouge',                        'price' => 420000, 'category' => 'fashion',     'image' => 'sprint'],
        ['id' => 4, 'name' => 'Pack data 10 Go',         'description' => 'Validité 30 jours, réseau national',              'price' => 15000,  'category' => 'telecom',     'image' => 'cell_tower'],
    ];

    public function index()
    {
        return view('shop.index', ['products' => self::$products]);
    }

    public function checkout(Request $request)
    {
        $validated = $request->validate([
            'product_id'  => 'required|integer|in:1,2,3,4',
            'card_number' => 'required|string|min:13|max:19',
            'expiry'      => ['required', 'regex:/^\d{2}\/\d{2,4}$/'],
            'cvv'         => 'required|digits_between:3,4',
        ]);

        $product = collect(self::$products)->firstWhere('id', (int) $validated['product_id']);

        // Normalize card number (strip spaces/dashes)
        $rawNumber = preg_replace('/[\s\-]/', '', $validated['card_number']);

        // Find card by last 4 digits and CVV (decrypted comparison via Eloquent cast)
        $card = Card::with('account.user')
            ->where('is_blocked', false)
            ->get()
            ->first(function ($c) use ($rawNumber, $validated) {
                $storedLast4 = substr($c->card_number, -4);
                $inputLast4  = substr($rawNumber, -4);
                if ($storedLast4 !== $inputLast4) return false;
                if ($c->cvv !== $validated['cvv']) return false;

                // Check expiry mm/yy or mm/yyyy
                [$inputMonth, $inputYear] = explode('/', $validated['expiry']);
                if (strlen($inputYear) === 2) $inputYear = '20' . $inputYear;
                $cardExpiry = $c->expiry_date;
                return $cardExpiry
                    && (int)$cardExpiry->format('m') === (int)$inputMonth
                    && (int)$cardExpiry->format('Y') === (int)$inputYear;
            });

        if (! $card) {
            return back()
                ->withInput()
                ->withErrors(['card_number' => 'Carte introuvable, bloquée ou informations incorrectes.'])
                ->with('product_id', $validated['product_id']);
        }

        if ($card->expiry_date->isPast()) {
            return back()
                ->withInput()
                ->withErrors(['expiry' => 'Cette carte est expirée.'])
                ->with('product_id', $validated['product_id']);
        }

        if ((float) $product['price'] > (float) $card->daily_limit) {
            return back()
                ->withInput()
                ->withErrors(['card_number' => 'Montant supérieur à la limite journalière de la carte.'])
                ->with('product_id', $validated['product_id']);
        }

        $account = $card->account;
        if (! $account || (float) $account->balance < (float) $product['price']) {
            return back()
                ->withInput()
                ->withErrors(['card_number' => 'Solde insuffisant sur le compte associé.'])
                ->with('product_id', $validated['product_id']);
        }

        // Create pending payment (valid 5 min)
        $payment = CardPayment::create([
            'card_id'    => $card->id,
            'account_id' => $account->id,
            'reference'  => 'CPY-' . strtoupper(Str::random(8)),
            'merchant'   => 'SCpay Shop',
            'product'    => $product['name'],
            'amount'     => $product['price'],
            'status'     => 'pending',
            'expires_at' => now()->addMinutes(5),
        ]);

        // Push notification to the cardholder's app
        UserNotification::create([
            'user_id' => $account->user_id,
            'title'   => '💳 Confirmation de paiement requise',
            'body'    => json_encode([
                'type'      => 'card_payment',
                'reference' => $payment->reference,
                'product'   => $product['name'],
                'merchant'  => 'SCpay Shop',
                'amount'    => $product['price'],
            ]),
        ]);

        return redirect()->route('shop.waiting', $payment->reference);
    }

    public function waiting(string $reference)
    {
        $payment = CardPayment::where('reference', $reference)->firstOrFail();
        return view('shop.waiting', compact('payment'));
    }

    public function status(string $reference)
    {
        $payment = CardPayment::where('reference', $reference)->firstOrFail();

        // Auto-expire
        if ($payment->isExpired()) {
            $payment->update(['status' => 'expired']);
        }

        return response()->json([
            'status'  => $payment->status,
            'product' => $payment->product,
            'amount'  => $payment->amount,
        ]);
    }
}
