<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('crypto_transactions', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained()->onDelete('cascade');
            $table->enum('type', ['buy', 'sell', 'send', 'receive']);
            $table->string('symbol', 10);
            $table->decimal('amount', 28, 18);
            $table->decimal('price_usd', 20, 8);
            $table->decimal('total_mga', 20, 2);
            $table->string('to_address', 200)->nullable();
            $table->string('from_address', 200)->nullable();
            $table->string('status', 20)->default('completed');
            $table->string('tx_hash', 100)->nullable();
            $table->timestamps();
            $table->index(['user_id', 'symbol']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('crypto_transactions');
    }
};
