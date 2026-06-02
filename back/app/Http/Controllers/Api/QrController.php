<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\TransferResource;
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

        return response()->json([
            'success' => true,
            'message' => 'QR code valide. Confirmez avec votre PIN dans l application.',
            'data' => $decoded,
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
        $decoded = json_decode(base64_decode($payload), true);
        abort_if(!is_array($decoded) || empty($decoded['account_id']), 422, 'QR code invalide.');

        return $decoded;
    }
}
