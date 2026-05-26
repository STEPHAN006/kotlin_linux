<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\TransactionFilterRequest;
use App\Http\Resources\TransactionResource;
use App\Services\TransactionService;
use Illuminate\Http\JsonResponse;

class TransactionController extends Controller
{
    public function __construct(
        protected TransactionService $transactionService
    ) {}

    /**
     * Get paginated transactions for the authenticated user.
     * Supports filtering by type, category, date range, amount range, and search.
     *
     * GET /api/transactions
     *
     * Query parameters:
     * - type: credit|debit
     * - category: string
     * - date_from: Y-m-d
     * - date_to: Y-m-d
     * - amount_min: numeric
     * - amount_max: numeric
     * - search: string (searches description and reference)
     * - per_page: integer (1-100, default 15)
     * - account_id: integer (specific account, defaults to primary)
     */
    public function index(TransactionFilterRequest $request): JsonResponse
    {
        $filters = $request->validated();
        $perPage = (int) ($filters['per_page'] ?? 15);

        // If a specific account_id is provided, use it; otherwise use primary account
        if (!empty($filters['account_id'])) {
            // Verify the account belongs to the authenticated user
            $account = $request->user()->accounts()->find($filters['account_id']);
            if (!$account) {
                return response()->json([
                    'success' => false,
                    'message' => 'Account not found or does not belong to you.',
                ], 403);
            }
            $transactions = $this->transactionService->getAccountTransactions(
                $filters['account_id'],
                $filters,
                $perPage
            );
        } else {
            $transactions = $this->transactionService->getUserTransactions(
                $request->user()->id,
                $filters,
                $perPage
            );
        }

        return response()->json([
            'success' => true,
            'data' => TransactionResource::collection($transactions),
            'meta' => [
                'current_page' => $transactions->currentPage(),
                'last_page' => $transactions->lastPage(),
                'per_page' => $transactions->perPage(),
                'total' => $transactions->total(),
            ],
        ]);
    }
}
