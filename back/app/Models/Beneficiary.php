<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Beneficiary extends Model
{
    use HasFactory;

    /**
     * The attributes that are mass assignable.
     *
     * @var list<string>
     */
    protected $fillable = [
        'user_id',
        'name',
        'bank_name',
        'account_number',
        'phone',
        'channel',
        'is_verified',
    ];

    /**
     * Get the attributes that should be cast.
     *
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'account_number' => 'encrypted',
            'is_verified' => 'boolean',
        ];
    }

    /**
     * Get the user that owns this beneficiary.
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
