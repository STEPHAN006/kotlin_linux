<?php

namespace App\Services;

use App\Models\User;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;

class AuthService
{
    /**
     * Register a new user and return an API token.
     *
     * @param array $data Validated registration data
     * @return array{user: User, token: string}
     */
    public function register(array $data): array
    {
        $user = User::create([
            'name' => $data['name'],
            'email' => $data['email'],
            'phone' => $data['phone'] ?? null,
            'password' => $data['password'], // Hashed by model cast
        ]);

        $token = $user->createToken('banking-app-token', ['*'])->plainTextToken;

        return [
            'user' => $user,
            'token' => $token,
        ];
    }

    /**
     * Authenticate a user and return an API token.
     *
     * @param array $data Validated login credentials
     * @return array{user: User, token: string}
     * @throws ValidationException
     */
    public function login(array $data): array
    {
        $user = User::where('email', $data['email'])->first();

        if (!$user || !Hash::check($data['password'], $user->password)) {
            throw ValidationException::withMessages([
                'email' => ['The provided credentials are incorrect.'],
            ]);
        }

        if (!$user->is_active) {
            throw ValidationException::withMessages([
                'email' => ['Your account has been deactivated. Please contact support.'],
            ]);
        }

        // Revoke all existing tokens for security (single-device policy)
        $user->tokens()->delete();

        $token = $user->createToken('banking-app-token', ['*'])->plainTextToken;

        return [
            'user' => $user->load('accounts'),
            'token' => $token,
        ];
    }

    /**
     * Logout the current user by revoking their active token.
     */
    public function logout(User $user): void
    {
        // Revoke the token used for this request
        $user->currentAccessToken()->delete();
    }
}
