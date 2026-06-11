<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\UserNotification;
use Illuminate\Http\Request;

class AdminKycController extends Controller
{
    public function index(Request $request)
    {
        $query = User::where('kyc_status', '!=', 'none')->where('role', 'user');

        if ($request->filled('status')) {
            $query->where('kyc_status', $request->status);
        }

        $users = $query->latest('kyc_submitted_at')->paginate(20)->withQueryString();

        $stats = [
            'pending'  => User::where('kyc_status', 'pending')->count(),
            'approved' => User::where('kyc_status', 'approved')->count(),
            'rejected' => User::where('kyc_status', 'rejected')->count(),
        ];

        return view('admin.kyc', compact('users', 'stats'));
    }

    public function show(User $user)
    {
        abort_if($user->kyc_status === 'none', 404);
        return view('admin.kyc_show', compact('user'));
    }

    public function approve(User $user)
    {
        abort_if(!in_array($user->kyc_status, ['pending', 'rejected']), 422);

        $user->update([
            'kyc_status'      => 'approved',
            'kyc_reviewed_at' => now(),
        ]);

        UserNotification::create_for(
            $user->id,
            'Compte vérifié',
            'Votre identité a été vérifiée avec succès. Vous avez maintenant accès à toutes les fonctionnalités de SCpay.'
        );

        return redirect()->route('admin.kyc')
            ->with('success', "KYC approuvé pour {$user->name}.");
    }

    public function reject(Request $request, User $user)
    {
        $request->validate(['reason' => ['required', 'string', 'max:500']]);
        abort_if(!in_array($user->kyc_status, ['pending', 'approved']), 422);

        $user->update([
            'kyc_status'           => 'rejected',
            'kyc_reviewed_at'      => now(),
            'kyc_rejection_reason' => $request->reason,
        ]);

        UserNotification::create_for(
            $user->id,
            'Vérification refusée',
            "Votre dossier KYC a été refusé. Motif : {$request->reason}. Vous pouvez re-soumettre vos documents."
        );

        return redirect()->route('admin.kyc')
            ->with('success', "KYC refusé pour {$user->name}.");
    }
}
