<?php

namespace App\Repositories;

use App\Models\Transaction;
use Illuminate\Contracts\Pagination\LengthAwarePaginator;
use Illuminate\Database\Eloquent\Collection;

class TransactionRepository
{
    /**
     * Get paginated transactions for an account with optional filters.
     */
    public function getForAccount(
        int $accountId,
        array $filters = [],
        int $perPage = 15
    ): LengthAwarePaginator {
        $query = Transaction::where('account_id', $accountId)
            ->orderBy('created_at', 'desc');

        // Filter by transaction type (credit/debit)
        if (!empty($filters['type'])) {
            $query->where('type', $filters['type']);
        }

        // Filter by category
        if (!empty($filters['category'])) {
            $query->where('category', $filters['category']);
        }

        // Filter by date range
        if (!empty($filters['date_from'])) {
            $query->where('created_at', '>=', $filters['date_from']);
        }

        if (!empty($filters['date_to'])) {
            $query->where('created_at', '<=', $filters['date_to'] . ' 23:59:59');
        }

        // Filter by minimum amount
        if (!empty($filters['amount_min'])) {
            $query->where('amount', '>=', $filters['amount_min']);
        }

        // Filter by maximum amount
        if (!empty($filters['amount_max'])) {
            $query->where('amount', '<=', $filters['amount_max']);
        }

        // Search in description or reference
        if (!empty($filters['search'])) {
            $search = $filters['search'];
            $query->where(function ($q) use ($search) {
                $q->where('description', 'like', "%{$search}%")
                  ->orWhere('reference', 'like', "%{$search}%");
            });
        }

        return $query->paginate($perPage);
    }

    /**
     * Get recent transactions across all accounts (for admin).
     */
    public function getRecent(int $limit = 10): Collection
    {
        return Transaction::with('account.user')
            ->orderBy('created_at', 'desc')
            ->limit($limit)
            ->get();
    }

    /**
     * Get total transaction count.
     */
    public function getCount(): int
    {
        return Transaction::count();
    }

    /**
     * Get total transaction volume.
     */
    public function getTotalVolume(): float
    {
        return (float) Transaction::sum('amount');
    }

    /**
     * Get transactions flagged as suspicious (large amounts or unusual patterns).
     * This is a placeholder for future fraud detection integration.
     */
    public function getSuspiciousTransactions(int $limit = 10): Collection
    {
        // Flag transactions over 10,000,000 MGA or multiple rapid transactions
        return Transaction::with('account.user')
            ->where('amount', '>', 10000000)
            ->orderBy('created_at', 'desc')
            ->limit($limit)
            ->get();
    }

    /**
     * Create a new transaction.
     */
    public function create(array $data): Transaction
    {
        return Transaction::create($data);
    }

    /**
     * Get transaction by reference.
     */
    public function findByReference(string $reference): ?Transaction
    {
        return Transaction::where('reference', $reference)->first();
    }
}
