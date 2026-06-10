<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Card;
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
}
