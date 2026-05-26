<?php

namespace App\Repositories;

use App\Models\Account;
use Illuminate\Database\Eloquent\Collection;

class AccountRepository
{
    /**
     * Get all accounts for a user.
     */
    public function getByUserId(int $userId): Collection
    {
        return Account::where('user_id', $userId)
            ->with('cards')
            ->get();
    }

    /**
     * Get a specific account by ID for a user.
     */
    public function findByIdForUser(int $accountId, int $userId): ?Account
    {
        return Account::where('id', $accountId)
            ->where('user_id', $userId)
            ->first();
    }

    /**
     * Get the primary (first active) account for a user.
     */
    public function getPrimaryAccount(int $userId): ?Account
    {
        return Account::where('user_id', $userId)
            ->where('status', 'active')
            ->first();
    }

    /**
     * Get all active accounts.
     */
    public function getAllActive(): Collection
    {
        return Account::where('status', 'active')->get();
    }

    /**
     * Get total balance across all accounts.
     */
    public function getTotalBalance(): float
    {
        return (float) Account::where('status', 'active')->sum('balance');
    }

    /**
     * Get total number of accounts.
     */
    public function getCount(): int
    {
        return Account::count();
    }

    /**
     * Create a new account.
     */
    public function create(array $data): Account
    {
        return Account::create($data);
    }

    /**
     * Update an account.
     */
    public function update(Account $account, array $data): Account
    {
        $account->update($data);
        return $account->fresh();
    }
}
