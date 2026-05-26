<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class TransferResource extends JsonResource
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
            'sender_account_id' => $this->sender_account_id,
            'receiver_account_id' => $this->receiver_account_id,
            'amount' => (float) $this->amount,
            'formatted_amount' => number_format($this->amount, 2, ',', ' ') . ' MGA',
            'status' => $this->status,
            'otp_verified' => $this->otp_verified,
            'reference' => $this->reference,
            'note' => $this->note,
            'channel' => $this->channel,
            'sender' => new AccountResource($this->whenLoaded('senderAccount')),
            'receiver' => new AccountResource($this->whenLoaded('receiverAccount')),
            'created_at' => $this->created_at?->toISOString(),
        ];
    }
}
