<?php

namespace Database\Factories;

use App\Models\Transaction;
use App\Models\Account;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<Transaction>
 */
class TransactionFactory extends Factory
{
    protected $model = Transaction::class;

    /**
     * Realistic transaction categories by type.
     */
    private const CREDIT_CATEGORIES = [
        'salary' => ['Monthly Salary Payment', 'Bi-weekly Salary', 'Overtime Payment', 'Bonus Payment'],
        'transfer_in' => ['Transfer from Savings', 'Incoming Wire Transfer', 'Family Transfer', 'Friend Payment'],
        'refund' => ['Purchase Refund', 'Service Refund', 'Overcharge Correction'],
        'interest' => ['Monthly Interest', 'Savings Interest Credit'],
        'mobile_money' => ['MVola Deposit', 'Airtel Money Received', 'Orange Money Deposit'],
    ];

    private const DEBIT_CATEGORIES = [
        'groceries' => ['Shoprite Groceries', 'Leader Price Market', 'Jumbo Score Purchase', 'Local Market Shopping'],
        'utilities' => ['JIRAMA Electricity', 'JIRAMA Water Bill', 'Internet Subscription', 'Telma Mobile Recharge'],
        'transport' => ['Fuel Station', 'Taxi Fare', 'Bus Ticket', 'Vehicle Maintenance'],
        'dining' => ['Restaurant La Varangue', 'Café de la Gare', 'Pizza Hut Antananarivo', 'Street Food Purchase'],
        'shopping' => ['Galaxy.mg Purchase', 'Clothing Store', 'Electronics Purchase', 'Pharmacy'],
        'rent' => ['Monthly Rent Payment', 'Office Rent', 'Parking Fee'],
        'education' => ['School Tuition', 'Book Purchase', 'Online Course Fee'],
        'healthcare' => ['Doctor Visit', 'Pharmacy Purchase', 'Lab Tests', 'Hospital Fee'],
        'transfer_out' => ['Transfer to Friend', 'Bill Payment', 'Loan Repayment'],
        'mobile_money' => ['MVola Withdrawal', 'Airtel Money Transfer', 'Orange Money Payment'],
    ];

    /**
     * Define the model's default state.
     *
     * @return array<string, mixed>
     */
    public function definition(): array
    {
        $type = $this->faker->randomElement(['credit', 'debit', 'debit', 'debit']); // 75% debit
        $categories = $type === 'credit' ? self::CREDIT_CATEGORIES : self::DEBIT_CATEGORIES;
        $category = $this->faker->randomElement(array_keys($categories));
        $description = $this->faker->randomElement($categories[$category]);

        // Realistic amounts by category (in MGA)
        $amount = match ($category) {
            'salary' => $this->faker->randomFloat(2, 800000, 5000000),
            'rent' => $this->faker->randomFloat(2, 300000, 2000000),
            'groceries' => $this->faker->randomFloat(2, 15000, 250000),
            'utilities' => $this->faker->randomFloat(2, 20000, 150000),
            'transport' => $this->faker->randomFloat(2, 5000, 80000),
            'dining' => $this->faker->randomFloat(2, 10000, 120000),
            'shopping' => $this->faker->randomFloat(2, 25000, 500000),
            'education' => $this->faker->randomFloat(2, 100000, 1500000),
            'healthcare' => $this->faker->randomFloat(2, 30000, 800000),
            'transfer_in', 'transfer_out' => $this->faker->randomFloat(2, 50000, 3000000),
            'refund' => $this->faker->randomFloat(2, 10000, 200000),
            'interest' => $this->faker->randomFloat(2, 1000, 50000),
            'mobile_money' => $this->faker->randomFloat(2, 10000, 1000000),
            default => $this->faker->randomFloat(2, 5000, 500000),
        };

        $date = $this->faker->dateTimeBetween('-6 months', 'now');
        $reference = 'TXN' . $date->format('Ymd') . '-' . strtoupper(substr(md5(uniqid(mt_rand(), true)), 0, 8));

        return [
            'account_id' => Account::factory(),
            'type' => $type,
            'amount' => $amount,
            'category' => $category,
            'description' => $description,
            'reference' => $reference,
            'balance_after' => null, // Will be calculated in seeder
            'metadata' => null,
            'created_at' => $date,
            'updated_at' => $date,
        ];
    }

    /**
     * Create a credit transaction.
     */
    public function credit(): static
    {
        return $this->state(function (array $attributes) {
            $category = $this->faker->randomElement(array_keys(self::CREDIT_CATEGORIES));
            return [
                'type' => 'credit',
                'category' => $category,
                'description' => $this->faker->randomElement(self::CREDIT_CATEGORIES[$category]),
            ];
        });
    }

    /**
     * Create a debit transaction.
     */
    public function debit(): static
    {
        return $this->state(function (array $attributes) {
            $category = $this->faker->randomElement(array_keys(self::DEBIT_CATEGORIES));
            return [
                'type' => 'debit',
                'category' => $category,
                'description' => $this->faker->randomElement(self::DEBIT_CATEGORIES[$category]),
            ];
        });
    }
}
