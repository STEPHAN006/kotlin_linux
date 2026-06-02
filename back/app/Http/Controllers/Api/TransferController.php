<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\TransferResource;
use App\Models\Transfer;
use App\Services\AuditService;
use App\Services\TransferService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

class TransferController extends Controller
{
    public function __construct(
        protected TransferService $transferService,
        protected AuditService $auditService
    ) {}

    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'sender_account_id' => ['required', 'integer', 'exists:accounts,id'],
            'receiver_account_id' => ['required', 'integer', 'exists:accounts,id'],
            'amount' => ['required', 'numeric', 'min:100'],
            'note' => ['nullable', 'string', 'max:255'],
            'channel' => ['nullable', Rule::in(['internal', 'mvola', 'airtel_money', 'orange_money', 'bank_transfer'])],
        ]);

        $result = $this->transferService->initiate($validated, $request->user()->id);
        $this->auditService->log($request->user()->id, 'transfer.initiated', [
            'reference' => $result['transfer']->reference,
            'otp_required' => $result['otp_required'],
        ], $result['otp_required'] ? 'warning' : 'info', $request, 201);

        return response()->json([
            'success' => true,
            'message' => $result['message'],
            'data' => [
                'otp_required' => $result['otp_required'],
                'transfer' => new TransferResource($result['transfer']->loadMissing('senderAccount', 'receiverAccount')),
            ],
        ], 201);
    }

    public function verify(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'reference' => ['required', 'string'],
            'otp' => ['required', 'digits:6'],
        ]);

        $transfer = $this->transferService->verify($validated['reference'], $validated['otp'], $request->user()->id);

        return response()->json([
            'success' => true,
            'message' => 'OTP valide. Virement execute.',
            'data' => new TransferResource($transfer),
        ]);
    }

    public function index(Request $request): JsonResponse
    {
        $accountIds = $request->user()->accounts()->pluck('id');
        $transfers = Transfer::with(['senderAccount', 'receiverAccount'])
            ->whereIn('sender_account_id', $accountIds)
            ->orWhereIn('receiver_account_id', $accountIds)
            ->latest()
            ->paginate((int) $request->get('per_page', 15));

        return response()->json([
            'success' => true,
            'data' => TransferResource::collection($transfers),
        ]);
    }
}
