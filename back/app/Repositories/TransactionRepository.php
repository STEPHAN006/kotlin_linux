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
     * Get transactions flagged as suspicious:
     * – Large amounts (> 10 000 000 MGA)
     * – Same amount repeated ≥ 3 times for the same account in the last 24 h
     * – Transactions made between midnight and 05:00 (atypical hours)
     */
    public function getSuspiciousTransactions(int $limit = 10): Collection
    {
        $largeAmount = Transaction::with('account.user')
            ->where('amount', '>', 10000000)
            ->orderBy('created_at', 'desc')
            ->limit($limit)
            ->get();

        // Same amount repeated ≥ 3 times per account in last 24 h
        $repeatedIds = Transaction::selectRaw('id')
            ->whereIn('id', function ($sub) {
                $sub->selectRaw('MIN(id)')
                    ->from('transactions')
                    ->where('created_at', '>=', now()->subHours(24))
                    ->groupBy('account_id', 'amount')
                    ->havingRaw('COUNT(*) >= 3');
            })
            ->pluck('id');

        $repeated = Transaction::with('account.user')
            ->whereIn('id', $repeatedIds)
            ->orderBy('created_at', 'desc')
            ->limit($limit)
            ->get();

        // Off-hours transactions (00:00 – 05:00) — compatible SQLite + MySQL
        $isMySQL = config('database.default') !== 'sqlite';
        $offHours = Transaction::with('account.user')
            ->when($isMySQL, fn($q) => $q->whereRaw('HOUR(created_at) < 5'),
                             fn($q) => $q->whereRaw("strftime('%H', created_at) < '05'"))
            ->orderBy('created_at', 'desc')
            ->limit($limit)
            ->get();

        return $largeAmount->merge($repeated)->merge($offHours)
            ->unique('id')
            ->sortByDesc('created_at')
            ->take($limit)
            ->values();
    }

    /**
     * Detect if an account has suspicious activity and return true if it should be frozen.
     * Triggers when: ≥ 5 debits in 10 minutes OR ≥ 3 identical amounts in 1 hour.
     */
    public function isSuspicious(int $accountId): bool
    {
        $rapidDebits = Transaction::where('account_id', $accountId)
            ->where('type', 'debit')
            ->where('created_at', '>=', now()->subMinutes(10))
            ->count();

        if ($rapidDebits >= 5) {
            return true;
        }

        $repeatedAmount = Transaction::where('account_id', $accountId)
            ->where('created_at', '>=', now()->subHour())
            ->groupBy('amount')
            ->havingRaw('COUNT(*) >= 3')
            ->count();

        return $repeatedAmount > 0;
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
