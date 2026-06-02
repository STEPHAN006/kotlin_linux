<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Transaction;
use Illuminate\Http\Request;

class StatementController extends Controller
{
    public function monthly(Request $request)
    {
        $validated = $request->validate([
            'account_id' => ['required', 'integer', 'exists:accounts,id'],
            'month' => ['nullable', 'date_format:Y-m'],
        ]);

        $account = $request->user()->accounts()->findOrFail($validated['account_id']);
        $month = $validated['month'] ?? now()->format('Y-m');
        $transactions = Transaction::where('account_id', $account->id)
            ->where('created_at', 'like', $month . '%')
            ->latest()
            ->get();

        $lines = [
            '%PDF-1.4',
            '1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj',
            '2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj',
            '3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R >> endobj',
        ];

        $text = "BT /F1 14 Tf 40 740 Td (Releve {$month} - compte {$account->id}) Tj";
        $y = 720;
        foreach ($transactions->take(18) as $transaction) {
            $label = substr($transaction->created_at->format('d/m') . ' ' . $transaction->type . ' ' . $transaction->amount . ' MGA ' . $transaction->description, 0, 80);
            $text .= " 40 {$y} Td ({$this->pdfSafe($label)}) Tj";
            $y -= 18;
        }
        $text .= ' ET';
        $lines[] = '4 0 obj << /Length ' . strlen($text) . ' >> stream ' . $text . ' endstream endobj';
        $lines[] = 'xref 0 5 0000000000 65535 f trailer << /Root 1 0 R /Size 5 >> startxref 0 %%EOF';

        return response(implode("\n", $lines), 200, [
            'Content-Type' => 'application/pdf',
            'Content-Disposition' => 'attachment; filename="statement-' . $month . '.pdf"',
        ]);
    }

    private function pdfSafe(string $value): string
    {
        return str_replace(['\\', '(', ')'], ['\\\\', '\\(', '\\)'], $value);
    }
}
