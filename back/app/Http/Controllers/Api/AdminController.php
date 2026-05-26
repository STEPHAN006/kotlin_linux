<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\TransactionResource;
use App\Services\AdminService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AdminController extends Controller
{
    public function __construct(
        protected AdminService $adminService
    ) {}

    /**
     * Get the admin dashboard summary.
     *
     * GET /api/admin/dashboard
     */
    public function dashboard(Request $request): JsonResponse
    {
        $stats = $this->adminService->getDashboardStats();
        $recentTransactions = $this->adminService->getRecentTransactions(10);

        return response()->json([
            'success' => true,
            'data' => [
                'statistics' => $stats,
                'recent_transactions' => TransactionResource::collection($recentTransactions),
            ],
        ]);
    }

    /**
     * Get all transactions for admin (with pagination).
     *
     * GET /api/admin/transactions
     */
    public function transactions(Request $request): JsonResponse
    {
        $limit = (int) $request->get('limit', 50);
        $transactions = $this->adminService->getRecentTransactions(min($limit, 200));

        return response()->json([
            'success' => true,
            'data' => TransactionResource::collection($transactions),
        ]);
    }

    /**
     * Get fraud alerts.
     *
     * GET /api/admin/fraud-alerts
     */
    public function fraudAlerts(Request $request): JsonResponse
    {
        $limit = (int) $request->get('limit', 10);
        $alerts = $this->adminService->getFraudAlerts(min($limit, 50));

        return response()->json([
            'success' => true,
            'data' => $alerts,
        ]);
    }
}
