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
        Schema::create('accounts', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained()->onDelete('cascade');
            $table->text('account_number'); // Encrypted at application level
            $table->decimal('balance', 15, 2)->default(0.00);
            $table->string('currency', 3)->default('MGA');
            $table->enum('status', ['active', 'inactive', 'frozen', 'closed'])->default('active');
            $table->enum('type', ['checking', 'savings', 'business'])->default('checking');
            $table->timestamps();

            $table->index(['user_id', 'status']);
            $table->index('currency');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('accounts');
    }
};
