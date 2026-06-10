<?php

namespace Tests\Feature;

use App\Models\Account;
use App\Models\Beneficiary;
use App\Models\Card;
use App\Models\CryptoWallet;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Cache;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class BankingFlowTest extends TestCase
{
    use RefreshDatabase;

    // ------------------------------------------------------------------ Auth

    public function test_user_can_register(): void
    {
        $this->postJson('/api/register', [
            'name'                  => 'Test User',
            'email'                 => 'test@scpay.mg',
            'phone'                 => '+261341111111',
            'password'              => 'password123',
            'password_confirmation' => 'password123',
        ])->assertCreated()
          ->assertJsonStructure(['data' => ['token', 'user']]);
    }

    public function test_user_can_login(): void
    {
        $user = User::factory()->create(['password' => bcrypt('secret')]);

        $this->postJson('/api/login', [
            'email'    => $user->email,
            'password' => 'secret',
        ])->assertOk()
          ->assertJsonStructure(['data' => ['token', 'user']]);
    }

    public function test_login_with_wrong_password_fails(): void
    {
        $user = User::factory()->create(['password' => bcrypt('secret')]);

        $this->postJson('/api/login', [
            'email'    => $user->email,
            'password' => 'wrongpassword',
        ])->assertUnprocessable();
    }

    public function test_duplicate_email_registration_fails(): void
    {
        User::factory()->create(['email' => 'dupe@scpay.mg']);

        $this->postJson('/api/register', [
            'name'                  => 'Dup User',
            'email'                 => 'dupe@scpay.mg',
            'phone'                 => '+261349999999',
            'password'              => 'password123',
            'password_confirmation' => 'password123',
        ])->assertUnprocessable();
    }

    public function test_unauthenticated_access_returns_401(): void
    {
        $this->getJson('/api/balance')->assertUnauthorized();
        $this->getJson('/api/transactions')->assertUnauthorized();
        $this->getJson('/api/cards')->assertUnauthorized();
    }

    public function test_authenticated_user_can_logout(): void
    {
        $user = User::factory()->create();
        Sanctum::actingAs($user);

        $this->postJson('/api/logout')->assertOk();
    }

    public function test_authenticated_user_can_get_profile(): void
    {
        $user = User::factory()->create();
        Sanctum::actingAs($user);

        $this->getJson('/api/user')
             ->assertOk()
             ->assertJsonPath('data.email', $user->email);
    }

    // ------------------------------------------------------------------ Accounts & Balance

    public function test_authenticated_user_can_get_balance(): void
    {
        [$user, $account] = $this->singleAccount();
        Sanctum::actingAs($user);

        $this->getJson('/api/balance')
             ->assertOk()
             ->assertJsonStructure(['data' => ['accounts', 'total_balance']]);
    }

    public function test_balance_reflects_account_sum(): void
    {
        [$user, $account] = $this->singleAccount(750000);
        Sanctum::actingAs($user);

        $response = $this->getJson('/api/balance')->assertOk();
        $this->assertEquals(750000, $response->json('data.total_balance'));
    }

    public function test_user_can_list_accounts(): void
    {
        [$user] = $this->singleAccount();
        Sanctum::actingAs($user);

        $this->getJson('/api/accounts')
             ->assertOk()
             ->assertJsonStructure(['data' => [['id', 'account_number', 'balance']]]);
    }

    // ------------------------------------------------------------------ Transactions

    public function test_user_can_get_transactions(): void
    {
        [$user] = $this->singleAccount();
        Sanctum::actingAs($user);

        $this->getJson('/api/transactions')
             ->assertOk()
             ->assertJsonStructure(['data']);
    }

    // ------------------------------------------------------------------ Transfers

    public function test_small_transfer_is_atomic_and_creates_transactions(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $response = $this->postJson('/api/transfers', [
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount'              => 100000,
            'note'                => 'Small transfer',
        ]);

        $response->assertCreated()
                 ->assertJsonPath('data.otp_required', false)
                 ->assertJsonPath('data.transfer.status', 'completed');

        $this->assertDatabaseHas('accounts', ['id' => $sender->id,   'balance' => 900000]);
        $this->assertDatabaseHas('accounts', ['id' => $receiver->id, 'balance' => 600000]);
        $this->assertDatabaseCount('transactions', 2);
        $this->assertDatabaseHas('audit_logs', ['action' => 'transfer.completed']);
    }

    public function test_large_transfer_requires_otp_then_completes_after_verification(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $init = $this->postJson('/api/transfers', [
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount'              => 600000,
            'note'                => 'Large transfer',
        ]);

        $init->assertCreated()->assertJsonPath('data.otp_required', true);

        $reference = $init->json('data.transfer.reference');
        $otp       = Cache::get('transfer_otp_' . $reference);
        $this->assertNotEmpty($otp);

        $verify = $this->postJson('/api/transfers/verify', [
            'reference' => $reference,
            'otp'       => $otp,
        ]);

        $verify->assertOk()
               ->assertJsonPath('data.status', 'completed')
               ->assertJsonPath('data.otp_verified', true);

        $this->assertDatabaseHas('accounts', ['id' => $sender->id,   'balance' => 400000]);
        $this->assertDatabaseHas('accounts', ['id' => $receiver->id, 'balance' => 1100000]);
    }

    public function test_wrong_otp_is_rejected(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $init = $this->postJson('/api/transfers', [
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount'              => 600000,
        ]);

        $reference = $init->json('data.transfer.reference');

        $this->postJson('/api/transfers/verify', [
            'reference' => $reference,
            'otp'       => '000000',
        ])->assertUnprocessable();
    }

    public function test_transfer_fails_with_insufficient_balance(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/transfers', [
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount'              => 9_999_999,
        ])->assertUnprocessable();
    }

    public function test_transfer_fails_to_same_account(): void
    {
        [$user, $sender] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/transfers', [
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $sender->id,
            'amount'              => 10000,
        ])->assertUnprocessable();
    }

    public function test_transfer_requires_positive_amount(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/transfers', [
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount'              => -500,
        ])->assertUnprocessable();
    }

    public function test_user_can_list_transfers(): void
    {
        [$user] = $this->accounts();
        Sanctum::actingAs($user);

        $this->getJson('/api/transfers')->assertOk()->assertJsonStructure(['data']);
    }

    // ------------------------------------------------------------------ Beneficiaries

    public function test_user_can_add_beneficiary(): void
    {
        [$user] = $this->singleAccount();
        Sanctum::actingAs($user);

        $this->postJson('/api/beneficiaries', [
            'name'           => 'Rasoa Marie',
            'bank_name'      => 'BNI Madagascar',
            'account_number' => 'MG12345678901234',
            'phone'          => '+261341234567',
            'channel'        => 'bank',
        ])->assertCreated()->assertJsonPath('data.is_verified', true);
    }

    public function test_user_can_list_beneficiaries(): void
    {
        [$user] = $this->singleAccount();
        Sanctum::actingAs($user);

        $this->getJson('/api/beneficiaries')->assertOk()->assertJsonStructure(['data']);
    }

    public function test_user_can_delete_beneficiary(): void
    {
        [$user] = $this->singleAccount();
        Sanctum::actingAs($user);

        $beneficiary = Beneficiary::create([
            'user_id'        => $user->id,
            'name'           => 'To Delete',
            'bank_name'      => 'BNI',
            'account_number' => 'MG99999999999999',
            'channel'        => 'bank',
            'is_verified'    => true,
        ]);

        $this->deleteJson('/api/beneficiaries/' . $beneficiary->id)->assertOk();

        $this->assertDatabaseMissing('beneficiaries', ['id' => $beneficiary->id]);
    }

    public function test_beneficiary_requires_name_and_account(): void
    {
        [$user] = $this->singleAccount();
        Sanctum::actingAs($user);

        $this->postJson('/api/beneficiaries', ['channel' => 'bank'])->assertUnprocessable();
    }

    // ------------------------------------------------------------------ Cards

    public function test_user_can_create_virtual_card(): void
    {
        [$user, $account] = $this->singleAccount();
        Sanctum::actingAs($user);

        $this->postJson('/api/cards', [
            'account_id'  => $account->id,
            'daily_limit' => 2500000,
        ])->assertCreated()
          ->assertJsonStructure(['data' => ['id', 'card_number_masked', 'expiry_date', 'is_blocked']]);
    }

    public function test_user_can_list_cards(): void
    {
        [$user] = $this->singleAccount();
        Sanctum::actingAs($user);

        $this->getJson('/api/cards')->assertOk()->assertJsonStructure(['data']);
    }

    public function test_user_can_toggle_card_block(): void
    {
        [$user, $account] = $this->singleAccount();
        Sanctum::actingAs($user);

        $card = $this->postJson('/api/cards', [
            'account_id'  => $account->id,
            'daily_limit' => 1000000,
        ])->json('data');

        $this->postJson('/api/cards/' . $card['id'] . '/toggle')
             ->assertOk()
             ->assertJsonPath('data.is_blocked', true);
    }

    public function test_user_can_unblock_card(): void
    {
        [$user, $account] = $this->singleAccount();
        Sanctum::actingAs($user);

        $card = $this->postJson('/api/cards', [
            'account_id'  => $account->id,
            'daily_limit' => 1000000,
        ])->json('data');

        $this->postJson('/api/cards/' . $card['id'] . '/toggle');
        $this->postJson('/api/cards/' . $card['id'] . '/toggle')
             ->assertOk()
             ->assertJsonPath('data.is_blocked', false);
    }

    // ------------------------------------------------------------------ QR Code

    public function test_user_can_generate_qr_code(): void
    {
        [$user, $account] = $this->singleAccount();
        Sanctum::actingAs($user);

        $this->postJson('/api/qr/generate', [
            'account_id' => $account->id,
            'amount'     => 25000,
        ])->assertOk()
          ->assertJsonStructure(['data' => ['payload', 'display']]);
    }

    public function test_user_can_scan_qr_code(): void
    {
        [$user, $account] = $this->singleAccount();
        Sanctum::actingAs($user);

        $qr = $this->postJson('/api/qr/generate', [
            'account_id' => $account->id,
            'amount'     => 10000,
        ])->json('data.payload');

        $this->postJson('/api/qr/scan', ['payload' => $qr])->assertOk();
    }

    public function test_qr_pay_completes_transfer(): void
    {
        // Two users each with their own account
        [$userA, $accountA] = $this->singleAccount(500000);
        [$userB, $accountB] = $this->singleAccount(500000);

        // User B generates a QR code for their own account (to receive payment)
        Sanctum::actingAs($userB);
        $qrResponse = $this->postJson('/api/qr/generate', [
            'account_id' => $accountB->id,
            'amount'     => 50000,
        ])->assertOk();
        $payload = $qrResponse->json('data.payload');
        $this->assertNotEmpty($payload);

        // User A scans and pays
        Sanctum::actingAs($userA);
        $this->postJson('/api/qr/pay', [
            'sender_account_id' => $accountA->id,
            'payload'           => $payload,
            'amount'            => 50000,
        ])->assertCreated()
          ->assertJsonStructure(['data' => ['otp_required', 'transfer']]);
    }

    // ------------------------------------------------------------------ Statement PDF

    public function test_statement_returns_pdf(): void
    {
        [$user, $account] = $this->singleAccount();
        Sanctum::actingAs($user);

        $this->getJson('/api/statements/monthly?account_id=' . $account->id)
             ->assertOk()
             ->assertHeader('content-type', 'application/pdf');
    }

    public function test_statement_requires_valid_account(): void
    {
        $user = User::factory()->create(['is_active' => true]);
        Sanctum::actingAs($user);

        $this->getJson('/api/statements/monthly?account_id=99999')
             ->assertUnprocessable();
    }

    // ------------------------------------------------------------------ Audit log

    public function test_audit_log_is_created_after_transfer(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/transfers', [
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount'              => 50000,
        ])->assertCreated();

        $this->assertDatabaseHas('audit_logs', ['action' => 'transfer.completed']);
    }

    // ------------------------------------------------------------------ Crypto

    public function test_user_can_get_crypto_wallets(): void
    {
        [$user, $account] = $this->singleAccount(500_000);
        Sanctum::actingAs($user);

        $this->getJson('/api/crypto/wallets')
            ->assertOk()
            ->assertJsonStructure(['data' => [['symbol', 'name', 'coin_id', 'address', 'balance']]]);

        $this->assertDatabaseCount('crypto_wallets', 8);
    }

    public function test_user_can_buy_crypto(): void
    {
        [$user, $account] = $this->singleAccount(1_000_000);
        Sanctum::actingAs($user);

        $this->postJson('/api/crypto/buy', [
            'symbol'      => 'USDT',
            'amount_mga'  => 45_000,
            'price_usd'   => 1.0,
            'mga_per_usd' => 4500,
        ])->assertOk()
          ->assertJsonPath('success', true)
          ->assertJsonStructure(['data' => ['symbol', 'crypto_amount', 'total_mga']]);

        $this->assertDatabaseHas('crypto_wallets', ['user_id' => $user->id, 'symbol' => 'USDT']);
        $this->assertDatabaseHas('crypto_transactions', ['user_id' => $user->id, 'type' => 'buy', 'symbol' => 'USDT']);
        $this->assertEquals(955_000, Account::find($account->id)->balance);
    }

    public function test_buy_crypto_fails_with_insufficient_mga_balance(): void
    {
        [$user, $account] = $this->singleAccount(10_000);
        Sanctum::actingAs($user);

        $this->postJson('/api/crypto/buy', [
            'symbol'      => 'BTC',
            'amount_mga'  => 500_000,
            'price_usd'   => 60000,
            'mga_per_usd' => 4500,
        ])->assertStatus(422)
          ->assertJsonPath('success', false);
    }

    public function test_user_can_sell_crypto(): void
    {
        [$user, $account] = $this->singleAccount(500_000);
        Sanctum::actingAs($user);

        CryptoWallet::create([
            'user_id' => $user->id,
            'symbol'  => 'USDT',
            'address' => '0xabc123',
            'balance' => 100.0,
        ]);

        $this->postJson('/api/crypto/sell', [
            'symbol'        => 'USDT',
            'crypto_amount' => 10.0,
            'price_usd'     => 1.0,
            'mga_per_usd'   => 4500,
        ])->assertOk()
          ->assertJsonPath('success', true)
          ->assertJsonStructure(['data' => ['symbol', 'crypto_amount', 'total_mga']]);

        $this->assertEquals(500_000 + 45_000, Account::find($account->id)->balance);
        $this->assertEquals(90.0, CryptoWallet::where('user_id', $user->id)->where('symbol', 'USDT')->first()->balance);
    }

    public function test_sell_crypto_fails_with_insufficient_balance(): void
    {
        [$user, $account] = $this->singleAccount(500_000);
        Sanctum::actingAs($user);

        CryptoWallet::create([
            'user_id' => $user->id,
            'symbol'  => 'BTC',
            'address' => 'bc1q_test',
            'balance' => 0.001,
        ]);

        $this->postJson('/api/crypto/sell', [
            'symbol'        => 'BTC',
            'crypto_amount' => 1.0,
            'price_usd'     => 60000,
            'mga_per_usd'   => 4500,
        ])->assertStatus(422)
          ->assertJsonPath('message', 'Solde crypto insuffisant.');
    }

    public function test_user_can_send_crypto(): void
    {
        [$user, $account] = $this->singleAccount(500_000);
        Sanctum::actingAs($user);

        CryptoWallet::create([
            'user_id' => $user->id,
            'symbol'  => 'ETH',
            'address' => '0xsender',
            'balance' => 2.0,
        ]);

        $this->postJson('/api/crypto/send', [
            'symbol'        => 'ETH',
            'crypto_amount' => 0.5,
            'to_address'    => '0x1234567890abcdef1234567890abcdef12345678',
            'price_usd'     => 3000,
            'mga_per_usd'   => 4500,
        ])->assertOk()
          ->assertJsonPath('success', true)
          ->assertJsonStructure(['data' => ['tx_hash']]);

        $this->assertEquals(1.5, CryptoWallet::where('user_id', $user->id)->where('symbol', 'ETH')->first()->balance);
        $this->assertDatabaseHas('crypto_transactions', ['user_id' => $user->id, 'type' => 'send', 'symbol' => 'ETH']);
    }

    public function test_user_can_list_crypto_transactions(): void
    {
        [$user, $account] = $this->singleAccount(500_000);
        Sanctum::actingAs($user);

        $this->getJson('/api/crypto/transactions')
            ->assertOk()
            ->assertJsonStructure(['data']);
    }

    // ------------------------------------------------------------------ KYC

    public function test_user_can_check_kyc_status(): void
    {
        [$user, $account] = $this->singleAccount();
        Sanctum::actingAs($user);

        $this->getJson('/api/kyc/status')
            ->assertOk()
            ->assertJsonPath('data.status', 'none');
    }

    public function test_kyc_submit_requires_all_fields(): void
    {
        [$user, $account] = $this->singleAccount();
        Sanctum::actingAs($user);

        $this->postJson('/api/kyc/submit', [])
            ->assertStatus(422);
    }

    public function test_notifications_endpoint_returns_list(): void
    {
        [$user, $account] = $this->singleAccount();
        Sanctum::actingAs($user);

        $this->getJson('/api/notifications')
            ->assertOk()
            ->assertJsonStructure(['data']);
    }

    // ------------------------------------------------------------------ Helpers

    private function singleAccount(float $balance = 1_000_000): array
    {
        $user = User::factory()->create(['role' => 'user', 'is_active' => true]);

        $account = Account::create([
            'user_id'        => $user->id,
            'account_number' => 'MG' . rand(10000000000000, 99999999999999),
            'balance'        => $balance,
            'currency'       => 'MGA',
            'status'         => 'active',
            'type'           => 'checking',
        ]);

        return [$user, $account];
    }

    private function accounts(): array
    {
        $user  = User::factory()->create(['role' => 'user', 'is_active' => true]);
        $other = User::factory()->create(['role' => 'user', 'is_active' => true]);

        $sender = Account::create([
            'user_id'        => $user->id,
            'account_number' => 'MG11111111111111',
            'balance'        => 1_000_000,
            'currency'       => 'MGA',
            'status'         => 'active',
            'type'           => 'checking',
        ]);

        $receiver = Account::create([
            'user_id'        => $other->id,
            'account_number' => 'MG22222222222222',
            'balance'        => 500_000,
            'currency'       => 'MGA',
            'status'         => 'active',
            'type'           => 'checking',
        ]);

        return [$user, $sender, $receiver];
    }
}
