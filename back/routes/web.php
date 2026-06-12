<?php

use App\Http\Controllers\Admin\AdminCardsController;
use App\Http\Controllers\ShopController;
use App\Models\User;
use Illuminate\Http\Request;
use App\Http\Controllers\Admin\AdminExportController;
use App\Http\Controllers\Admin\AdminFraudController;
use App\Http\Controllers\Admin\AdminKycController;
use App\Http\Controllers\Admin\AdminSupportController;
use App\Http\Controllers\Admin\AdminTransactionController;
use App\Http\Controllers\Admin\AdminUsersController;
use App\Http\Controllers\Admin\DashboardController;
use App\Http\Middleware\AdminWebAuth;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return redirect()->route('admin.login');
});

// Email verification (user taps the signed URL from their email browser)
Route::get('/email/verify/{id}/{hash}', function (Request $request) {
    $user = User::findOrFail($request->route('id'));

    if (!hash_equals(sha1($user->getEmailForVerification()), (string) $request->route('hash'))) {
        abort(403, 'Lien de vérification invalide.');
    }

    if (!$request->hasValidSignature()) {
        abort(403, 'Ce lien de vérification a expiré.');
    }

    if (!$user->hasVerifiedEmail()) {
        $user->markEmailAsVerified();
    }

    return view('emails.verified');
})->middleware('signed')->name('verification.verify');

// Public e-commerce demo
Route::get('/shop',                    [ShopController::class, 'index'])->name('shop.index');
Route::post('/shop/checkout',          [ShopController::class, 'checkout'])->name('shop.checkout');
Route::get('/shop/waiting/{reference}',[ShopController::class, 'waiting'])->name('shop.waiting');
Route::get('/shop/status/{reference}', [ShopController::class, 'status'])->name('shop.status');

// Admin authentication
Route::get('/admin/login', [DashboardController::class, 'showLogin'])->name('admin.login');
Route::post('/admin/login', [DashboardController::class, 'login'])->name('admin.login.post');
Route::post('/admin/logout', [DashboardController::class, 'logout'])->name('admin.logout');

// Protected admin panel
Route::prefix('admin')->middleware(AdminWebAuth::class)->name('admin.')->group(function () {

    Route::get('/dashboard', [DashboardController::class, 'index'])->name('dashboard');

    // Transactions
    Route::get('/transactions', [AdminTransactionController::class, 'index'])->name('transactions');

    // Users
    Route::get('/users', [AdminUsersController::class, 'index'])->name('users');
    Route::get('/users/{user}', [AdminUsersController::class, 'show'])->name('users.show');
    Route::patch('/users/{user}', [AdminUsersController::class, 'update'])->name('users.update');
    Route::delete('/users/{user}', [AdminUsersController::class, 'destroy'])->name('users.destroy');
    Route::patch('/users/{user}/toggle', [AdminUsersController::class, 'toggle'])->name('users.toggle');
    Route::post('/users/{user}/reset-password', [AdminUsersController::class, 'resetPassword'])->name('users.reset-password');
    Route::patch('/users/{user}/accounts/{account}/toggle', [AdminUsersController::class, 'toggleAccount'])->name('users.accounts.toggle');

    // Cards
    Route::get('/cards', [AdminCardsController::class, 'index'])->name('cards');
    Route::patch('/cards/{card}/toggle', [AdminCardsController::class, 'toggle'])->name('cards.toggle');
    Route::get('/cards/test', [AdminCardsController::class, 'testPayment'])->name('cards.test');
    Route::post('/cards/test', [AdminCardsController::class, 'processTestPayment'])->name('cards.test.process');
    Route::patch('/cards/{card}/limit', [AdminCardsController::class, 'updateLimit'])->name('cards.limit');

    // Support
    Route::get('/support', [AdminSupportController::class, 'index'])->name('support.index');
    Route::get('/support/{ticket}', [AdminSupportController::class, 'show'])->name('support.show');
    Route::post('/support/{ticket}/reply', [AdminSupportController::class, 'reply'])->name('support.reply');
    Route::post('/support/{ticket}/close', [AdminSupportController::class, 'close'])->name('support.close');

    // KYC / Identity Verification
    Route::get('/kyc', [AdminKycController::class, 'index'])->name('kyc');
    Route::get('/kyc/{user}', [AdminKycController::class, 'show'])->name('kyc.show');
    Route::post('/kyc/{user}/approve', [AdminKycController::class, 'approve'])->name('kyc.approve');
    Route::post('/kyc/{user}/reject', [AdminKycController::class, 'reject'])->name('kyc.reject');

    // Fraud
    Route::get('/fraud', [AdminFraudController::class, 'index'])->name('fraud');

    // Export
    Route::get('/export', [AdminExportController::class, 'index'])->name('export');
    Route::get('/export/download', [AdminExportController::class, 'download'])->name('export.download');
});
