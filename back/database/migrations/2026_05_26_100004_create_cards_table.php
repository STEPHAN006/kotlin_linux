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
        Schema::create('cards', function (Blueprint $table) {
            $table->id();
            $table->foreignId('account_id')->constrained()->onDelete('cascade');
            $table->text('card_number');    // Encrypted at application level
            $table->text('cvv');            // Encrypted at application level
            $table->date('expiry_date');
            $table->boolean('is_blocked')->default(false);
            $table->enum('type', ['visa', 'mastercard', 'virtual'])->default('visa');
            $table->decimal('daily_limit', 15, 2)->default(5000000.00);
            $table->timestamps();

            $table->index(['account_id', 'is_blocked']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('cards');
    }
};
