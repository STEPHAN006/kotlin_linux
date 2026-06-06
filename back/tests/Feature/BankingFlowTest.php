<?php

namespace Tests\Feature;

use App\Models\Account;
use App\Models\Beneficiary;
use App\Models\Card;
use App\Models\SupportMessage;
use App\Models\SupportTicket;
use App\Models\Transaction;
use App\Models\Transfer;
use App\Models\User;
use App\Models\UserNotification;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Cache;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class BankingFlowTest extends TestCase
{
    use RefreshDatabase;

    // ─── Auth ─────────────────────────────────────────────────────────────────

    public function test_user_can_register_and_gets_token(): void
    {
        $response = $this->postJson('/api/register', [
            'name'                  => 'Test User',
            'email'                 => 'test@scpay.mg',
            'phone'                 => '+261341234567',
            'password'              => 'password123',
            'password_confirmation' => 'password123',
        ]);

        $response->assertCreated()
            ->assertJsonStructure(['data' => ['token', 'user']]);

        $this->assertDatabaseHas('users', ['email' => 'test@scpay.mg']);
    }

    public function test_register_creates_default_mga_account(): void
    {
        $this->postJson('/api/register', [
            'name'                  => 'New User',
            'email'                 => 'new@scpay.mg',
            'phone'                 => '+261340000001',
            'password'              => 'password123',
            'password_confirmation' => 'password123',
        ])->assertCreated();

        $user = User::where('email', 'new@scpay.mg')->first();
        $this->assertNotNull($user);
        $this->assertDatabaseHas('accounts', ['user_id' => $user->id, 'currency' => 'MGA']);
    }

    public function test_register_rejects_duplicate_email(): void
    {
        User::factory()->create(['email' => 'dup@scpay.mg']);

        $this->postJson('/api/register', [
            'name'                  => 'Dup',
            'email'                 => 'dup@scpay.mg',
            'phone'                 => '+261340000002',
            'password'              => 'password123',
            'password_confirmation' => 'password123',
        ])->assertUnprocessable();
    }

    public function test_user_can_login_with_correct_credentials(): void
    {
        User::factory()->create(['email' => 'login@scpay.mg', 'password' => bcrypt('secret')]);

        $this->postJson('/api/login', ['email' => 'login@scpay.mg', 'password' => 'secret'])
            ->assertOk()
            ->assertJsonStructure(['data' => ['token']]);
    }

    public function test_login_rejects_wrong_password(): void
    {
        User::factory()->create(['email' => 'bad@scpay.mg', 'password' => bcrypt('correct')]);

        $this->postJson('/api/login', ['email' => 'bad@scpay.mg', 'password' => 'wrong'])
            ->assertUnprocessable();
    }

    public function test_user_can_logout(): void
    {
        $user = User::factory()->create();
        Sanctum::actingAs($user);

        $this->postJson('/api/logout')->assertOk();
    }

    public function test_authenticated_user_profile_is_returned(): void
    {
        $user = User::factory()->create();
        Sanctum::actingAs($user);

        $this->getJson('/api/user')
            ->assertOk()
            ->assertJsonPath('data.email', $user->email);
    }

    public function test_unauthenticated_request_is_rejected(): void
    {
        $this->getJson('/api/balance')->assertUnauthorized();
    }

    // ─── Accounts & Balance ───────────────────────────────────────────────────

    public function test_balance_returns_accounts_and_totals(): void
    {
        [$user, $sender] = $this->accounts();
        Sanctum::actingAs($user);

        $this->getJson('/api/balance')
            ->assertOk()
            ->assertJsonStructure(['data' => ['accounts', 'total_balance', 'currency']]);
    }

    public function test_balance_total_reflects_all_user_accounts(): void
    {
        $user = User::factory()->create(['role' => 'user', 'is_active' => true]);

        Account::create(['user_id' => $user->id, 'account_number' => 'MGA0001', 'balance' => 200000, 'currency' => 'MGA', 'status' => 'active', 'type' => 'checking']);
        Account::create(['user_id' => $user->id, 'account_number' => 'MGA0002', 'balance' => 300000, 'currency' => 'MGA', 'status' => 'active', 'type' => 'savings']);

        Sanctum::actingAs($user);

        $response = $this->getJson('/api/balance')->assertOk();
        $this->assertEquals(500000, $response->json('data.total_balance'));
    }

    // ─── Transfers ────────────────────────────────────────────────────────────

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
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount'              => 600000,
            'note'                => 'Large transfer',
        ]);

        $init->assertCreated()->assertJsonPath('data.otp_required', true);

        $reference = $init->json('data.transfer.reference');
        $otp = Cache::get('transfer_otp_' . $reference);
        $this->assertNotEmpty($otp);

        $this->postJson('/api/transfers/verify', ['reference' => $reference, 'otp' => $otp])
            ->assertOk()
            ->assertJsonPath('data.status', 'completed')
            ->assertJsonPath('data.otp_verified', true);

        $this->assertDatabaseHas('accounts', ['id' => $sender->id, 'balance' => 400000]);
        $this->assertDatabaseHas('accounts', ['id' => $receiver->id, 'balance' => 1100000]);
    }

    public function test_transfer_fails_with_insufficient_balance(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/transfers', [
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount'              => 9999999,
        ])->assertUnprocessable();

        $this->assertDatabaseHas('accounts', ['id' => $sender->id, 'balance' => 1000000]);
    }

    public function test_transfer_rejects_amount_below_minimum(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/transfers', [
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount'              => 50,
        ])->assertUnprocessable();
    }

    public function test_transfer_otp_verification_fails_with_wrong_code(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $init = $this->postJson('/api/transfers', [
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount'              => 600000,
        ])->assertCreated();

        $reference = $init->json('data.transfer.reference');

        $this->postJson('/api/transfers/verify', ['reference' => $reference, 'otp' => '000000'])
            ->assertStatus(422);
    }

    public function test_user_cannot_transfer_from_another_users_account(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        $otherUser = User::factory()->create(['role' => 'user', 'is_active' => true]);
        Sanctum::actingAs($otherUser);

        // otherUser doesn't own $sender → 404 (findOrFail) or 422
        $response = $this->postJson('/api/transfers', [
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount'              => 100000,
        ]);
        $this->assertContains($response->status(), [404, 422]);
    }

    public function test_transfer_list_returns_authenticated_user_transfers(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/transfers', [
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount'              => 100000,
        ]);

        $this->getJson('/api/transfers')
            ->assertOk()
            ->assertJsonStructure(['data']);
    }

    // ─── Beneficiaries ────────────────────────────────────────────────────────

    public function test_beneficiary_can_be_created(): void
    {
        [$user] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/beneficiaries', [
            'name'           => 'Jean Dupont',
            'bank_name'      => 'BNI Madagascar',
            'account_number' => 'MG12345678901234',
            'phone'          => '+261341234567',
            'channel'        => 'bank',
        ])->assertCreated()->assertJsonPath('data.is_verified', true);

        $this->assertDatabaseHas('beneficiaries', ['name' => 'Jean Dupont']);
    }

    public function test_beneficiary_list_returns_only_user_entries(): void
    {
        [$user] = $this->accounts();
        $other = User::factory()->create(['role' => 'user', 'is_active' => true]);

        Beneficiary::create(['user_id' => $other->id, 'name' => 'Other', 'bank_name' => 'BNI', 'account_number' => 'MG99', 'channel' => 'bank', 'is_verified' => true]);

        Sanctum::actingAs($user);
        $response = $this->getJson('/api/beneficiaries')->assertOk();

        $names = collect($response->json('data'))->pluck('name');
        $this->assertFalse($names->contains('Other'));
    }

    public function test_beneficiary_can_be_deleted(): void
    {
        [$user] = $this->accounts();

        $bene = Beneficiary::create([
            'user_id'        => $user->id,
            'name'           => 'To Delete',
            'bank_name'      => 'BNI',
            'account_number' => 'MG00000001',
            'channel'        => 'bank',
            'is_verified'    => true,
        ]);

        Sanctum::actingAs($user);

        $this->deleteJson("/api/beneficiaries/{$bene->id}")
            ->assertOk();

        $this->assertDatabaseMissing('beneficiaries', ['id' => $bene->id]);
    }

    // ─── Cards ────────────────────────────────────────────────────────────────

    public function test_card_can_be_created_for_account(): void
    {
        [$user, $sender] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/cards', [
            'account_id'  => $sender->id,
            'daily_limit' => 2500000,
        ])->assertCreated()->assertJsonStructure(['data' => ['id', 'card_number_masked']]);
    }

    public function test_card_toggle_blocks_and_unblocks(): void
    {
        [$user, $sender] = $this->accounts();
        Sanctum::actingAs($user);

        $card = $this->postJson('/api/cards', ['account_id' => $sender->id, 'daily_limit' => 1000000])
            ->assertCreated()->json('data');

        $this->postJson("/api/cards/{$card['id']}/toggle")
            ->assertOk()->assertJsonPath('data.is_blocked', true);

        $this->postJson("/api/cards/{$card['id']}/toggle")
            ->assertOk()->assertJsonPath('data.is_blocked', false);
    }

    public function test_card_list_returns_user_cards(): void
    {
        [$user, $sender] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/cards', ['account_id' => $sender->id, 'daily_limit' => 1000000]);

        $response = $this->getJson('/api/cards')->assertOk();
        $this->assertGreaterThanOrEqual(1, count($response->json('data')));
    }

    // ─── QR ───────────────────────────────────────────────────────────────────

    public function test_qr_generate_returns_payload(): void
    {
        [$user, $sender] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/qr/generate', [
            'account_id' => $sender->id,
            'amount'     => 25000,
        ])->assertOk()->assertJsonStructure(['data' => ['payload', 'display']]);
    }

    public function test_qr_pay_completes_transaction(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        $receiverUser = $receiver->user;

        // Receiver generates their QR
        Sanctum::actingAs($receiverUser);
        $qr = $this->postJson('/api/qr/generate', ['account_id' => $receiver->id, 'amount' => 50000])
            ->assertOk()->json('data.payload');

        // Sender scans and pays
        Sanctum::actingAs($user);
        $this->postJson('/api/qr/scan', ['payload' => $qr])->assertOk();
        $this->postJson('/api/qr/pay', [
            'payload'           => $qr,
            'sender_account_id' => $sender->id,
            'amount'            => 50000,
        ])->assertSuccessful();

        $this->assertDatabaseHas('accounts', ['id' => $sender->id, 'balance' => 950000]);
    }

    public function test_qr_pay_rejects_amount_below_minimum(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $qr = $this->postJson('/api/qr/generate', ['account_id' => $receiver->id, 'amount' => 50000])
            ->json('data.payload');

        $this->postJson('/api/qr/pay', [
            'payload'    => $qr,
            'account_id' => $sender->id,
            'amount'     => 50,
        ])->assertUnprocessable();
    }

    public function test_qr_scan_rejects_invalid_payload(): void
    {
        [$user] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/qr/scan', ['payload' => 'not-valid-base64!!!'])
            ->assertUnprocessable();
    }

    // ─── Statements ───────────────────────────────────────────────────────────

    public function test_monthly_statement_returns_pdf(): void
    {
        [$user, $sender] = $this->accounts();
        Sanctum::actingAs($user);

        $this->getJson('/api/statements/monthly?account_id=' . $sender->id)
            ->assertOk()
            ->assertHeader('content-type', 'application/pdf');
    }

    public function test_csv_export_returns_csv_file(): void
    {
        [$user, $sender] = $this->accounts();
        Sanctum::actingAs($user);

        $response = $this->getJson('/api/transactions/export?account_id=' . $sender->id . '&format=csv')
            ->assertOk();

        $this->assertStringContainsString('text/csv', $response->headers->get('Content-Type'));
    }

    public function test_swift_export_returns_mt940_format(): void
    {
        [$user, $sender] = $this->accounts();
        Sanctum::actingAs($user);

        $response = $this->call('GET', '/api/transactions/export', [
            'account_id' => $sender->id,
            'format'     => 'swift',
        ]);

        $response->assertOk();
        $this->assertStringContainsString(':20:SCPAY-EXPORT', $response->getContent());
    }

    public function test_statement_rejects_other_users_account(): void
    {
        [$user, $sender] = $this->accounts();
        $other = User::factory()->create(['role' => 'user', 'is_active' => true]);
        Sanctum::actingAs($other);

        $this->getJson('/api/statements/monthly?account_id=' . $sender->id)
            ->assertNotFound();
    }

    // ─── Transactions ─────────────────────────────────────────────────────────

    public function test_transactions_list_returns_authenticated_user_records(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/transfers', [
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount'              => 100000,
        ]);

        $this->getJson('/api/transactions')
            ->assertOk()
            ->assertJsonStructure(['data']);
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    public function test_admin_dashboard_accessible_by_admin(): void
    {
        $admin = User::factory()->create(['role' => 'admin', 'is_active' => true]);
        Sanctum::actingAs($admin);

        $this->getJson('/api/admin/dashboard')
            ->assertOk()
            ->assertJsonStructure(['data']);
    }

    public function test_admin_routes_blocked_for_regular_user(): void
    {
        $user = User::factory()->create(['role' => 'user', 'is_active' => true]);
        Sanctum::actingAs($user);

        $this->getJson('/api/admin/dashboard')->assertForbidden();
    }

    public function test_admin_can_list_all_transactions(): void
    {
        $admin = User::factory()->create(['role' => 'admin', 'is_active' => true]);
        Sanctum::actingAs($admin);

        $this->getJson('/api/admin/transactions')
            ->assertOk()
            ->assertJsonStructure(['data']);
    }

    // ─── Health ───────────────────────────────────────────────────────────────

    public function test_health_endpoint_is_public_and_operational(): void
    {
        $this->getJson('/api/health')
            ->assertOk()
            ->assertJsonPath('success', true);
    }

    // ─── Compound flow ────────────────────────────────────────────────────────

    public function test_beneficiaries_cards_qr_and_statement_endpoints_work(): void
    {
        [$user, $sender] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/beneficiaries', [
            'name'           => 'Demo Beneficiary',
            'bank_name'      => 'BNI Madagascar',
            'account_number' => 'MG12345678901234',
            'phone'          => '+261341234567',
            'channel'        => 'bank',
        ])->assertCreated()->assertJsonPath('data.is_verified', true);

        $card = $this->postJson('/api/cards', [
            'account_id'  => $sender->id,
            'daily_limit' => 2500000,
        ])->assertCreated()->json('data');

        $this->postJson('/api/cards/' . $card['id'] . '/toggle')
            ->assertOk()
            ->assertJsonPath('data.is_blocked', true);

        $this->postJson('/api/qr/generate', [
            'account_id' => $sender->id,
            'amount'     => 25000,
        ])->assertOk()->assertJsonStructure(['data' => ['payload', 'display']]);

        $this->getJson('/api/statements/monthly?account_id=' . $sender->id)
            ->assertOk()
            ->assertHeader('content-type', 'application/pdf');
    }

    // ─── Notifications ────────────────────────────────────────────────────────

    public function test_notifications_list_returns_user_notifications(): void
    {
        [$user] = $this->accounts();
        UserNotification::create(['user_id' => $user->id, 'title' => 'Test notif', 'body' => 'Corps']);
        Sanctum::actingAs($user);

        $response = $this->getJson('/api/notifications')->assertOk();
        $this->assertCount(1, $response->json('data'));
        $this->assertEquals('Test notif', $response->json('data.0.title'));
    }

    public function test_notifications_only_returns_own_notifications(): void
    {
        [$user] = $this->accounts();
        $other = User::factory()->create(['role' => 'user', 'is_active' => true]);
        UserNotification::create(['user_id' => $other->id, 'title' => 'Not mine', 'body' => 'x']);

        Sanctum::actingAs($user);
        $response = $this->getJson('/api/notifications')->assertOk();
        $this->assertCount(0, $response->json('data'));
    }

    public function test_mark_all_notifications_read(): void
    {
        [$user] = $this->accounts();
        UserNotification::create(['user_id' => $user->id, 'title' => 'A', 'body' => 'x']);
        UserNotification::create(['user_id' => $user->id, 'title' => 'B', 'body' => 'y']);
        Sanctum::actingAs($user);

        $this->postJson('/api/notifications/read-all')->assertOk();

        $response = $this->getJson('/api/notifications')->assertOk();
        $allRead = collect($response->json('data'))->every(fn($n) => $n['read'] === true);
        $this->assertTrue($allRead);
    }

    public function test_transfer_creates_notification_for_sender(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        Sanctum::actingAs($user);

        $this->postJson('/api/transfers', [
            'sender_account_id'   => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount'              => 100000,
        ])->assertCreated();

        $this->assertDatabaseHas('user_notifications', [
            'user_id' => $user->id,
        ]);
    }

    // ─── Support Chat ─────────────────────────────────────────────────────────

    public function test_support_ticket_is_created_on_first_request(): void
    {
        [$user] = $this->accounts();
        Sanctum::actingAs($user);

        $response = $this->getJson('/api/support/ticket')->assertOk();
        $this->assertEquals('open', $response->json('data.status'));
        $this->assertNotEmpty($response->json('data.messages'));
        $this->assertDatabaseHas('support_tickets', ['user_id' => $user->id]);
    }

    public function test_support_ticket_reuse_same_open_ticket(): void
    {
        [$user] = $this->accounts();
        Sanctum::actingAs($user);

        $first  = $this->getJson('/api/support/ticket')->assertOk()->json('data.id');
        $second = $this->getJson('/api/support/ticket')->assertOk()->json('data.id');
        $this->assertEquals($first, $second);
        $this->assertDatabaseCount('support_tickets', 1);
    }

    public function test_user_can_send_support_message(): void
    {
        [$user] = $this->accounts();
        Sanctum::actingAs($user);

        $ticketId = $this->getJson('/api/support/ticket')->json('data.id');

        $response = $this->postJson("/api/support/ticket/{$ticketId}/messages", [
            'message' => 'Bonjour, j\'ai un problème.',
        ])->assertOk();

        $userMessages = collect($response->json('data.messages'))
            ->where('sender', 'user');
        $this->assertCount(1, $userMessages);
    }

    public function test_support_message_validates_required_fields(): void
    {
        [$user] = $this->accounts();
        Sanctum::actingAs($user);
        $ticketId = $this->getJson('/api/support/ticket')->json('data.id');

        $this->postJson("/api/support/ticket/{$ticketId}/messages", [])
            ->assertUnprocessable();
    }

    // ─── Anomaly detection ────────────────────────────────────────────────────

    public function test_fraud_alerts_endpoint_returns_structured_data(): void
    {
        $admin = User::factory()->create(['role' => 'admin', 'is_active' => true]);
        Sanctum::actingAs($admin);

        $response = $this->getJson('/api/admin/fraud-alerts')->assertOk();
        $data = $response->json('data');
        $this->assertArrayHasKey('alert_count', $data);
        $this->assertArrayHasKey('alerts', $data);
        $this->assertArrayHasKey('system_status', $data);
    }

    public function test_large_transaction_appears_in_fraud_alerts(): void
    {
        [$user, $sender] = $this->accounts();
        $admin = User::factory()->create(['role' => 'admin', 'is_active' => true]);

        Transaction::create([
            'account_id'    => $sender->id,
            'type'          => 'debit',
            'amount'        => 15000000,
            'category'      => 'transfer_out',
            'description'   => 'Gros virement',
            'reference'     => 'TXN-BIG-001',
            'balance_after' => 0,
        ]);

        Sanctum::actingAs($admin);
        $response = $this->getJson('/api/admin/fraud-alerts')->assertOk();
        $this->assertGreaterThan(0, $response->json('data.alert_count'));
    }

    public function test_repeated_same_amount_detected_as_suspicious(): void
    {
        [$user, $sender] = $this->accounts();
        $repo = new \App\Repositories\TransactionRepository();

        for ($i = 0; $i < 3; $i++) {
            Transaction::create([
                'account_id'    => $sender->id,
                'type'          => 'debit',
                'amount'        => 50000,
                'category'      => 'transfer_out',
                'description'   => 'Repeat',
                'reference'     => 'TXN-RPT-00' . $i,
                'balance_after' => 500000,
            ]);
        }

        $this->assertTrue($repo->isSuspicious($sender->id));
    }

    // ─── Admin dashboard ──────────────────────────────────────────────────────

    public function test_admin_dashboard_returns_statistics(): void
    {
        $admin = User::factory()->create(['role' => 'admin', 'is_active' => true]);
        Sanctum::actingAs($admin);

        $response = $this->getJson('/api/admin/dashboard')->assertOk();
        $stats = $response->json('data.statistics');
        $this->assertArrayHasKey('total_users', $stats);
        $this->assertArrayHasKey('total_transactions', $stats);
        $this->assertArrayHasKey('total_balance', $stats);
    }

    public function test_admin_transactions_respects_limit_param(): void
    {
        [$user, $sender, $receiver] = $this->accounts();
        $admin = User::factory()->create(['role' => 'admin', 'is_active' => true]);
        Sanctum::actingAs($user);

        for ($i = 0; $i < 5; $i++) {
            $this->postJson('/api/transfers', [
                'sender_account_id'   => $sender->id,
                'receiver_account_id' => $receiver->id,
                'amount'              => 10000,
            ]);
        }

        Sanctum::actingAs($admin);
        $response = $this->getJson('/api/admin/transactions?limit=3')->assertOk();
        $this->assertLessThanOrEqual(3, count($response->json('data')));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private function accounts(): array
    {
        $user  = User::factory()->create(['role' => 'user', 'is_active' => true]);
        $other = User::factory()->create(['role' => 'user', 'is_active' => true]);

        $sender = Account::create([
            'user_id'        => $user->id,
            'account_number' => 'MG11111111111111',
            'balance'        => 1000000,
            'currency'       => 'MGA',
            'status'         => 'active',
            'type'           => 'checking',
        ]);

        $receiver = Account::create([
            'user_id'        => $other->id,
            'account_number' => 'MG22222222222222',
            'balance'        => 500000,
            'currency'       => 'MGA',
            'status'         => 'active',
            'type'           => 'checking',
        ]);

        return [$user, $sender, $receiver];
    }
}
