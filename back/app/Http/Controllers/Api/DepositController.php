<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Account;
use App\Models\Deposit;
use App\Models\Transaction;
use App\Models\UserNotification;
use App\Services\AuditService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\Rule;

class DepositController extends Controller
{
    private const METHOD_LABELS = [
        'mvola'        => 'MVola',
        'orange_money' => 'Orange Money',
        'airtel_money' => 'Airtel Money',
        'cash'         => 'Espèces',
    ];

    public function __construct(protected AuditService $auditService) {}

    /**
     * Crée un dépôt en attente (sans créditer le compte).
     * POST /api/deposits
     */
    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'account_id' => ['required', 'integer', 'exists:accounts,id'],
            'amount'     => ['required', 'numeric', 'min:1000'],
            'method'     => ['required', Rule::in(['mvola', 'orange_money', 'airtel_money', 'cash'])],
            'phone'      => ['required_unless:method,cash', 'nullable', 'string', 'max:20'],
        ]);

        $account = Account::where('user_id', $request->user()->id)
            ->findOrFail($validated['account_id']);

        if (!$account->isActive()) {
            return response()->json(['success' => false, 'message' => 'Compte inactif ou bloqué.'], 422);
        }

        $deposit = Deposit::create([
            'account_id' => $account->id,
            'amount'     => (float) $validated['amount'],
            'method'     => $validated['method'],
            'phone'      => $validated['phone'] ?? null,
            'reference'  => $this->generateReference(),
            'status'     => 'pending',
            'metadata'   => ['initiated_at' => now()->toISOString()],
        ]);

        $this->auditService->log($request->user()->id, 'deposit.initiated', [
            'reference'  => $deposit->reference,
            'amount'     => $deposit->amount,
            'method'     => $deposit->method,
        ], 'info', $request, 201);

        return response()->json([
            'success' => true,
            'message' => 'Dépôt en attente de paiement.',
            'data'    => [
                'reference' => $deposit->reference,
                'amount'    => $deposit->amount,
                'method'    => $deposit->method,
                'status'    => 'pending',
            ],
        ], 201);
    }

    /**
     * Confirme le dépôt : crédite le compte.
     * POST /api/deposits/{reference}/confirm
     */
    public function confirm(Request $request, string $reference): JsonResponse
    {
        $deposit = Deposit::where('reference', $reference)
            ->whereHas('account', fn ($q) => $q->where('user_id', $request->user()->id))
            ->firstOrFail();

        if ($deposit->status !== 'pending') {
            return response()->json(['success' => false, 'message' => 'Ce dépôt ne peut plus être confirmé.'], 422);
        }

        DB::transaction(function () use ($deposit) {
            $account = $deposit->account;
            $account->increment('balance', $deposit->amount);
            $account->refresh();

            $deposit->update([
                'status'        => 'completed',
                'balance_after' => $account->balance,
                'metadata'      => array_merge($deposit->metadata ?? [], [
                    'completed_at' => now()->toISOString(),
                ]),
            ]);

            Transaction::create([
                'account_id'    => $account->id,
                'type'          => 'credit',
                'amount'        => $deposit->amount,
                'category'      => $deposit->method === 'cash' ? 'deposit_cash' : 'mobile_money',
                'description'   => 'Dépôt via ' . self::METHOD_LABELS[$deposit->method],
                'reference'     => $deposit->reference,
                'balance_after' => $account->balance,
                'metadata'      => ['deposit_reference' => $deposit->reference],
            ]);

            $formatted = number_format($deposit->amount, 0, ',', ' ') . ' MGA';
            UserNotification::create([
                'user_id' => $account->user_id,
                'title'   => 'Dépôt reçu — ' . $formatted,
                'body'    => 'Votre dépôt de ' . $formatted . ' via ' . self::METHOD_LABELS[$deposit->method] . ' a été crédité. Réf: ' . $deposit->reference,
            ]);
        });

        $this->auditService->log($request->user()->id, 'deposit.completed', [
            'reference' => $deposit->reference,
            'amount'    => $deposit->amount,
        ], 'info', $request);

        return response()->json([
            'success' => true,
            'message' => 'Dépôt confirmé et crédité.',
            'data'    => [
                'reference'   => $deposit->reference,
                'amount'      => $deposit->amount,
                'method'      => $deposit->method,
                'status'      => 'completed',
                'new_balance' => $deposit->balance_after,
            ],
        ]);
    }

    /**
     * Annule un dépôt en attente.
     * POST /api/deposits/{reference}/cancel
     */
    public function cancel(Request $request, string $reference): JsonResponse
    {
        $deposit = Deposit::where('reference', $reference)
            ->whereHas('account', fn ($q) => $q->where('user_id', $request->user()->id))
            ->firstOrFail();

        if ($deposit->status !== 'pending') {
            return response()->json(['success' => false, 'message' => 'Ce dépôt ne peut plus être annulé.'], 422);
        }

        $deposit->update(['status' => 'cancelled']);

        $this->auditService->log($request->user()->id, 'deposit.cancelled', [
            'reference' => $deposit->reference,
        ], 'info', $request);

        return response()->json(['success' => true, 'message' => 'Dépôt annulé.']);
    }

    private function generateReference(): string
    {
        return 'DEP' . now()->format('Ymd') . '-' . strtoupper(substr(md5(uniqid('', true)), 0, 8));
    }
}
