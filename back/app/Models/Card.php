<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Card extends Model
{
    use HasFactory;

    /**
     * The attributes that are mass assignable.
     *
     * @var list<string>
     */
    protected $fillable = [
        'account_id',
        'card_number',
        'cvv',
        'expiry_date',
        'is_blocked',
        'type',
        'daily_limit',
    ];

    /**
     * Get the attributes that should be cast.
     *
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'card_number' => 'encrypted',
            'cvv' => 'encrypted',
            'expiry_date' => 'date',
            'is_blocked' => 'boolean',
            'daily_limit' => 'decimal:2',
        ];
    }

    /**
     * Get the account that owns this card.
     */
    public function account(): BelongsTo
    {
        return $this->belongsTo(Account::class);
    }

    /**
     * Get masked card number (last 4 digits only).
     */
    public function getMaskedNumberAttribute(): string
    {
        return '**** **** **** ' . substr($this->card_number, -4);
    }
}
