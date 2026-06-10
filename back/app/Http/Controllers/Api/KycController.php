<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

class KycController extends Controller
{
    public function status(Request $request): JsonResponse
    {
        $user = $request->user();

        return response()->json([
            'success' => true,
            'data' => [
                'status' => $user->kyc_status ?? 'none',
                'rejection_reason' => $user->kyc_rejection_reason,
                'submitted_at' => $user->kyc_submitted_at?->toISOString(),
            ],
        ]);
    }

    public function submit(Request $request): JsonResponse
    {
        $request->validate([
            'cin_full_name' => ['required', 'string', 'max:255'],
            'cin_recto'     => ['required', 'image', 'mimes:jpeg,png,jpg', 'max:8192'],
            'cin_verso'     => ['required', 'image', 'mimes:jpeg,png,jpg', 'max:8192'],
        ]);

        $user = $request->user();

        if (in_array($user->kyc_status, ['pending', 'approved'])) {
            return response()->json([
                'success' => false,
                'message' => 'Vérification déjà soumise ou approuvée.',
            ], 422);
        }

        // Remove previous documents if re-submitting after rejection
        if ($user->cin_recto) Storage::disk('public')->delete($user->cin_recto);
        if ($user->cin_verso) Storage::disk('public')->delete($user->cin_verso);

        $rectoPath = $request->file('cin_recto')->store('kyc', 'public');
        $versoPath = $request->file('cin_verso')->store('kyc', 'public');

        $user->update([
            'kyc_status'           => 'pending',
            'cin_full_name'        => $request->cin_full_name,
            'cin_recto'            => $rectoPath,
            'cin_verso'            => $versoPath,
            'kyc_submitted_at'     => now(),
            'kyc_reviewed_at'      => null,
            'kyc_rejection_reason' => null,
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Documents soumis avec succès. Votre identité est en cours de vérification.',
            'data'    => ['status' => 'pending'],
        ]);
    }
}
