<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class CardResource extends JsonResource
{
    /**
     * Transform the resource into an array.
     * Card numbers and CVVs are NEVER exposed in full through the API.
     *
     * @return array<string, mixed>
     */
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'account_id' => $this->account_id,
            'card_number_masked' => $this->masked_number,
            'last_four' => substr($this->card_number, -4),
            'expiry_date' => $this->expiry_date?->format('m/Y'),
            'is_blocked' => $this->is_blocked,
            'type' => $this->type,
            'daily_limit' => (float) $this->daily_limit,
            'formatted_daily_limit' => number_format($this->daily_limit, 2, ',', ' ') . ' MGA',
            'created_at' => $this->created_at?->toISOString(),
        ];
    }
}
