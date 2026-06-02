<?php

namespace Tests\Feature;

use App\Models\Account;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Cache;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class BankingFlowTest extends TestCase
{
    use RefreshDatabase;

    public function test_small_transfer_is_atomic_and_creates_transactions(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $response = $this->postJson('/api/transfers', [
            'sender_account_id' => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount' => 100000,
            'note' => 'Small transfer',
        ]);

        $response->assertCreated()
            ->assertJsonPath('data.otp_required', false)
            ->assertJsonPath('data.transfer.status', 'completed');

        $this->assertDatabaseHas('accounts', ['id' => $sender->id, 'balance' => 900000]);
        $this->assertDatabaseHas('accounts', ['id' => $receiver->id, 'balance' => 600000]);
        $this->assertDatabaseCount('transactions', 2);
        $this->assertDatabaseHas('audit_logs', ['action' => 'transfer.completed']);
    }

    public function test_large_transfer_requires_otp_then_completes_after_verification(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $init = $this->postJson('/api/transfers', [
            'sender_account_id' => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount' => 600000,
            'note' => 'Large transfer',
        ]);

        $init->assertCreated()->assertJsonPath('data.otp_required', true);

        $reference = $init->json('data.transfer.reference');
        $otp = Cache::get('transfer_otp_' . $reference);
        $this->assertNotEmpty($otp);

        $verify = $this->postJson('/api/transfers/verify', [
            'reference' => $reference,
            'otp' => $otp,
        ]);

        $verify->assertOk()
            ->assertJsonPath('data.status', 'completed')
            ->assertJsonPath('data.otp_verified', true);

        $this->assertDatabaseHas('accounts', ['id' => $sender->id, 'balance' => 400000]);
        $this->assertDatabaseHas('accounts', ['id' => $receiver->id, 'balance' => 1100000]);
    }

    public function test_beneficiaries_cards_qr_and_statement_endpoints_work(): void
    {
        [$user, $sender] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/beneficiaries', [
            'name' => 'Demo Beneficiary',
            'bank_name' => 'BNI Madagascar',
            'account_number' => 'MG12345678901234',
            'phone' => '+261341234567',
            'channel' => 'bank',
        ])->assertCreated()->assertJsonPath('data.is_verified', true);

        $card = $this->postJson('/api/cards', [
            'account_id' => $sender->id,
            'daily_limit' => 2500000,
        ])->assertCreated()->json('data');

        $this->postJson('/api/cards/' . $card['id'] . '/toggle')
            ->assertOk()
            ->assertJsonPath('data.is_blocked', true);

        $this->postJson('/api/qr/generate', [
            'account_id' => $sender->id,
            'amount' => 25000,
        ])->assertOk()->assertJsonStructure(['data' => ['payload', 'display']]);

        $this->getJson('/api/statements/monthly?account_id=' . $sender->id)
            ->assertOk()
            ->assertHeader('content-type', 'application/pdf');
    }

    private function accounts(): array
    {
        $user = User::factory()->create(['role' => 'user', 'is_active' => true]);
        $other = User::factory()->create(['role' => 'user', 'is_active' => true]);

        $sender = Account::create([
            'user_id' => $user->id,
            'account_number' => 'MG11111111111111',
            'balance' => 1000000,
            'currency' => 'MGA',
            'status' => 'active',
            'type' => 'checking',
        ]);

        $receiver = Account::create([
            'user_id' => $other->id,
            'account_number' => 'MG22222222222222',
            'balance' => 500000,
            'currency' => 'MGA',
            'status' => 'active',
            'type' => 'checking',
        ]);

        return [$user, $sender, $receiver];
    }
}
