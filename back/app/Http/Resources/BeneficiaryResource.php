<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class BeneficiaryResource extends JsonResource
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
            'name' => $this->name,
            'bank_name' => $this->bank_name,
            'account_number_masked' => $this->maskAccountNumber($this->account_number),
            'phone' => $this->phone,
            'channel' => $this->channel,
            'is_verified' => $this->is_verified,
            'created_at' => $this->created_at?->toISOString(),
        ];
    }

    /**
     * Mask the account number for security.
     */
    private function maskAccountNumber(?string $number): string
    {
        if (!$number || strlen($number) < 4) {
            return '****';
        }
        return str_repeat('*', strlen($number) - 4) . substr($number, -4);
    }
}
