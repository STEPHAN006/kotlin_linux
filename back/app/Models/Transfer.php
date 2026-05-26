<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Transfer extends Model
{
    use HasFactory;

    /**
     * The attributes that are mass assignable.
     *
     * @var list<string>
     */
    protected $fillable = [
        'sender_account_id',
        'receiver_account_id',
        'amount',
        'status',
        'otp_verified',
        'reference',
        'note',
        'channel',
        'metadata',
    ];

    /**
     * Get the attributes that should be cast.
     *
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'amount' => 'decimal:2',
            'otp_verified' => 'boolean',
            'metadata' => 'array',
        ];
    }

    /**
     * Get the sender account.
     */
    public function senderAccount(): BelongsTo
    {
        return $this->belongsTo(Account::class, 'sender_account_id');
    }

    /**
     * Get the receiver account.
     */
    public function receiverAccount(): BelongsTo
    {
        return $this->belongsTo(Account::class, 'receiver_account_id');
    }

    /**
     * Check if the transfer is completed.
     */
    public function isCompleted(): bool
    {
        return $this->status === 'completed';
    }
}
