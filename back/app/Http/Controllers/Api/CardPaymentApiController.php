<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\CardPayment;
use App\Models\Transaction;
use App\Models\UserNotification;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class CardPaymentApiController extends Controller
{
    public function pending(Request $request): JsonResponse
    {
        $userId = $request->user()->id;

        // Auto-expire stale payments first
        CardPayment::whereHas('account', fn($q) => $q->where('user_id', $userId))
            ->where('status', 'pending')
            ->where('expires_at', '<', now())
            ->update(['status' => 'expired']);

        $payments = CardPayment::with('card')
            ->whereHas('account', fn($q) => $q->where('user_id', $userId))
            ->where('status', 'pending')
            ->orderByDesc('created_at')
            ->get()
            ->map(fn($p) => [
                'reference'  => $p->reference,
                'merchant'   => $p->merchant,
                'product'    => $p->product,
                'amount'     => (float) $p->amount,
                'card_masked'=> $p->card?->masked_number,
                'expires_at' => $p->expires_at->toISOString(),
            ]);

        return response()->json(['success' => true, 'data' => $payments]);
    }

    public function confirm(Request $request, string $reference): JsonResponse
    {
        $payment = CardPayment::with('account.user', 'card')
            ->where('reference', $reference)
            ->where('status', 'pending')
            ->firstOrFail();

        // Ownership check
        abort_unless($payment->account->user_id === $request->user()->id, 403);

        if ($payment->isExpired()) {
            $payment->update(['status' => 'expired']);
            return response()->json(['success' => false, 'message' => 'Paiement expiré.'], 422);
        }

        DB::transaction(function () use ($payment) {
            $account = $payment->account;

            if ((float) $account->balance < (float) $payment->amount) {
                abort(422, 'Solde insuffisant.');
            }

            $newBalance = (float) $account->balance - (float) $payment->amount;
            $account->update(['balance' => $newBalance]);

            Transaction::create([
                'account_id'    => $account->id,
                'type'          => 'debit',
                'amount'        => $payment->amount,
                'category'      => 'ecommerce',
                'description'   => $payment->merchant . ' — ' . $payment->product,
                'reference'     => 'TXN-' . strtoupper(substr(md5($payment->reference), 0, 10)),
                'balance_after' => $newBalance,
                'metadata'      => ['card_payment_ref' => $payment->reference],
            ]);

            $payment->update([
                'status'       => 'approved',
                'confirmed_at' => now(),
            ]);

            UserNotification::create([
                'user_id' => $account->user_id,
                'title'   => '✅ Paiement confirmé',
                'body'    => number_format((float)$payment->amount, 0, ',', ' ') . ' MGA débités — ' . $payment->product,
            ]);
        });

        return response()->json(['success' => true, 'message' => 'Paiement confirmé.']);
    }

    public function decline(Request $request, string $reference): JsonResponse
    {
        $payment = CardPayment::with('account')
            ->where('reference', $reference)
            ->where('status', 'pending')
            ->firstOrFail();

        abort_unless($payment->account->user_id === $request->user()->id, 403);

        $payment->update(['status' => 'declined', 'confirmed_at' => now()]);

        UserNotification::create([
            'user_id' => $payment->account->user_id,
            'title'   => '❌ Paiement refusé',
            'body'    => 'Vous avez refusé le paiement de ' . number_format((float)$payment->amount, 0, ',', ' ') . ' MGA — ' . $payment->product,
        ]);

        return response()->json(['success' => true, 'message' => 'Paiement refusé.']);
    }
}
