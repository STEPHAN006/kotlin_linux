<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Transaction;
use Barryvdh\DomPDF\Facade\Pdf;
use Illuminate\Http\Request;

class StatementController extends Controller
{
    public function monthly(Request $request)
    {
        $validated = $request->validate([
            'account_id' => ['required', 'integer', 'exists:accounts,id'],
            'month'      => ['nullable', 'date_format:Y-m'],
        ]);

        $account = $request->user()->accounts()->findOrFail($validated['account_id']);
        $month   = $validated['month'] ?? now()->format('Y-m');

        $transactions = Transaction::where('account_id', $account->id)
            ->where('created_at', 'like', $month . '%')
            ->latest()
            ->get();

        $totalCredit = $transactions->where('type', 'credit')->sum('amount');
        $totalDebit  = $transactions->where('type', 'debit')->sum('amount');
        $user        = $request->user();

        $html = view('pdf.statement', compact(
            'account', 'transactions', 'month', 'totalCredit', 'totalDebit', 'user'
        ))->render();

        $pdf = Pdf::loadHtml($html)
            ->setPaper('a4', 'portrait')
            ->setOptions(['defaultFont' => 'sans-serif', 'isHtml5ParserEnabled' => true]);

        return $pdf->download("releve-{$month}-compte-{$account->id}.pdf");
    }

    public function exportCsv(Request $request)
    {
        $validated = $request->validate([
            'account_id' => ['required', 'integer', 'exists:accounts,id'],
            'date_from'  => ['nullable', 'date'],
            'date_to'    => ['nullable', 'date'],
            'format'     => ['nullable', 'in:csv,swift'],
        ]);

        $account = $request->user()->accounts()->findOrFail($validated['account_id']);
        $format  = $validated['format'] ?? 'csv';

        $query = Transaction::where('account_id', $account->id)->latest();

        if (!empty($validated['date_from'])) {
            $query->whereDate('created_at', '>=', $validated['date_from']);
        }
        if (!empty($validated['date_to'])) {
            $query->whereDate('created_at', '<=', $validated['date_to']);
        }

        $transactions = $query->get();

        if ($format === 'swift') {
            return $this->swiftExport($account, $transactions);
        }

        return $this->csvExport($account, $transactions);
    }

    private function csvExport($account, $transactions)
    {
        $lines = ["Date,Type,Montant,Devise,Description,Reference,Solde apres"];

        foreach ($transactions as $t) {
            $lines[] = implode(',', [
                $t->created_at->format('Y-m-d H:i:s'),
                $t->type,
                number_format((float) $t->amount, 2, '.', ''),
                $account->currency,
                '"' . str_replace('"', '""', $t->description ?? '') . '"',
                $t->reference,
                number_format((float) $t->balance_after, 2, '.', ''),
            ]);
        }

        return response(implode("\n", $lines), 200, [
            'Content-Type'        => 'text/csv; charset=UTF-8',
            'Content-Disposition' => 'attachment; filename="transactions-' . $account->id . '.csv"',
        ]);
    }

    private function swiftExport($account, $transactions)
    {
        $lines = [":20:SCPAY-EXPORT", ":25:{$account->account_number}", ":28C:00001/001", ":60F:C" . now()->format('ymd') . "{$account->currency}" . number_format((float) $account->balance, 2, ',', '')];

        foreach ($transactions as $t) {
            $dc     = $t->type === 'credit' ? 'C' : 'D';
            $date   = $t->created_at->format('ymd');
            $amount = number_format((float) $t->amount, 2, ',', '');
            $desc   = substr($t->description ?? 'TRANSFER', 0, 35);
            $lines[] = ":61:{$date}{$dc}{$amount}N//{$t->reference}";
            $lines[] = ":86:{$desc}";
        }

        $lines[] = ":62F:C" . now()->format('ymd') . "{$account->currency}" . number_format((float) $account->balance, 2, ',', '');
        $lines[] = "-}";

        return response(implode("\r\n", $lines), 200, [
            'Content-Type'        => 'text/plain; charset=UTF-8',
            'Content-Disposition' => 'attachment; filename="swift-' . $account->id . '.txt"',
        ]);
    }
}
