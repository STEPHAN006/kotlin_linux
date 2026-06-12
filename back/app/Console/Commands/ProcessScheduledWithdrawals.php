<?php

namespace App\Console\Commands;

use App\Models\Account;
use App\Models\ScheduledWithdrawal;
use App\Models\Transaction;
use App\Models\Transfer;
use App\Models\UserNotification;
use App\Services\TransferService;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;

class ProcessScheduledWithdrawals extends Command
{
    protected $signature   = 'withdrawals:process';
    protected $description = 'Execute les retraits automatiques arrivés à échéance';

    public function handle(): void
    {
        $due = ScheduledWithdrawal::where('is_active', true)
            ->where('next_run_at', '<=', now())
            ->with(['senderAccount', 'beneficiary'])
            ->get();

        $this->info("Retraits à traiter : {$due->count()}");

        foreach ($due as $sw) {
            try {
                $this->processOne($sw);
                $this->line("  ✓ ID {$sw->id} — {$sw->amount} MGA vers {$sw->beneficiary->name}");
            } catch (\Throwable $e) {
                Log::error("Retrait automatique ID {$sw->id} échoué : {$e->getMessage()}");
                $this->error("  ✗ ID {$sw->id} — {$e->getMessage()}");

                // Notifier l'utilisateur de l'échec
                UserNotification::create([
                    'user_id' => $sw->user_id,
                    'title'   => 'Retrait automatique échoué',
                    'body'    => "Le retrait automatique de " . number_format((float) $sw->amount, 0, ',', ' ') . " MGA vers {$sw->beneficiary->name} a échoué : {$e->getMessage()}",
                ]);
            }
        }

        $this->info('Terminé.');
    }

    private function processOne(ScheduledWithdrawal $sw): void
    {
        $sender = $sw->senderAccount;

        if (!$sender || $sender->status !== 'active') {
            throw new \RuntimeException('Compte source inactif ou introuvable.');
        }

        if ((float) $sender->balance < (float) $sw->amount) {
            throw new \RuntimeException('Solde insuffisant.');
        }

        DB::transaction(function () use ($sw, $sender) {
            $sender = Account::lockForUpdate()->findOrFail($sender->id);

            if ((float) $sender->balance < (float) $sw->amount) {
                throw new \RuntimeException('Solde insuffisant.');
            }

            $sender->balance = (float) $sender->balance - (float) $sw->amount;
            $sender->save();

            $reference = 'AUTO' . now()->format('Ymd') . '-' . strtoupper(substr(md5(uniqid('', true)), 0, 8));
            $note = $sw->note ?: "Retrait automatique vers {$sw->beneficiary->name}";

            Transaction::create([
                'account_id'    => $sender->id,
                'type'          => 'debit',
                'amount'        => $sw->amount,
                'category'      => 'scheduled_withdrawal',
                'description'   => $note,
                'reference'     => $reference,
                'balance_after' => $sender->balance,
                'metadata'      => ['scheduled_withdrawal_id' => $sw->id],
            ]);

            $formattedAmount = number_format((float) $sw->amount, 0, ',', ' ') . ' MGA';
            UserNotification::create([
                'user_id' => $sw->user_id,
                'title'   => "Retrait automatique — {$formattedAmount}",
                'body'    => "Retrait de {$formattedAmount} vers {$sw->beneficiary->name} effectué. Réf : {$reference}",
            ]);

            $sw->update([
                'last_run_at' => now(),
                'run_count'   => $sw->run_count + 1,
                'next_run_at' => now()->addDays($sw->frequency_days),
            ]);
        });
    }
}
