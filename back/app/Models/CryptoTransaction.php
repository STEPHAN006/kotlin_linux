<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class CryptoTransaction extends Model
{
    protected $fillable = [
        'user_id', 'type', 'symbol', 'amount',
        'price_usd', 'total_mga', 'to_address',
        'from_address', 'status', 'tx_hash',
    ];

    protected function casts(): array
    {
        return [
            'amount'    => 'decimal:18',
            'price_usd' => 'decimal:8',
            'total_mga' => 'decimal:2',
        ];
    }

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
