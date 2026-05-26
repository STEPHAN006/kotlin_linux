<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\AccountController;
use App\Http\Controllers\Api\TransactionController;
use App\Http\Controllers\Api\AdminController;
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

        // Accounts & Balance
        Route::get('/balance', [AccountController::class, 'balance']);
        Route::get('/accounts', [AccountController::class, 'index']);

        // Transactions
        Route::get('/transactions', [TransactionController::class, 'index']);

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

        /*
        |------------------------------------------------------------------
        | Future Endpoints (Placeholder structure)
        |------------------------------------------------------------------
        |
        | These route groups are prepared for future feature integration:
        |
        | - POST /api/transfers          -> Initiate transfer
        | - POST /api/transfers/verify    -> OTP verification
        | - POST /api/qr/generate        -> Generate QR payment code
        | - POST /api/qr/scan            -> Process QR payment
        | - POST /api/mobile-money/mvola  -> MVola integration
        | - POST /api/mobile-money/airtel -> Airtel Money integration
        | - POST /api/mobile-money/orange -> Orange Money integration
        | - POST /api/biometric/verify    -> Biometric authentication
        |
        */
    });
});
