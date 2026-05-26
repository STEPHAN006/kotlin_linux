<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class TransactionResource extends JsonResource
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
            'account_id' => $this->account_id,
            'type' => $this->type,
            'amount' => (float) $this->amount,
            'formatted_amount' => ($this->type === 'debit' ? '-' : '+') .
                number_format($this->amount, 2, ',', ' ') . ' MGA',
            'category' => $this->category,
            'description' => $this->description,
            'reference' => $this->reference,
            'balance_after' => $this->balance_after ? (float) $this->balance_after : null,
            'metadata' => $this->when($this->metadata, $this->metadata),
            'created_at' => $this->created_at?->toISOString(),
            'date_human' => $this->created_at?->diffForHumans(),
        ];
    }
}
