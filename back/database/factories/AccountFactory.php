<?php

namespace Database\Factories;

use App\Models\Account;
use App\Models\User;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends Factory<Account>
 */
class AccountFactory extends Factory
{
    protected $model = Account::class;

    /**
     * Define the model's default state.
     *
     * @return array<string, mixed>
     */
    public function definition(): array
    {
        return [
            'user_id' => User::factory(),
            'account_number' => 'MG' . str_pad($this->faker->unique()->numberBetween(10000000000000, 99999999999999), 14, '0', STR_PAD_LEFT),
            'balance' => $this->faker->randomFloat(2, 50000, 25000000), // 50K to 25M MGA
            'currency' => 'MGA',
            'status' => 'active',
            'type' => $this->faker->randomElement(['checking', 'savings', 'checking']), // Weighted toward checking
        ];
    }

    /**
     * Set the account as inactive.
     */
    public function inactive(): static
    {
        return $this->state(fn (array $attributes) => [
            'status' => 'inactive',
        ]);
    }

    /**
     * Set the account as a savings account with higher balance.
     */
    public function savings(): static
    {
        return $this->state(fn (array $attributes) => [
            'type' => 'savings',
            'balance' => $this->faker->randomFloat(2, 1000000, 100000000),
        ]);
    }
}
