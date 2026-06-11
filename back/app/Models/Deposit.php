<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Deposit extends Model
{
    protected $fillable = [
        'account_id',
        'amount',
        'method',
        'phone',
        'reference',
        'status',
        'balance_after',
        'metadata',
    ];

    protected $casts = [
        'amount'        => 'float',
        'balance_after' => 'float',
        'metadata'      => 'array',
    ];

    public function account(): BelongsTo
    {
        return $this->belongsTo(Account::class);
    }
}
