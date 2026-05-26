<?php

namespace App\Services;

use App\Repositories\AccountRepository;
use App\Repositories\TransactionRepository;
use App\Models\User;
use Illuminate\Database\Eloquent\Collection;

class AdminService
{
    public function __construct(
        protected AccountRepository $accountRepository,
        protected TransactionRepository $transactionRepository
    ) {}

    /**
     * Get the admin dashboard summary statistics.
     */
    public function getDashboardStats(): array
    {
        return [
            'total_users' => User::count(),
            'total_accounts' => $this->accountRepository->getCount(),
            'total_transactions' => $this->transactionRepository->getCount(),
            'total_balance' => $this->accountRepository->getTotalBalance(),
            'total_volume' => $this->transactionRepository->getTotalVolume(),
            'active_accounts' => User::where('is_active', true)->count(),
            'currency' => 'MGA',
        ];
    }

    /**
     * Get recent transactions for admin overview.
     */
    public function getRecentTransactions(int $limit = 20): Collection
    {
        return $this->transactionRepository->getRecent($limit);
    }

    /**
     * Get fraud alert data.
     * This is a placeholder that returns suspicious transactions.
     * Future: integrate with ML-based fraud detection, real-time monitoring,
     * and mobile money transaction pattern analysis.
     */
    public function getFraudAlerts(int $limit = 10): array
    {
        $suspiciousTransactions = $this->transactionRepository->getSuspiciousTransactions($limit);

        return [
            'alert_count' => $suspiciousTransactions->count(),
            'alerts' => $suspiciousTransactions->map(function ($txn) {
                return [
                    'id' => $txn->id,
                    'type' => 'high_value_transaction',
                    'severity' => $txn->amount > 50000000 ? 'critical' : 'warning',
                    'transaction_reference' => $txn->reference,
                    'amount' => (float) $txn->amount,
                    'account_holder' => $txn->account?->user?->name ?? 'Unknown',
                    'description' => "High value {$txn->type} of " . number_format($txn->amount, 2) . " MGA",
                    'timestamp' => $txn->created_at->toISOString(),
                ];
            }),
            'system_status' => 'monitoring',
            'last_scan' => now()->toISOString(),
            // Placeholder for future integrations
            'upcoming_features' => [
                'ml_fraud_detection',
                'real_time_monitoring',
                'mobile_money_pattern_analysis',
                'biometric_anomaly_detection',
                'geolocation_verification',
            ],
        ];
    }
}
