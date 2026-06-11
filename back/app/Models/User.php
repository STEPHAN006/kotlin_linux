<?php

namespace App\Models;

use Database\Factories\UserFactory;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable implements MustVerifyEmail
{
    /** @use HasFactory<UserFactory> */
    use HasFactory, HasApiTokens, Notifiable;

    /**
     * The attributes that are mass assignable.
     *
     * @var list<string>
     */
    protected $fillable = [
        'name',
        'email',
        'phone',
        'avatar',
        'password',
        'role',
        'is_active',
        'kyc_status',
        'cin_full_name',
        'cin_recto',
        'cin_verso',
        'kyc_submitted_at',
        'kyc_reviewed_at',
        'kyc_rejection_reason',
        'fcm_token',
    ];

    /**
     * The attributes that should be hidden for serialization.
     *
     * @var list<string>
     */
    protected $hidden = [
        'password',
        'remember_token',
    ];

    /**
     * Get the attributes that should be cast.
     *
     * @return array<string, string>
     */
    protected function casts(): array
    {
        return [
            'email_verified_at' => 'datetime',
            'password' => 'hashed',
            'is_active' => 'boolean',
            'kyc_submitted_at' => 'datetime',
            'kyc_reviewed_at' => 'datetime',
        ];
    }

    /**
     * Check if the user is an admin.
     */
    public function getAvatarUrlAttribute(): ?string
    {
        if (!$this->avatar) return null;
        return url('storage/' . $this->avatar);
    }

    public function getCinRectoUrlAttribute(): ?string
    {
        if (!$this->cin_recto) return null;
        return url('storage/' . $this->cin_recto);
    }

    public function getCinVersoUrlAttribute(): ?string
    {
        if (!$this->cin_verso) return null;
        return url('storage/' . $this->cin_verso);
    }

    public function isAdmin(): bool
    {
        return $this->role === 'admin';
    }

    /**
     * Get the accounts belonging to this user.
     */
    public function accounts(): HasMany
    {
        return $this->hasMany(Account::class);
    }

    /**
     * Get the beneficiaries belonging to this user.
     */
    public function beneficiaries(): HasMany
    {
        return $this->hasMany(Beneficiary::class);
    }
}
