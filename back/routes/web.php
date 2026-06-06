<?php

use App\Http\Controllers\AdminSupportController;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return view('welcome');
});

// Admin panel
Route::get('/admin', fn() => redirect()->route('admin.login'));
Route::get('/admin/login', [AdminSupportController::class, 'loginForm'])->name('admin.login');
Route::post('/admin/login', [AdminSupportController::class, 'login'])->name('admin.login.post');
Route::post('/admin/logout', [AdminSupportController::class, 'logout'])->name('admin.logout');

Route::prefix('admin')->group(function () {
    Route::get('/dashboard', [AdminSupportController::class, 'adminDashboard'])->name('admin.dashboard');
    Route::get('/transactions-list', [AdminSupportController::class, 'adminTransactions'])->name('admin.transactions');
    Route::get('/fraud-dashboard', [AdminSupportController::class, 'adminFraud'])->name('admin.fraud');
    Route::get('/support', [AdminSupportController::class, 'index'])->name('admin.support.index');
    Route::get('/support/{id}', [AdminSupportController::class, 'show'])->name('admin.support.show');
    Route::post('/support/{id}/reply', [AdminSupportController::class, 'reply'])->name('admin.support.reply');
    Route::post('/support/{id}/close', [AdminSupportController::class, 'close'])->name('admin.support.close');
});
