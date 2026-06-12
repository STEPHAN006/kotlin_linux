<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Card;
use App\Models\Transaction;
use Illuminate\Http\Request;

class AdminCardsController extends Controller
{
    public function index(Request $request)
    {
        $query = Card::with(['account.user'])->latest();

        if ($request->filled('search')) {
            $query->whereHas('account.user', function ($q) use ($request) {
                $q->where('name', 'like', "%{$request->search}%")
                  ->orWhere('email', 'like', "%{$request->search}%");
            });
        }

        if ($request->filled('status')) {
            $query->where('is_blocked', $request->status === 'blocked');
        }

        if ($request->filled('type')) {
            $query->where('type', $request->type);
        }

        $cards = $query->paginate(25);

        $stats = [
            'total'   => Card::count(),
            'active'  => Card::where('is_blocked', false)->count(),
            'blocked' => Card::where('is_blocked', true)->count(),
            'virtual' => Card::where('type', 'virtual')->count(),
        ];

        return view('admin.cards', compact('cards', 'stats'));
    }

    public function toggle(int $cardId)
    {
        $card = Card::with('account.user')->findOrFail($cardId);
        $card->update(['is_blocked' => !$card->is_blocked]);

        $msg = $card->is_blocked
            ? "Carte bloquée ({$card->masked_number})"
            : "Carte débloquée ({$card->masked_number})";

        return back()->with('success', $msg);
    }

    public function updateLimit(Request $request, int $cardId)
    {
        $card = Card::findOrFail($cardId);

        $validated = $request->validate([
            'daily_limit' => 'required|numeric|min:0|max:10000000',
        ]);

        $card->update($validated);

        return back()->with('success', "Limite journalière mise à jour : " . number_format($validated['daily_limit'], 0, ',', ' ') . " MGA");
    }

    public function testPayment(Request $request)
    {
        $virtualCards = Card::with('account.user')
            ->where('type', 'virtual')
            ->latest()
            ->get();

        $history = session('card_test_history', []);

        return view('admin.card_test', compact('virtualCards', 'history'));
    }

    public function processTestPayment(Request $request)
    {
        $validated = $request->validate([
            'card_id'  => 'required|exists:cards,id',
            'merchant' => 'required|string|max:100',
            'category' => 'required|string',
            'amount'   => 'required|numeric|min:100|max:50000000',
        ]);

        $card   = Card::with('account.user')->findOrFail($validated['card_id']);
        $amount = (float) $validated['amount'];
        $now    = now();

        // Evaluate result
        if ($card->is_blocked) {
            $status = 'declined';
            $reason = 'Carte bloquée';
            $code   = 'DO_NOT_HONOR';
        } elseif ($card->expiry_date && $card->expiry_date->isPast()) {
            $status = 'declined';
            $reason = 'Carte expirée';
            $code   = 'EXPIRED_CARD';
        } elseif ($amount > $card->daily_limit) {
            $status = 'declined';
            $reason = 'Limite journalière dépassée (' . number_format($card->daily_limit, 0, ',', ' ') . ' MGA max)';
            $code   = 'INSUFFICIENT_FUNDS';
        } else {
            $status = 'approved';
            $reason = 'Paiement autorisé';
            $code   = 'APPROVED';
        }

        $entry = [
            'at'         => $now->format('d/m/Y H:i:s'),
            'card_masked'=> $card->masked_number,
            'holder'     => $card->account?->user?->name ?? '—',
            'merchant'   => $validated['merchant'],
            'category'   => $validated['category'],
            'amount'     => $amount,
            'status'     => $status,
            'reason'     => $reason,
            'code'       => $code,
            'auth_code'  => $status === 'approved' ? strtoupper(substr(md5(uniqid()), 0, 6)) : null,
        ];

        $history = array_slice(array_merge([$entry], session('card_test_history', [])), 0, 10);
        session(['card_test_history' => $history]);

        return redirect()->route('admin.cards.test')
            ->with('test_result', $entry);
    }
}
