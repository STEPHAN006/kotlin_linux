<?php

namespace App\Services;

use App\Models\Account;
use App\Models\Transaction;
use App\Models\Transfer;
use App\Models\UserNotification;
use App\Repositories\TransactionRepository;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;
use Illuminate\Validation\ValidationException;

class TransferService
{
    public function __construct(
        protected AuditService $auditService,
        protected TransactionRepository $txnRepo
    ) {}

    public function initiate(array $data, int $userId): array
    {
        $sender = Account::where('user_id', $userId)->findOrFail($data['sender_account_id']);
        $receiver = Account::findOrFail($data['receiver_account_id']);
        $amount = (float) $data['amount'];

        $this->assertTransferAllowed($sender, $receiver, $amount);

        $requiresOtp = $this->requiresOtp($sender, $amount);
        $transfer = Transfer::create([
            'sender_account_id' => $sender->id,
            'receiver_account_id' => $receiver->id,
            'amount' => $amount,
            'status' => $requiresOtp ? 'pending' : 'completed',
            'otp_verified' => !$requiresOtp,
            'reference' => self::generateReference(),
            'note' => $data['note'] ?? null,
            'channel' => $data['channel'] ?? 'internal',
            'metadata' => ['initiated_at' => now()->toISOString()],
        ]);

        if ($requiresOtp) {
            $otp = (string) random_int(100000, 999999);
            Cache::put($this->otpCacheKey($transfer), $otp, now()->addMinutes(10));
            Log::info('Bank transfer OTP generated', [
                'transfer_reference' => $transfer->reference,
                'otp' => $otp,
            ]);

            return [
                'transfer' => $transfer,
                'otp_required' => true,
                'message' => 'OTP requis. En demo, le code est visible dans storage/logs/laravel.log.',
            ];
        }

        $completed = $this->complete($transfer, false);

        return [
            'transfer' => $completed,
            'otp_required' => false,
            'message' => 'Virement execute avec succes.',
        ];
    }

    public function verify(string $reference, string $otp, int $userId): Transfer
    {
        $transfer = Transfer::where('reference', $reference)
            ->whereHas('senderAccount', fn ($query) => $query->where('user_id', $userId))
            ->firstOrFail();

        if ($transfer->status !== 'pending') {
            throw ValidationException::withMessages(['reference' => 'Ce virement n est plus en attente.']);
        }

        $expected = Cache::get($this->otpCacheKey($transfer));
        if (!$expected || !hash_equals($expected, $otp)) {
            throw ValidationException::withMessages(['otp' => 'Code OTP invalide ou expire.']);
        }

        Cache::forget($this->otpCacheKey($transfer));

        return $this->complete($transfer, true);
    }

    private function complete(Transfer $transfer, bool $otpVerified): Transfer
    {
        return DB::transaction(function () use ($transfer, $otpVerified) {
            $sender = Account::lockForUpdate()->findOrFail($transfer->sender_account_id);
            $receiver = Account::lockForUpdate()->findOrFail($transfer->receiver_account_id);

            $this->assertTransferAllowed($sender, $receiver, (float) $transfer->amount);

            $sender->balance = (float) $sender->balance - (float) $transfer->amount;
            $receiver->balance = (float) $receiver->balance + (float) $transfer->amount;
            $sender->save();
            $receiver->save();

            Transaction::create([
                'account_id' => $sender->id,
                'type' => 'debit',
                'amount' => $transfer->amount,
                'category' => 'transfer_out',
                'description' => $transfer->note ?: 'Virement vers compte ' . $receiver->id,
                'reference' => TransactionService::generateReference(),
                'balance_after' => $sender->balance,
                'metadata' => ['transfer_reference' => $transfer->reference],
            ]);

            Transaction::create([
                'account_id' => $receiver->id,
                'type' => 'credit',
                'amount' => $transfer->amount,
                'category' => 'transfer_in',
                'description' => 'Virement recu du compte ' . $sender->id,
                'reference' => TransactionService::generateReference(),
                'balance_after' => $receiver->balance,
                'metadata' => ['transfer_reference' => $transfer->reference],
            ]);

            $transfer->update([
                'status' => 'completed',
                'otp_verified' => $otpVerified,
                'metadata' => array_merge($transfer->metadata ?? [], [
                    'completed_at' => now()->toISOString(),
                ]),
            ]);

            $this->auditService->log($sender->user_id, 'transfer.completed', [
                'transfer_reference' => $transfer->reference,
                'amount' => (float) $transfer->amount,
                'receiver_account_id' => $receiver->id,
                'otp_verified' => $otpVerified,
            ]);

            $formattedAmount = number_format((float) $transfer->amount, 0, ',', ' ') . ' MGA';
            UserNotification::create([
                'user_id' => $sender->user_id,
                'title'   => 'Virement envoyé — ' . $formattedAmount,
                'body'    => 'Votre virement de ' . $formattedAmount . ' a été effectué avec succès. Réf: ' . $transfer->reference,
            ]);
            if ($receiver->user_id !== $sender->user_id) {
                UserNotification::create([
                    'user_id' => $receiver->user_id,
                    'title'   => 'Virement reçu — ' . $formattedAmount,
                    'body'    => 'Vous avez reçu un virement de ' . $formattedAmount . '. Réf: ' . $transfer->reference,
                ]);
            }

            // Auto-freeze account if suspicious activity is detected after the transfer
            if ($this->txnRepo->isSuspicious($sender->id)) {
                $sender->update(['status' => 'frozen']);
                $this->auditService->log($sender->user_id, 'account.frozen', [
                    'account_id' => $sender->id,
                    'reason' => 'Activité suspecte détectée (transactions répétées ou fréquence anormale)',
                ], 'critical');
                Log::warning("Compte {$sender->id} gelé automatiquement suite à une activité suspecte.");
            }

            return $transfer->fresh(['senderAccount', 'receiverAccount']);
        });
    }

    private function assertTransferAllowed(Account $sender, Account $receiver, float $amount): void
    {
        if (!$sender->isActive() || !$receiver->isActive()) {
            throw ValidationException::withMessages(['account' => 'Compte inactif ou bloque.']);
        }

        if ($sender->id === $receiver->id) {
            throw ValidationException::withMessages(['receiver_account_id' => 'Le compte destinataire doit etre different.']);
        }

        if ($amount <= 0 || $amount > (float) $sender->balance) {
            throw ValidationException::withMessages(['amount' => 'Solde insuffisant ou montant invalide.']);
        }
    }

    private function requiresOtp(Account $sender, float $amount): bool
    {
        $fixedThreshold = (float) config('banking.otp_threshold_amount', env('OTP_THRESHOLD_AMOUNT', 500000));
        $relativeThreshold = (float) $sender->balance * 0.30;

        return $amount >= $fixedThreshold || $amount >= $relativeThreshold;
    }

    private function otpCacheKey(Transfer $transfer): string
    {
        return 'transfer_otp_' . $transfer->reference;
    }

    public static function generateReference(): string
    {
        return 'TRF' . now()->format('Ymd') . '-' . strtoupper(substr(md5(uniqid('', true)), 0, 8));
    }
}
