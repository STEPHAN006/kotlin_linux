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
        Schema::create('beneficiaries', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained()->onDelete('cascade');
            $table->string('name');
            $table->string('bank_name');
            $table->text('account_number'); // Encrypted at application level
            $table->string('phone', 20)->nullable();
            $table->enum('channel', ['bank', 'mvola', 'airtel_money', 'orange_money'])->default('bank');
            $table->boolean('is_verified')->default(false);
            $table->timestamps();

            $table->index(['user_id', 'channel']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('beneficiaries');
    }
};
