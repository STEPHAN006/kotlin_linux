<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    public function up(): void
    {
        // SQLite n'impose pas de contrainte enum — MODIFY COLUMN n'existe pas non plus.
        // En MySQL/MariaDB, on étend l'enum pour autoriser 'swap'.
        if (config('database.default') !== 'sqlite') {
            DB::statement("ALTER TABLE crypto_transactions MODIFY COLUMN type ENUM('buy','sell','send','receive','swap') NOT NULL");
        }
    }

    public function down(): void
    {
        if (config('database.default') !== 'sqlite') {
            DB::statement("ALTER TABLE crypto_transactions MODIFY COLUMN type ENUM('buy','sell','send','receive') NOT NULL");
        }
    }
};
