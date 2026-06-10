<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Transaction;
use Illuminate\Http\Request;

class AdminTransactionController extends Controller
{
    public function index(Request $request)
    {
        $query = Transaction::with('account.user')->orderByDesc('created_at');

        if ($request->filled('type'))       $query->where('type', $request->type);
        if ($request->filled('date_from'))  $query->whereDate('created_at', '>=', $request->date_from);
        if ($request->filled('date_to'))    $query->whereDate('created_at', '<=', $request->date_to);
        if ($request->filled('amount_min')) $query->where('amount', '>=', $request->amount_min);
        if ($request->filled('search')) {
            $s = $request->search;
            $query->where(fn($q) => $q
                ->where('reference', 'like', "%$s%")
                ->orWhere('description', 'like', "%$s%")
            );
        }

        $transactions = $query->paginate(30)->withQueryString();

        return view('admin.transactions', compact('transactions'));
    }
}
