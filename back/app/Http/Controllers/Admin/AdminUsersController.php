<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Account;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class AdminUsersController extends Controller
{
    public function index(Request $request)
    {
        $query = User::withCount(['accounts'])->with([
            'accounts' => fn($q) => $q->withCount('transactions'),
        ])->latest();

        if ($request->filled('search')) {
            $query->where(function ($q) use ($request) {
                $q->where('name', 'like', "%{$request->search}%")
                  ->orWhere('email', 'like', "%{$request->search}%")
                  ->orWhere('phone', 'like', "%{$request->search}%");
            });
        }
        if ($request->filled('status')) {
            $query->where('is_active', $request->status === 'active');
        }

        $users = $query->paginate(20);

        $stats = [
            'total'  => User::count(),
            'active' => User::where('is_active', true)->count(),
            'frozen' => User::where('is_active', false)->count(),
            'admin'  => User::where('role', 'admin')->count(),
        ];

        return view('admin.users', compact('users', 'stats'));
    }

    public function show(int $userId)
    {
        $user = User::with([
            'accounts.cards',
            'accounts' => fn($q) => $q->withCount('transactions'),
        ])->findOrFail($userId);

        $recentTransactions = \App\Models\Transaction::whereIn(
            'account_id', $user->accounts->pluck('id')
        )->with('account')->latest()->limit(10)->get();

        return view('admin.user_show', compact('user', 'recentTransactions'));
    }

    public function update(Request $request, int $userId)
    {
        $user = User::findOrFail($userId);

        if ($user->isAdmin()) {
            return back()->with('error', 'Impossible de modifier un admin.');
        }

        $validated = $request->validate([
            'name'  => 'required|string|max:255',
            'email' => 'required|email|unique:users,email,' . $userId,
            'phone' => 'nullable|string|max:30',
        ]);

        $user->update($validated);

        return back()->with('success', "Profil mis à jour : {$user->name}");
    }

    public function resetPassword(int $userId)
    {
        $user = User::findOrFail($userId);

        if ($user->isAdmin()) {
            return back()->with('error', 'Impossible de modifier un admin.');
        }

        $newPassword = 'Pass' . rand(1000, 9999) . '!';
        $user->update(['password' => Hash::make($newPassword)]);

        return back()->with('success', "Nouveau mot de passe : {$newPassword}");
    }

    public function destroy(int $userId)
    {
        $user = User::findOrFail($userId);

        if ($user->isAdmin()) {
            return back()->with('error', 'Impossible de supprimer un admin.');
        }

        $name = $user->name;
        $user->delete();

        return redirect()->route('admin.users')->with('success', "Utilisateur supprimé : {$name}");
    }

    public function toggle(Request $request, int $userId)
    {
        $user = User::findOrFail($userId);

        if ($user->isAdmin()) {
            return back()->with('error', 'Impossible de modifier un admin.');
        }

        $user->update(['is_active' => !$user->is_active]);

        $msg = $user->is_active ? "Compte activé : {$user->name}" : "Compte gelé : {$user->name}";
        return back()->with('success', $msg);
    }

    public function toggleAccount(int $userId, int $accountId)
    {
        $account = Account::where('user_id', $userId)->findOrFail($accountId);
        $newStatus = $account->status === 'active' ? 'suspended' : 'active';
        $account->update(['status' => $newStatus]);

        $msg = $newStatus === 'active' ? 'Compte bancaire activé' : 'Compte bancaire suspendu';
        return back()->with('success', $msg);
    }
}
