<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ScheduledWithdrawal extends Model
{
    protected $fillable = [
        'user_id',
        'sender_account_id',
        'beneficiary_id',
        'amount',
        'note',
        'frequency_days',
        'next_run_at',
        'last_run_at',
        'run_count',
        'is_active',
    ];

    protected function casts(): array
    {
        return [
            'amount'       => 'decimal:2',
            'is_active'    => 'boolean',
            'next_run_at'  => 'datetime',
            'last_run_at'  => 'datetime',
        ];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function senderAccount(): BelongsTo
    {
        return $this->belongsTo(Account::class, 'sender_account_id');
    }

    public function beneficiary(): BelongsTo
    {
        return $this->belongsTo(Beneficiary::class);
    }
}
