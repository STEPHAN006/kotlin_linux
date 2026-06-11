<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\ScheduledWithdrawal;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class ScheduledWithdrawalController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $items = ScheduledWithdrawal::where('user_id', $request->user()->id)
            ->with('beneficiary')
            ->latest()
            ->get()
            ->map(fn ($sw) => $this->format($sw));

        return response()->json(['success' => true, 'data' => $items]);
    }

    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'sender_account_id' => ['required', 'integer'],
            'beneficiary_id'    => ['required', 'integer'],
            'amount'            => ['required', 'numeric', 'min:1000'],
            'note'              => ['nullable', 'string', 'max:200'],
            'frequency_days'    => ['required', 'integer', 'in:1,7,14,30'],
        ]);

        // Ownership checks
        $account = $request->user()->accounts()->findOrFail($validated['sender_account_id']);
        $benef   = $request->user()->beneficiaries()->findOrFail($validated['beneficiary_id']);

        $sw = ScheduledWithdrawal::create([
            'user_id'           => $request->user()->id,
            'sender_account_id' => $account->id,
            'beneficiary_id'    => $benef->id,
            'amount'            => $validated['amount'],
            'note'              => $validated['note'] ?? null,
            'frequency_days'    => $validated['frequency_days'],
            'next_run_at'       => now()->addDays($validated['frequency_days']),
            'is_active'         => true,
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Retrait automatique programmé.',
            'data'    => $this->format($sw->load('beneficiary')),
        ], 201);
    }

    public function toggle(Request $request, ScheduledWithdrawal $scheduledWithdrawal): JsonResponse
    {
        abort_unless($scheduledWithdrawal->user_id === $request->user()->id, 403);
        $scheduledWithdrawal->update(['is_active' => !$scheduledWithdrawal->is_active]);

        return response()->json([
            'success' => true,
            'message' => $scheduledWithdrawal->is_active ? 'Retrait activé.' : 'Retrait suspendu.',
            'data'    => $this->format($scheduledWithdrawal->load('beneficiary')),
        ]);
    }

    public function destroy(Request $request, ScheduledWithdrawal $scheduledWithdrawal): JsonResponse
    {
        abort_unless($scheduledWithdrawal->user_id === $request->user()->id, 403);
        $scheduledWithdrawal->delete();

        return response()->json(['success' => true, 'message' => 'Retrait automatique supprimé.']);
    }

    private function format(ScheduledWithdrawal $sw): array
    {
        return [
            'id'             => $sw->id,
            'amount'         => (float) $sw->amount,
            'note'           => $sw->note,
            'frequency_days' => $sw->frequency_days,
            'next_run_at'    => $sw->next_run_at?->toISOString(),
            'last_run_at'    => $sw->last_run_at?->toISOString(),
            'run_count'      => $sw->run_count,
            'is_active'      => $sw->is_active,
            'beneficiary'    => $sw->beneficiary ? [
                'id'       => $sw->beneficiary->id,
                'name'     => $sw->beneficiary->name,
                'bank_name' => $sw->beneficiary->bank_name,
                'channel'  => $sw->beneficiary->channel,
            ] : null,
            'created_at'     => $sw->created_at?->toISOString(),
        ];
    }
}
