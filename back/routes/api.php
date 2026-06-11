<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\CardPaymentApiController;
use App\Http\Controllers\Api\AccountController;
use App\Http\Controllers\Api\CryptoController;
use App\Http\Controllers\Api\KycController;
use App\Http\Controllers\Api\TransactionController;
use App\Http\Controllers\Api\AdminController;
use App\Http\Controllers\Api\BeneficiaryController;
use App\Http\Controllers\Api\CardController;
use App\Http\Controllers\Api\NotificationController;
use App\Http\Controllers\Api\QrController;
use App\Http\Controllers\Api\StatementController;
use App\Http\Controllers\Api\SupportController;
use App\Http\Controllers\Api\DepositController;
use App\Http\Controllers\Api\ScheduledWithdrawalController;
use App\Http\Controllers\Api\TransferController;
use App\Http\Middleware\EnsureUserIsAdmin;
use App\Http\Middleware\ForceJsonResponse;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
|
| Mobile Banking Application API
| All routes are prefixed with /api automatically.
|
| Authentication: Laravel Sanctum (token-based)
| Response format: JSON
|
*/

// Force JSON responses for all API routes
Route::middleware(ForceJsonResponse::class)->group(function () {

    /*
    |----------------------------------------------------------------------
    | Public Routes (No authentication required)
    |----------------------------------------------------------------------
    */
    Route::post('/register', [AuthController::class, 'register']);
    Route::post('/login', [AuthController::class, 'login']);

    // Health check
    Route::get('/health', function () {
        return response()->json([
            'success' => true,
            'message' => 'Banking API is operational.',
            'version' => '1.0.0',
            'timestamp' => now()->toISOString(),
        ]);
    });

    /*
    |----------------------------------------------------------------------
    | Protected Routes (Sanctum authentication required)
    |----------------------------------------------------------------------
    */
    Route::middleware('auth:sanctum')->group(function () {

        // Authentication
        Route::post('/logout', [AuthController::class, 'logout']);
        Route::get('/user', [AuthController::class, 'user']);
        Route::put('/profile', [AuthController::class, 'updateProfile']);
        Route::post('/profile/avatar', [AuthController::class, 'uploadAvatar']);
        Route::post('/fcm-token', [AuthController::class, 'updateFcmToken']);
        Route::post('/email/resend', [AuthController::class, 'resendVerification']);

        // Crypto wallets & trading
        Route::get('/crypto/wallets', [CryptoController::class, 'wallets']);
        Route::post('/crypto/buy', [CryptoController::class, 'buy']);
        Route::post('/crypto/sell', [CryptoController::class, 'sell']);
        Route::post('/crypto/send', [CryptoController::class, 'send']);
        Route::post('/crypto/swap', [CryptoController::class, 'swap']);
        Route::get('/crypto/transactions', [CryptoController::class, 'transactions']);

        // Identity verification (KYC)
        Route::get('/kyc/status', [KycController::class, 'status']);
        Route::post('/kyc/submit', [KycController::class, 'submit']);

        // Accounts & Balance
        Route::get('/balance', [AccountController::class, 'balance']);
        Route::get('/accounts', [AccountController::class, 'index']);

        // Transactions
        Route::get('/transactions', [TransactionController::class, 'index']);

        // Deposits
        Route::post('/deposits', [DepositController::class, 'store']);
        Route::post('/deposits/{reference}/confirm', [DepositController::class, 'confirm']);
        Route::post('/deposits/{reference}/cancel', [DepositController::class, 'cancel']);

        // Transfers, OTP validation and QR payment helpers
        Route::get('/transfers', [TransferController::class, 'index']);
        Route::post('/transfers', [TransferController::class, 'store']);
        Route::post('/transfers/verify', [TransferController::class, 'verify']);
        Route::post('/qr/generate', [QrController::class, 'generate']);
        Route::post('/qr/scan', [QrController::class, 'scan']);
        Route::post('/qr/pay', [QrController::class, 'pay']);

        // Beneficiaries
        Route::get('/beneficiaries', [BeneficiaryController::class, 'index']);
        Route::post('/beneficiaries', [BeneficiaryController::class, 'store']);
        Route::delete('/beneficiaries/{beneficiary}', [BeneficiaryController::class, 'destroy']);

        // Virtual cards and monthly statements
        Route::get('/cards', [CardController::class, 'index']);
        Route::post('/cards', [CardController::class, 'store']);
        Route::get('/cards/{card}/reveal', [CardController::class, 'reveal']);
        Route::patch('/cards/{card}/limit', [CardController::class, 'updateLimit']);
        Route::post('/cards/{card}/toggle', [CardController::class, 'toggle']);
        Route::delete('/cards/{card}', [CardController::class, 'destroy']);
        Route::get('/statements/monthly', [StatementController::class, 'monthly']);
        Route::get('/transactions/export', [StatementController::class, 'exportCsv']);

        // Card payment confirmation (e-commerce)
        Route::get('/card-payments/pending',              [CardPaymentApiController::class, 'pending']);
        Route::post('/card-payments/{reference}/confirm', [CardPaymentApiController::class, 'confirm']);
        Route::post('/card-payments/{reference}/decline', [CardPaymentApiController::class, 'decline']);

        // Notifications
        Route::get('/notifications', [NotificationController::class, 'index']);
        Route::post('/notifications/{id}/read', [NotificationController::class, 'markRead']);
        Route::post('/notifications/read-all', [NotificationController::class, 'markAllRead']);

        // Scheduled automatic withdrawals
        Route::get('/scheduled-withdrawals', [ScheduledWithdrawalController::class, 'index']);
        Route::post('/scheduled-withdrawals', [ScheduledWithdrawalController::class, 'store']);
        Route::patch('/scheduled-withdrawals/{scheduledWithdrawal}/toggle', [ScheduledWithdrawalController::class, 'toggle']);
        Route::delete('/scheduled-withdrawals/{scheduledWithdrawal}', [ScheduledWithdrawalController::class, 'destroy']);

        // Customer support chat
        Route::get('/support/ticket', [SupportController::class, 'getOrCreateTicket']);
        Route::post('/support/ticket/{ticketId}/messages', [SupportController::class, 'sendMessage']);

        /*
        |------------------------------------------------------------------
        | Admin Routes (Admin role required)
        |------------------------------------------------------------------
        */
        Route::middleware(EnsureUserIsAdmin::class)
            ->prefix('admin')
            ->group(function () {
                Route::get('/dashboard', [AdminController::class, 'dashboard']);
                Route::get('/transactions', [AdminController::class, 'transactions']);
                Route::get('/fraud-alerts', [AdminController::class, 'fraudAlerts']);
            });

    });
});
