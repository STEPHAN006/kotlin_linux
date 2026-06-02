<?php

namespace App\Services;

use App\Models\AuditLog;
use Illuminate\Http\Request;

class AuditService
{
    public function log(
        ?int $userId,
        string $action,
        array $details = [],
        string $severity = 'info',
        ?Request $request = null,
        ?int $statusCode = null
    ): AuditLog {
        return AuditLog::create([
            'user_id' => $userId,
            'action' => $action,
            'endpoint' => $request?->path(),
            'status_code' => $statusCode,
            'device_id' => $request?->header('X-Device-Id'),
            'ip_address' => $request?->ip(),
            'details' => $details,
            'severity' => $severity,
        ]);
    }
}
