<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class AccountResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'account_number' => $this->maskAccountNumber($this->account_number),
            'account_number_full' => $this->when(
                $request->routeIs('account.details'),
                $this->account_number
            ),
            'balance' => (float) $this->balance,
            'formatted_balance' => number_format($this->balance, 2, ',', ' ') . ' ' . $this->currency,
            'currency' => $this->currency,
            'status' => $this->status,
            'type' => $this->type,
            'cards' => CardResource::collection($this->whenLoaded('cards')),
            'created_at' => $this->created_at?->toISOString(),
        ];
    }

    /**
     * Mask the account number for security (show last 4 digits).
     */
    private function maskAccountNumber(?string $number): string
    {
        if (!$number || strlen($number) < 4) {
            return '****';
        }
        return str_repeat('*', strlen($number) - 4) . substr($number, -4);
    }
}
