<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\CardResource;
use App\Models\Card;
use App\Services\AuditService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class CardController extends Controller
{
    public function __construct(protected AuditService $auditService) {}

    public function index(Request $request): JsonResponse
    {
        $cards = Card::whereHas('account', fn ($query) => $query->where('user_id', $request->user()->id))
            ->latest()
            ->get();

        return response()->json([
            'success' => true,
            'data' => CardResource::collection($cards),
        ]);
    }

    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'account_id' => ['required', 'integer', 'exists:accounts,id'],
            'daily_limit' => ['nullable', 'numeric', 'min:10000'],
        ]);

        $account = $request->user()->accounts()->findOrFail($validated['account_id']);
        $card = Card::create([
            'account_id' => $account->id,
            'card_number' => '4' . str_pad((string) random_int(0, 999999999999999), 15, '0', STR_PAD_LEFT),
            'cvv' => (string) random_int(100, 999),
            'expiry_date' => now()->addYears(3)->toDateString(),
            'type' => 'virtual',
            'daily_limit' => $validated['daily_limit'] ?? 5000000,
            'is_blocked' => false,
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Carte virtuelle generee.',
            'data' => new CardResource($card),
        ], 201);
    }

    public function updateLimit(Request $request, Card $card): JsonResponse
    {
        abort_unless($card->account?->user_id === $request->user()->id, 403);

        $validated = $request->validate([
            'daily_limit' => ['required', 'numeric', 'min:10000', 'max:10000000'],
        ]);

        $card->update(['daily_limit' => $validated['daily_limit']]);

        return response()->json([
            'success' => true,
            'message' => 'Limite mise à jour.',
            'data'    => new CardResource($card),
        ]);
    }

    public function reveal(Request $request, Card $card): JsonResponse
    {
        abort_unless($card->account?->user_id === $request->user()->id, 403);

        $this->auditService->log($request->user()->id, 'card.revealed', [
            'card_id' => $card->id,
        ], 'warning', $request);

        $number = $card->card_number;
        $formatted = implode(' ', str_split(str_pad($number, 16, '0', STR_PAD_LEFT), 4));

        return response()->json([
            'success' => true,
            'data' => [
                'card_number' => $formatted,
                'cvv'         => $card->cvv,
                'expiry_date' => $card->expiry_date?->format('m/Y'),
            ],
        ]);
    }

    public function toggle(Request $request, Card $card): JsonResponse
    {
        abort_unless($card->account?->user_id === $request->user()->id, 403);
        $card->update(['is_blocked' => !$card->is_blocked]);

        $this->auditService->log($request->user()->id, 'card.toggled', [
            'card_id' => $card->id,
            'is_blocked' => $card->is_blocked,
        ], 'warning', $request);

        return response()->json([
            'success' => true,
            'message' => $card->is_blocked ? 'Carte bloquee.' : 'Carte debloquee.',
            'data' => new CardResource($card),
        ]);
    }

    public function destroy(Request $request, Card $card): JsonResponse
    {
        abort_unless($card->account?->user_id === $request->user()->id, 403);

        $this->auditService->log($request->user()->id, 'card.deleted', [
            'card_id' => $card->id,
        ], 'warning', $request);

        $card->delete();

        return response()->json([
            'success' => true,
            'message' => 'Carte supprimee.',
        ]);
    }
}
