<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('transactions', function (Blueprint $table) {
            $table->id();
            $table->foreignId('account_id')->constrained()->onDelete('cascade');
            $table->enum('type', ['credit', 'debit'])->index();
            $table->decimal('amount', 15, 2);
            $table->string('category', 50)->nullable()->index();
            $table->string('description')->nullable();
            $table->string('reference', 30)->unique();
            $table->decimal('balance_after', 15, 2)->nullable();
            $table->json('metadata')->nullable(); // Future: store OTP, QR, mobile money info
            $table->timestamps();

            $table->index(['account_id', 'type']);
            $table->index(['account_id', 'created_at']);
            $table->index('created_at');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('transactions');
    }
};
