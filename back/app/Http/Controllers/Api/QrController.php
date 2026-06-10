<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\TransferResource;
use App\Models\Account;
use App\Services\TransferService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class QrController extends Controller
{
    public function __construct(protected TransferService $transferService) {}

    public function generate(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'account_id' => ['required', 'integer', 'exists:accounts,id'],
            'amount' => ['nullable', 'numeric', 'min:100'],
        ]);

        $account = $request->user()->accounts()->findOrFail($validated['account_id']);
        $payload = [
            'type' => 'bank_payment',
            'account_id' => $account->id,
            'amount' => $validated['amount'] ?? null,
            'currency' => $account->currency,
            'nonce' => bin2hex(random_bytes(8)),
        ];

        return response()->json([
            'success' => true,
            'data' => [
                'payload' => base64_encode(json_encode($payload)),
                'display' => $payload,
            ],
        ]);
    }

    public function scan(Request $request): JsonResponse
    {
        $validated = $request->validate(['payload' => ['required', 'string']]);
        $decoded = $this->decodePayload($validated['payload']);

        abort_if(($decoded['type'] ?? null) !== 'bank_payment', 422, 'QR code invalide.');

        $account = Account::with('user')->findOrFail((int) $decoded['account_id']);
        $masked  = '•••• ' . substr($account->account_number, -4);

        return response()->json([
            'success' => true,
            'message' => 'QR code valide.',
            'data' => [
                'payload'          => $validated['payload'],
                'recipient_name'   => $account->user->name,
                'account_masked'   => $masked,
                'suggested_amount' => $decoded['amount'] ?? null,
                'currency'         => $decoded['currency'] ?? 'MGA',
            ],
        ]);
    }

    public function pay(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'sender_account_id' => ['required', 'integer', 'exists:accounts,id'],
            'payload' => ['required', 'string'],
            'amount' => ['required', 'numeric', 'min:100'],
        ]);

        $decoded = $this->decodePayload($validated['payload']);
        abort_if(($decoded['type'] ?? null) !== 'bank_payment', 422, 'QR code invalide.');

        $result = $this->transferService->initiate([
            'sender_account_id' => $validated['sender_account_id'],
            'receiver_account_id' => (int) $decoded['account_id'],
            'amount' => (float) $validated['amount'],
            'note' => 'Paiement QR SCpay',
            'channel' => 'internal',
        ], $request->user()->id);

        return response()->json([
            'success' => true,
            'message' => $result['message'],
            'data' => [
                'otp_required' => $result['otp_required'],
                'transfer' => new TransferResource($result['transfer']->loadMissing('senderAccount', 'receiverAccount')),
            ],
        ], 201);
    }

    private function decodePayload(string $payload): array
    {
        // Normalise Base64: support URL-safe variant and strip whitespace
        $normalized = str_replace(['-', '_', ' ', "\n", "\r"], ['+', '/', '', '', ''], trim($payload));
        // Add padding if needed
        $padded = str_pad($normalized, strlen($normalized) + (4 - strlen($normalized) % 4) % 4, '=');

        $decoded = json_decode(base64_decode($padded), true);

        if (!is_array($decoded) || empty($decoded['account_id'])) {
            abort(422, 'QR code invalide ou expiré. Veuillez régénérer le code.');
        }

        return $decoded;
    }
}
