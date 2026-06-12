<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Services\AdminService;

class AdminFraudController extends Controller
{
    public function __construct(private AdminService $adminService) {}

    public function index()
    {
        $alerts = $this->adminService->getFraudAlerts(50);

        return view('admin.fraud', compact('alerts'));
    }
}
