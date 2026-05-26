<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\LoginRequest;
use App\Http\Requests\RegisterRequest;
use App\Http\Resources\UserResource;
use App\Services\AuthService;
use App\Services\AccountService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class AuthController extends Controller
{
    public function __construct(
        protected AuthService $authService,
        protected AccountService $accountService
    ) {}

    /**
     * Register a new user.
     *
     * POST /api/register
     */
    public function register(RegisterRequest $request): JsonResponse
    {
        $result = $this->authService->register($request->validated());

        // Create a default checking account for the new user
        $this->accountService->createAccount($result['user']->id, [
            'initial_balance' => 0,
            'currency' => 'MGA',
            'type' => 'checking',
        ]);

        $result['user']->load('accounts');

        return response()->json([
            'success' => true,
            'message' => 'Account created successfully.',
            'data' => [
                'user' => new UserResource($result['user']),
                'token' => $result['token'],
                'token_type' => 'Bearer',
            ],
        ], 201);
    }

    /**
     * Login an existing user.
     *
     * POST /api/login
     */
    public function login(LoginRequest $request): JsonResponse
    {
        $result = $this->authService->login($request->validated());

        return response()->json([
            'success' => true,
            'message' => 'Login successful.',
            'data' => [
                'user' => new UserResource($result['user']),
                'token' => $result['token'],
                'token_type' => 'Bearer',
            ],
        ]);
    }

    /**
     * Logout the current user.
     *
     * POST /api/logout
     */
    public function logout(Request $request): JsonResponse
    {
        $this->authService->logout($request->user());

        return response()->json([
            'success' => true,
            'message' => 'Logged out successfully.',
        ]);
    }

    /**
     * Get the authenticated user's profile.
     *
     * GET /api/user
     */
    public function user(Request $request): JsonResponse
    {
        $user = $request->user()->load('accounts.cards', 'beneficiaries');

        return response()->json([
            'success' => true,
            'data' => new UserResource($user),
        ]);
    }
}
