<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\CryptoTransaction;
use App\Models\CryptoWallet;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\Rule;

class CryptoController extends Controller
{
    private const SUPPORTED = [
        'BTC'  => ['name' => 'Bitcoin',    'coin_id' => 'bitcoin'],
        'ETH'  => ['name' => 'Ethereum',   'coin_id' => 'ethereum'],
        'SOL'  => ['name' => 'Solana',     'coin_id' => 'solana'],
        'BNB'  => ['name' => 'BNB',        'coin_id' => 'binancecoin'],
        'USDT' => ['name' => 'Tether USD', 'coin_id' => 'tether'],
        'USDC' => ['name' => 'USD Coin',   'coin_id' => 'usd-coin'],
        'TON'  => ['name' => 'Toncoin',    'coin_id' => 'toncoin'],
        'S'    => ['name' => 'Sonic',      'coin_id' => 'sonic-3'],
    ];

    /** GET /api/crypto/wallets */
    public function wallets(Request $request): JsonResponse
    {
        $user    = $request->user();
        $wallets = CryptoWallet::where('user_id', $user->id)->get()->keyBy('symbol');

        $result = collect(self::SUPPORTED)->map(function ($info, $symbol) use ($wallets, $user) {
            $wallet = $wallets->get($symbol);

            if (!$wallet) {
                $wallet = CryptoWallet::create([
                    'user_id' => $user->id,
                    'symbol'  => $symbol,
                    'address' => self::generateAddress($symbol),
                    'balance' => 0,
                ]);
            }

            return [
                'symbol'  => $symbol,
                'name'    => $info['name'],
                'coin_id' => $info['coin_id'],
                'address' => $wallet->address,
                'balance' => (float) $wallet->balance,
            ];
        })->values();

        return response()->json(['success' => true, 'data' => $result]);
    }

    /** POST /api/crypto/buy */
    public function buy(Request $request): JsonResponse
    {
        $request->validate([
            'symbol'      => ['required', Rule::in(array_keys(self::SUPPORTED))],
            'amount_mga'  => ['required', 'numeric', 'min:1000'],
            'price_usd'   => ['required', 'numeric', 'min:0.000001'],
            'mga_per_usd' => ['required', 'numeric', 'min:1'],
        ]);

        $user    = $request->user();
        $account = $user->accounts()->where('status', 'active')->first();

        if (!$account) {
            return response()->json(['success' => false, 'message' => 'Aucun compte actif.'], 422);
        }

        $amountMga    = (float) $request->amount_mga;
        $priceUsd     = (float) $request->price_usd;
        $mgaPerUsd    = (float) $request->mga_per_usd;
        $priceMga     = $priceUsd * $mgaPerUsd;
        $cryptoAmount = $amountMga / $priceMga;

        if ((float) $account->balance < $amountMga) {
            return response()->json(['success' => false, 'message' => 'Solde MGA insuffisant.'], 422);
        }

        DB::transaction(function () use ($user, $account, $request, $amountMga, $priceUsd, $cryptoAmount) {
            $account->decrement('balance', $amountMga);

            $wallet = CryptoWallet::firstOrCreate(
                ['user_id' => $user->id, 'symbol' => $request->symbol],
                ['address' => self::generateAddress($request->symbol), 'balance' => 0]
            );
            $wallet->increment('balance', $cryptoAmount);

            CryptoTransaction::create([
                'user_id'   => $user->id,
                'type'      => 'buy',
                'symbol'    => $request->symbol,
                'amount'    => $cryptoAmount,
                'price_usd' => $priceUsd,
                'total_mga' => $amountMga,
                'tx_hash'   => '0x' . bin2hex(random_bytes(32)),
            ]);
        });

        return response()->json([
            'success' => true,
            'message' => "Achat {$request->symbol} effectué.",
            'data'    => [
                'symbol'       => $request->symbol,
                'crypto_amount' => round($cryptoAmount, 8),
                'total_mga'    => $amountMga,
            ],
        ]);
    }

    /** POST /api/crypto/sell */
    public function sell(Request $request): JsonResponse
    {
        $request->validate([
            'symbol'        => ['required', Rule::in(array_keys(self::SUPPORTED))],
            'crypto_amount' => ['required', 'numeric', 'min:0.000001'],
            'price_usd'     => ['required', 'numeric', 'min:0.000001'],
            'mga_per_usd'   => ['required', 'numeric', 'min:1'],
        ]);

        $user   = $request->user();
        $wallet = CryptoWallet::where('user_id', $user->id)
            ->where('symbol', $request->symbol)->first();

        $cryptoAmount = (float) $request->crypto_amount;

        if (!$wallet || (float) $wallet->balance < $cryptoAmount) {
            return response()->json(['success' => false, 'message' => 'Solde crypto insuffisant.'], 422);
        }

        $account = $user->accounts()->where('status', 'active')->first();
        if (!$account) {
            return response()->json(['success' => false, 'message' => 'Aucun compte actif.'], 422);
        }

        $priceUsd  = (float) $request->price_usd;
        $mgaPerUsd = (float) $request->mga_per_usd;
        $totalMga  = $cryptoAmount * $priceUsd * $mgaPerUsd;

        DB::transaction(function () use ($user, $account, $wallet, $request, $cryptoAmount, $priceUsd, $totalMga) {
            $wallet->decrement('balance', $cryptoAmount);
            $account->increment('balance', $totalMga);

            CryptoTransaction::create([
                'user_id'   => $user->id,
                'type'      => 'sell',
                'symbol'    => $request->symbol,
                'amount'    => $cryptoAmount,
                'price_usd' => $priceUsd,
                'total_mga' => $totalMga,
                'tx_hash'   => '0x' . bin2hex(random_bytes(32)),
            ]);
        });

        return response()->json([
            'success' => true,
            'message' => "Vente {$request->symbol} effectuée.",
            'data'    => [
                'symbol'        => $request->symbol,
                'crypto_amount' => $cryptoAmount,
                'total_mga'     => round($totalMga, 2),
            ],
        ]);
    }

    /** POST /api/crypto/send */
    public function send(Request $request): JsonResponse
    {
        $request->validate([
            'symbol'        => ['required', Rule::in(array_keys(self::SUPPORTED))],
            'crypto_amount' => ['required', 'numeric', 'min:0.000001'],
            'to_address'    => ['required', 'string', 'min:10', 'max:200'],
            'price_usd'     => ['required', 'numeric', 'min:0.000001'],
            'mga_per_usd'   => ['required', 'numeric', 'min:1'],
        ]);

        $user         = $request->user();
        $wallet       = CryptoWallet::where('user_id', $user->id)->where('symbol', $request->symbol)->first();
        $cryptoAmount = (float) $request->crypto_amount;

        if (!$wallet || (float) $wallet->balance < $cryptoAmount) {
            return response()->json(['success' => false, 'message' => 'Solde crypto insuffisant.'], 422);
        }

        $totalMga = $cryptoAmount * (float) $request->price_usd * (float) $request->mga_per_usd;
        $txHash   = '0x' . bin2hex(random_bytes(32));

        DB::transaction(function () use ($user, $wallet, $request, $cryptoAmount, $totalMga, $txHash) {
            $wallet->decrement('balance', $cryptoAmount);

            CryptoTransaction::create([
                'user_id'    => $user->id,
                'type'       => 'send',
                'symbol'     => $request->symbol,
                'amount'     => $cryptoAmount,
                'price_usd'  => (float) $request->price_usd,
                'total_mga'  => $totalMga,
                'to_address' => $request->to_address,
                'tx_hash'    => $txHash,
            ]);
        });

        return response()->json([
            'success' => true,
            'message' => "Envoi {$request->symbol} effectué.",
            'data'    => ['tx_hash' => $txHash],
        ]);
    }

    /** GET /api/crypto/transactions */
    public function transactions(Request $request): JsonResponse
    {
        $txns = CryptoTransaction::where('user_id', $request->user()->id)
            ->latest()
            ->paginate(30);

        return response()->json(['success' => true, 'data' => $txns]);
    }

    private static function generateAddress(string $symbol): string
    {
        return match ($symbol) {
            'BTC'             => 'bc1q' . substr(bin2hex(random_bytes(20)), 0, 38),
            'ETH', 'USDT', 'USDC' => '0x' . bin2hex(random_bytes(20)),
            'BNB'             => '0x' . bin2hex(random_bytes(20)),
            'SOL'             => rtrim(strtr(base64_encode(random_bytes(32)), '+/', 'AB'), '='),
            'TON'             => 'UQ' . substr(rtrim(strtr(base64_encode(random_bytes(34)), '+/', '-_'), '='), 0, 46),
            default           => '0x' . bin2hex(random_bytes(20)),
        };
    }
}
