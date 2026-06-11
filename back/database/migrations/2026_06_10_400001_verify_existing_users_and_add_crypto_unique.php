<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        // Mark all pre-existing users as email-verified so they keep full access
        DB::table('users')
            ->whereNull('email_verified_at')
            ->update(['email_verified_at' => DB::raw('created_at')]);

        // Unique constraint on crypto_wallets (user_id, symbol) prevents duplicate wallets
        Schema::table('crypto_wallets', function (Blueprint $table) {
            $table->unique(['user_id', 'symbol'], 'crypto_wallets_user_symbol_unique');
        });
    }

    public function down(): void
    {
        Schema::table('crypto_wallets', function (Blueprint $table) {
            $table->dropUnique('crypto_wallets_user_symbol_unique');
        });
    }
};
