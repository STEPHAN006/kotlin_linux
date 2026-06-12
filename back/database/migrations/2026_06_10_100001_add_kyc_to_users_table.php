<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->string('kyc_status')->default('none')->after('is_active');
            $table->string('cin_full_name')->nullable()->after('kyc_status');
            $table->string('cin_recto')->nullable()->after('cin_full_name');
            $table->string('cin_verso')->nullable()->after('cin_recto');
            $table->timestamp('kyc_submitted_at')->nullable()->after('cin_verso');
            $table->timestamp('kyc_reviewed_at')->nullable()->after('kyc_submitted_at');
            $table->text('kyc_rejection_reason')->nullable()->after('kyc_reviewed_at');
        });
    }

    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn([
                'kyc_status', 'cin_full_name', 'cin_recto', 'cin_verso',
                'kyc_submitted_at', 'kyc_reviewed_at', 'kyc_rejection_reason',
            ]);
        });
    }
};
