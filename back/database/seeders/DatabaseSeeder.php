<?php

namespace Database\Seeders;

use App\Models\Account;
use App\Models\Beneficiary;
use App\Models\Card;
use App\Models\CryptoWallet;
use App\Models\Transaction;
use App\Models\User;
use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class DatabaseSeeder extends Seeder
{
    use WithoutModelEvents;

    /**
     * Seed the application's database with realistic banking data.
     */
    public function run(): void
    {
        $this->command->info('🏦 Seeding Banking Application Database...');

        // ================================================================
        // 1. Create Admin User
        // ================================================================
        $admin = User::create([
            'name' => 'Admin BankingApp',
            'email' => 'admin@bankingapp.mg',
            'phone' => '+261340000000',
            'password' => 'Admin@2026',
            'role' => 'admin',
            'is_active' => true,
            'email_verified_at' => now(),
        ]);
        $this->command->info('✅ Admin user created: admin@bankingapp.mg / Admin@2026');

        // ================================================================
        // 2. Create 3 Demo Users with Bank Accounts
        // ================================================================
        $demoUsers = [
            [
                'name' => 'Rakoto Jean',
                'email' => 'rakoto@example.com',
                'phone' => '+261341111111',
                'balance' => 4850000.00,
                'type' => 'checking',
            ],
            [
                'name' => 'Rasoa Marie',
                'email' => 'rasoa@example.com',
                'phone' => '+261342222222',
                'balance' => 12750000.00,
                'type' => 'savings',
            ],
            [
                'name' => 'Rabe Paul',
                'email' => 'rabe@example.com',
                'phone' => '+261343333333',
                'balance' => 1920000.00,
                'type' => 'checking',
            ],
        ];

        $accounts = [];
        foreach ($demoUsers as $userData) {
            $user = User::create([
                'name' => $userData['name'],
                'email' => $userData['email'],
                'phone' => $userData['phone'],
                'password' => 'Password@123',
                'role' => 'user',
                'is_active' => true,
                'email_verified_at' => now(),
            ]);

            $account = Account::create([
                'user_id' => $user->id,
                'account_number' => 'MG' . str_pad(mt_rand(10000000000000, 99999999999999), 14, '0', STR_PAD_LEFT),
                'balance' => $userData['balance'],
                'currency' => 'MGA',
                'status' => 'active',
                'type' => $userData['type'],
            ]);

            $accounts[] = $account;

            // Create a card for each account
            Card::create([
                'account_id' => $account->id,
                'card_number' => '4' . str_pad(mt_rand(100000000000000, 999999999999999), 15, '0', STR_PAD_LEFT),
                'cvv' => str_pad(mt_rand(100, 999), 3, '0', STR_PAD_LEFT),
                'expiry_date' => now()->addYears(3)->format('Y-m-d'),
                'is_blocked' => false,
                'type' => 'visa',
                'daily_limit' => 5000000.00,
            ]);

            // Create beneficiaries for each user
            Beneficiary::create([
                'user_id' => $user->id,
                'name' => fake()->name(),
                'bank_name' => fake()->randomElement(['BNI Madagascar', 'BOA Madagascar', 'BFV-SG', 'Access Banque', 'BMOI']),
                'account_number' => 'MG' . str_pad(mt_rand(10000000000000, 99999999999999), 14, '0', STR_PAD_LEFT),
                'phone' => '+26134' . str_pad(mt_rand(1000000, 9999999), 7, '0', STR_PAD_LEFT),
                'channel' => fake()->randomElement(['bank', 'mvola', 'airtel_money']),
                'is_verified' => true,
            ]);

            $this->command->info("✅ User created: {$userData['email']} / Password@123 (Balance: " . number_format($userData['balance'], 2) . " MGA)");
        }

        // ================================================================
        // 3. Create Admin Account
        // ================================================================
        $adminAccount = Account::create([
            'user_id' => $admin->id,
            'account_number' => 'MG00000000000001',
            'balance' => 100000000.00,
            'currency' => 'MGA',
            'status' => 'active',
            'type' => 'business',
        ]);

        // ================================================================
        // 4. Create 50 Realistic Historical Transactions
        // ================================================================
        $this->command->info('📊 Generating 50 historical transactions...');

        $creditCategories = [
            'salary' => ['Monthly Salary Payment', 'Bi-weekly Salary', 'Overtime Payment', 'Performance Bonus'],
            'transfer_in' => ['Transfer from Savings', 'Incoming Transfer', 'Family Support', 'Freelance Payment'],
            'refund' => ['Purchase Refund - Shoprite', 'Service Refund', 'Overcharge Correction'],
            'interest' => ['Monthly Interest Credit', 'Savings Interest'],
            'mobile_money' => ['MVola Deposit', 'Airtel Money Received', 'Orange Money Deposit'],
        ];

        $debitCategories = [
            'groceries' => ['Shoprite Ankorondrano', 'Leader Price Analakely', 'Jumbo Score Tanjombato', 'Local Market Purchase'],
            'utilities' => ['JIRAMA Electricity Bill', 'JIRAMA Water Bill', 'Telma Internet', 'Orange Mobile Recharge'],
            'transport' => ['Shell Fuel Station', 'Taxi Be Fare', 'Cotisse Transport', 'Car Wash Service'],
            'dining' => ['Restaurant La Varangue', 'Café de la Gare', 'KFC Analakely', 'Gastro Pizza'],
            'shopping' => ['Galaxy.mg Electronics', 'Havana Clothing', 'Pharmaplus Pharmacy', 'Librairie de Madagascar'],
            'rent' => ['Monthly Rent - Ivandry', 'Office Rent Payment', 'Parking Subscription'],
            'education' => ['University of Antananarivo Fees', 'INSCAE Tuition', 'Online Course - Udemy'],
            'healthcare' => ['Clinic Espace Médical', 'Pharmacy Purchase', 'Lab Analysis - CERVO'],
            'transfer_out' => ['Transfer to Rabe', 'Bill Payment - JIRAMA', 'Loan Repayment - Access Banque'],
            'mobile_money' => ['MVola Transfer', 'Airtel Money Payment', 'Orange Money Withdrawal'],
        ];

        $transactionCount = 0;
        foreach ($accounts as $account) {
            // Generate ~17 transactions per account (total ~51)
            $numTransactions = $account === $accounts[0] ? 20 : ($account === $accounts[1] ? 18 : 12);
            $runningBalance = (float) $account->balance;

            // Generate transactions from oldest to newest
            $dates = collect();
            for ($i = 0; $i < $numTransactions; $i++) {
                $dates->push(fake()->dateTimeBetween('-6 months', 'now'));
            }
            $dates = $dates->sort()->values();

            // Work backwards to calculate balance_after correctly
            $transactions = [];
            for ($i = 0; $i < $numTransactions; $i++) {
                $isCredit = fake()->boolean(30); // 30% credits, 70% debits
                $type = $isCredit ? 'credit' : 'debit';

                if ($isCredit) {
                    $category = fake()->randomElement(array_keys($creditCategories));
                    $description = fake()->randomElement($creditCategories[$category]);
                    $amount = match ($category) {
                        'salary' => fake()->randomFloat(2, 800000, 4000000),
                        'transfer_in' => fake()->randomFloat(2, 50000, 2000000),
                        'refund' => fake()->randomFloat(2, 10000, 200000),
                        'interest' => fake()->randomFloat(2, 2000, 30000),
                        'mobile_money' => fake()->randomFloat(2, 20000, 500000),
                        default => fake()->randomFloat(2, 10000, 500000),
                    };
                } else {
                    $category = fake()->randomElement(array_keys($debitCategories));
                    $description = fake()->randomElement($debitCategories[$category]);
                    $amount = match ($category) {
                        'groceries' => fake()->randomFloat(2, 15000, 200000),
                        'utilities' => fake()->randomFloat(2, 25000, 120000),
                        'transport' => fake()->randomFloat(2, 5000, 60000),
                        'dining' => fake()->randomFloat(2, 12000, 80000),
                        'shopping' => fake()->randomFloat(2, 30000, 400000),
                        'rent' => fake()->randomFloat(2, 400000, 1500000),
                        'education' => fake()->randomFloat(2, 100000, 1200000),
                        'healthcare' => fake()->randomFloat(2, 25000, 500000),
                        'transfer_out' => fake()->randomFloat(2, 50000, 1500000),
                        'mobile_money' => fake()->randomFloat(2, 10000, 300000),
                        default => fake()->randomFloat(2, 10000, 300000),
                    };
                }

                $date = $dates[$i];
                $reference = 'TXN' . $date->format('Ymd') . '-' . strtoupper(substr(md5(uniqid(mt_rand(), true)), 0, 8));

                $transactions[] = [
                    'type' => $type,
                    'amount' => $amount,
                    'category' => $category,
                    'description' => $description,
                    'reference' => $reference,
                    'date' => $date,
                ];
            }

            // Calculate balance_after for each transaction (from last to first)
            $currentBalance = $runningBalance;
            for ($i = count($transactions) - 1; $i >= 0; $i--) {
                $transactions[$i]['balance_after'] = $currentBalance;
                if ($transactions[$i]['type'] === 'credit') {
                    $currentBalance -= $transactions[$i]['amount'];
                } else {
                    $currentBalance += $transactions[$i]['amount'];
                }
            }

            // Insert transactions in chronological order
            foreach ($transactions as $txn) {
                Transaction::create([
                    'account_id' => $account->id,
                    'type' => $txn['type'],
                    'amount' => $txn['amount'],
                    'category' => $txn['category'],
                    'description' => $txn['description'],
                    'reference' => $txn['reference'],
                    'balance_after' => round($txn['balance_after'], 2),
                    'metadata' => null,
                    'created_at' => $txn['date'],
                    'updated_at' => $txn['date'],
                ]);
                $transactionCount++;
            }
        }

        $this->command->info("✅ {$transactionCount} transactions created across " . count($accounts) . " accounts.");

        // ================================================================
        // 5. Seed Crypto Wallets (demo balances for each user)
        // ================================================================
        $this->command->info('₿ Seeding crypto wallets...');

        $symbols = ['BTC', 'ETH', 'SOL', 'BNB', 'USDT', 'USDC', 'TON', 'S'];

        $demoBalances = [
            'rakoto@example.com' => ['BTC' => 0.0012, 'ETH' => 0.08, 'SOL' => 6.5, 'USDT' => 120.0, 'USDC' => 0, 'BNB' => 0.3, 'TON' => 50.0, 'S' => 0],
            'rasoa@example.com'  => ['BTC' => 0.0035, 'ETH' => 0.25, 'SOL' => 0,   'USDT' => 450.0, 'USDC' => 200.0, 'BNB' => 0, 'TON' => 0, 'S' => 1000.0],
            'rabe@example.com'   => ['BTC' => 0,      'ETH' => 0,    'SOL' => 2.0, 'USDT' => 55.0,  'USDC' => 0, 'BNB' => 0, 'TON' => 25.0, 'S' => 0],
        ];

        $addressGenerators = [
            'BTC'  => fn() => 'bc1q' . substr(bin2hex(random_bytes(20)), 0, 38),
            'ETH'  => fn() => '0x' . bin2hex(random_bytes(20)),
            'SOL'  => fn() => rtrim(strtr(base64_encode(random_bytes(32)), '+/', 'AB'), '='),
            'BNB'  => fn() => '0x' . bin2hex(random_bytes(20)),
            'USDT' => fn() => '0x' . bin2hex(random_bytes(20)),
            'USDC' => fn() => '0x' . bin2hex(random_bytes(20)),
            'TON'  => fn() => 'UQ' . substr(rtrim(strtr(base64_encode(random_bytes(34)), '+/', '-_'), '='), 0, 46),
            'S'    => fn() => '0x' . bin2hex(random_bytes(20)),
        ];

        User::where('role', 'user')->get()->each(function (User $user) use ($symbols, $demoBalances, $addressGenerators) {
            $balances = $demoBalances[$user->email] ?? [];
            foreach ($symbols as $symbol) {
                CryptoWallet::create([
                    'user_id' => $user->id,
                    'symbol'  => $symbol,
                    'address' => ($addressGenerators[$symbol])(),
                    'balance' => $balances[$symbol] ?? 0,
                ]);
            }
        });

        // Admin gets empty wallets
        foreach ($symbols as $symbol) {
            CryptoWallet::create([
                'user_id' => $admin->id,
                'symbol'  => $symbol,
                'address' => ($addressGenerators[$symbol])(),
                'balance' => 0,
            ]);
        }

        $this->command->info('✅ Crypto wallets seeded.');

        // ================================================================
        // Summary
        // ================================================================
        $this->command->newLine();
        $this->command->info('═══════════════════════════════════════════');
        $this->command->info('🏦 BANKING APP SEEDING COMPLETE');
        $this->command->info('═══════════════════════════════════════════');
        $this->command->info("👤 Users: " . User::count());
        $this->command->info("💳 Accounts: " . Account::count());
        $this->command->info("📊 Transactions: " . Transaction::count());
        $this->command->info("💰 Cards: " . Card::count());
        $this->command->info("👥 Beneficiaries: " . Beneficiary::count());
        $this->command->newLine();
        $this->command->info('🔐 Login Credentials:');
        $this->command->info('   Admin:  admin@bankingapp.mg / Admin@2026');
        $this->command->info('   User 1: rakoto@example.com / Password@123');
        $this->command->info('   User 2: rasoa@example.com / Password@123');
        $this->command->info('   User 3: rabe@example.com / Password@123');
        $this->command->info('═══════════════════════════════════════════');
    }
}
