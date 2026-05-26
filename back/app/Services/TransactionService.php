<?php

namespace App\Services;

use App\Repositories\TransactionRepository;
use App\Repositories\AccountRepository;
use Illuminate\Contracts\Pagination\LengthAwarePaginator;

class TransactionService
{
    public function __construct(
        protected TransactionRepository $transactionRepository,
        protected AccountRepository $accountRepository
    ) {}

    /**
     * Get paginated transactions for a user's primary account with filters.
     */
    public function getUserTransactions(
        int $userId,
        array $filters = [],
        int $perPage = 15
    ): LengthAwarePaginator {
        $account = $this->accountRepository->getPrimaryAccount($userId);

        if (!$account) {
            // Return empty paginator if no account exists
            return new \Illuminate\Pagination\LengthAwarePaginator([], 0, $perPage);
        }

        return $this->transactionRepository->getForAccount(
            $account->id,
            $filters,
            $perPage
        );
    }

    /**
     * Get transactions for a specific account with filters.
     */
    public function getAccountTransactions(
        int $accountId,
        array $filters = [],
        int $perPage = 15
    ): LengthAwarePaginator {
        return $this->transactionRepository->getForAccount(
            $accountId,
            $filters,
            $perPage
        );
    }

    /**
     * Generate a unique transaction reference.
     * Format: TXN + timestamp + random (e.g., TXN20260526-ABCD1234)
     */
    public static function generateReference(): string
    {
        return 'TXN' . now()->format('Ymd') . '-' . strtoupper(substr(md5(uniqid(mt_rand(), true)), 0, 8));
    }
}
