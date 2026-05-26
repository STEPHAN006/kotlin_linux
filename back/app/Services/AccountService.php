<?php

namespace App\Services;

use App\Repositories\AccountRepository;
use App\Models\Account;
use Illuminate\Database\Eloquent\Collection;
use Illuminate\Support\Str;

class AccountService
{
    public function __construct(
        protected AccountRepository $accountRepository
    ) {}

    /**
     * Get all accounts for a user.
     */
    public function getUserAccounts(int $userId): Collection
    {
        return $this->accountRepository->getByUserId($userId);
    }

    /**
     * Get the primary account balance details.
     */
    public function getBalance(int $userId): array
    {
        $accounts = $this->accountRepository->getByUserId($userId);

        if ($accounts->isEmpty()) {
            return [
                'accounts' => [],
                'total_balance' => 0,
                'currency' => 'MGA',
            ];
        }

        $totalBalance = $accounts->where('status', 'active')->sum('balance');

        return [
            'accounts' => $accounts,
            'total_balance' => (float) $totalBalance,
            'currency' => $accounts->first()->currency,
        ];
    }

    /**
     * Generate a unique account number.
     * Format: MG + 14 digits (total 16 characters)
     */
    public static function generateAccountNumber(): string
    {
        do {
            $number = 'MG' . str_pad(mt_rand(1, 99999999999999), 14, '0', STR_PAD_LEFT);
        } while (Account::where('account_number', $number)->exists());

        return $number;
    }

    /**
     * Create a new account for a user.
     */
    public function createAccount(int $userId, array $data = []): Account
    {
        return $this->accountRepository->create([
            'user_id' => $userId,
            'account_number' => self::generateAccountNumber(),
            'balance' => $data['initial_balance'] ?? 0,
            'currency' => $data['currency'] ?? 'MGA',
            'status' => 'active',
            'type' => $data['type'] ?? 'checking',
        ]);
    }
}
