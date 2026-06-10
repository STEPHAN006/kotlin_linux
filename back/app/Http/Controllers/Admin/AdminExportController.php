<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Transaction;
use App\Models\User;
use Illuminate\Http\Request;

class AdminExportController extends Controller
{
    public function index()
    {
        return view('admin.export');
    }

    public function download(Request $request)
    {
        $format = $request->get('format', 'transactions');

        if ($format === 'users') {
            return $this->exportUsers();
        }

        return $this->exportTransactions($request);
    }

    private function exportTransactions(Request $request)
    {
        $query = Transaction::with('account.user')->orderByDesc('created_at');

        if ($request->filled('date_from')) $query->whereDate('created_at', '>=', $request->date_from);
        if ($request->filled('date_to'))   $query->whereDate('created_at', '<=', $request->date_to);
        if ($request->filled('type'))      $query->where('type', $request->type);

        $transactions = $query->get();

        $bom = "\xEF\xBB\xBF";
        $headers = ['ID', 'Référence', 'Type', 'Montant (MGA)', 'Description', 'Compte', 'Utilisateur', 'Date'];

        $lines = [$bom . implode(';', $headers)];
        foreach ($transactions as $t) {
            $lines[] = implode(';', [
                $t->id,
                $t->reference,
                $t->type,
                number_format($t->amount, 2, '.', ''),
                str_replace(';', ',', $t->description ?? ''),
                $t->account?->account_number ?? '',
                $t->account?->user?->name ?? '',
                $t->created_at->format('Y-m-d H:i:s'),
            ]);
        }

        return response(implode("\n", $lines), 200, [
            'Content-Type'        => 'text/csv; charset=UTF-8',
            'Content-Disposition' => 'attachment; filename="transactions_' . now()->format('Y-m-d') . '.csv"',
        ]);
    }

    private function exportUsers()
    {
        $users = User::with('accounts')->where('role', 'user')->get();

        $bom     = "\xEF\xBB\xBF";
        $headers = ['ID', 'Nom', 'Email', 'Téléphone', 'Statut', 'KYC', 'Comptes', 'Date inscription'];

        $lines = [$bom . implode(';', $headers)];
        foreach ($users as $u) {
            $lines[] = implode(';', [
                $u->id,
                str_replace(';', ',', $u->name),
                $u->email,
                $u->phone ?? '',
                $u->is_active ? 'Actif' : 'Inactif',
                $u->kyc_status ?? 'none',
                $u->accounts->count(),
                $u->created_at->format('Y-m-d'),
            ]);
        }

        return response(implode("\n", $lines), 200, [
            'Content-Type'        => 'text/csv; charset=UTF-8',
            'Content-Disposition' => 'attachment; filename="utilisateurs_' . now()->format('Y-m-d') . '.csv"',
        ]);
    }
}
