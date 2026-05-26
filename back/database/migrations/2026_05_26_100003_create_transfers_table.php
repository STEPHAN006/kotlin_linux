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
        Schema::create('transfers', function (Blueprint $table) {
            $table->id();
            $table->foreignId('sender_account_id')->constrained('accounts')->onDelete('cascade');
            $table->foreignId('receiver_account_id')->constrained('accounts')->onDelete('cascade');
            $table->decimal('amount', 15, 2);
            $table->enum('status', ['pending', 'completed', 'failed', 'cancelled'])->default('pending');
            $table->boolean('otp_verified')->default(false);
            $table->string('reference', 30)->unique();
            $table->string('note')->nullable();
            $table->enum('channel', ['internal', 'mvola', 'airtel_money', 'orange_money', 'bank_transfer'])->default('internal');
            $table->json('metadata')->nullable(); // Future: mobile money transaction IDs
            $table->timestamps();

            $table->index(['sender_account_id', 'status']);
            $table->index(['receiver_account_id', 'status']);
            $table->index('created_at');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('transfers');
    }
};
