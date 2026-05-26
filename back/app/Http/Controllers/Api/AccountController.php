<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\AccountResource;
use App\Services\AccountService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AccountController extends Controller
{
    public function __construct(
        protected AccountService $accountService
    ) {}

    /**
     * Get the authenticated user's balance summary.
     *
     * GET /api/balance
     */
    public function balance(Request $request): JsonResponse
    {
        $balanceData = $this->accountService->getBalance($request->user()->id);

        return response()->json([
            'success' => true,
            'data' => [
                'accounts' => AccountResource::collection($balanceData['accounts']),
                'total_balance' => $balanceData['total_balance'],
                'formatted_total_balance' => number_format($balanceData['total_balance'], 2, ',', ' ') . ' ' . $balanceData['currency'],
                'currency' => $balanceData['currency'],
            ],
        ]);
    }

    /**
     * Get all accounts for the authenticated user.
     *
     * GET /api/accounts
     */
    public function index(Request $request): JsonResponse
    {
        $accounts = $this->accountService->getUserAccounts($request->user()->id);

        return response()->json([
            'success' => true,
            'data' => AccountResource::collection($accounts),
        ]);
    }
}
