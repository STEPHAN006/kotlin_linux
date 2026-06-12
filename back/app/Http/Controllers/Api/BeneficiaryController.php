<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\BeneficiaryResource;
use App\Models\Beneficiary;
use App\Services\AuditService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

class BeneficiaryController extends Controller
{
    public function __construct(protected AuditService $auditService) {}

    public function index(Request $request): JsonResponse
    {
        return response()->json([
            'success' => true,
            'data' => BeneficiaryResource::collection($request->user()->beneficiaries()->latest()->get()),
        ]);
    }

    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'name' => ['required', 'string', 'max:120'],
            'bank_name' => ['required', 'string', 'max:120'],
            'account_number' => ['required', 'string', 'max:40'],
            'phone' => ['nullable', 'string', 'max:20'],
            'channel' => ['required', Rule::in(['bank', 'mvola', 'airtel_money', 'orange_money'])],
        ]);

        $beneficiary = $request->user()->beneficiaries()->create($validated + ['is_verified' => true]);
        $this->auditService->log($request->user()->id, 'beneficiary.created', [
            'beneficiary_id' => $beneficiary->id,
            'channel' => $beneficiary->channel,
        ], 'info', $request, 201);

        return response()->json([
            'success' => true,
            'message' => 'Beneficiaire ajoute.',
            'data' => new BeneficiaryResource($beneficiary),
        ], 201);
    }

    public function destroy(Request $request, Beneficiary $beneficiary): JsonResponse
    {
        abort_unless($beneficiary->user_id === $request->user()->id, 403);
        $beneficiary->delete();

        return response()->json([
            'success' => true,
            'message' => 'Beneficiaire supprime.',
        ]);
    }
}
