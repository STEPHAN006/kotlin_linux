<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AdminWebAuthController;
use App\Http\Controllers\AdminWebDashboardController;
use App\Http\Middleware\EnsureUserIsAdmin;

// Redirect root to admin login
Route::get('/', function () {
    return redirect()->route('admin.login');
});

Route::prefix('admin')->name('admin.')->group(function () {
    // Guest Routes
    Route::middleware('guest:web')->group(function () {
        Route::get('/login', [AdminWebAuthController::class, 'showLoginForm'])->name('login');
        Route::post('/login', [AdminWebAuthController::class, 'login']);
    });

    // Authenticated Admin Routes
    Route::middleware(['auth:web', EnsureUserIsAdmin::class])->group(function () {
        Route::post('/logout', [AdminWebAuthController::class, 'logout'])->name('logout');
        
        Route::get('/dashboard', [AdminWebDashboardController::class, 'index'])->name('dashboard');
        Route::get('/users', [AdminWebDashboardController::class, 'users'])->name('users');
        Route::get('/transactions', [AdminWebDashboardController::class, 'transactions'])->name('transactions');
    });
});
