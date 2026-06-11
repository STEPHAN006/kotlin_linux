<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('deposits', function (Blueprint $table) {
            $table->id();
            $table->foreignId('account_id')->constrained()->onDelete('cascade');
            $table->decimal('amount', 15, 2);
            $table->enum('method', ['mvola', 'orange_money', 'airtel_money', 'cash']);
            $table->string('phone', 20)->nullable();
            $table->string('reference', 30)->unique();
            $table->enum('status', ['pending', 'completed', 'cancelled'])->default('pending')->index();
            $table->decimal('balance_after', 15, 2)->nullable();
            $table->json('metadata')->nullable();
            $table->timestamps();

            $table->index(['account_id', 'status']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('deposits');
    }
};
