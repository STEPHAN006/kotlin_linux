<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Models\Transaction;
use App\Services\AdminService;
use Illuminate\Http\Request;
use Illuminate\Support\Carbon;

class AdminWebDashboardController extends Controller
{
    public function __construct(
        protected AdminService $adminService
    ) {}

    /**
     * Show the admin dashboard overview.
     */
    public function index()
    {
        $stats = $this->adminService->getDashboardStats();
        $recentTransactions = $this->adminService->getRecentTransactions(5);

        // Prepare chart data (Last 7 days transaction volume)
        $chartData = $this->getChartData();

        return view('admin.dashboard', compact('stats', 'recentTransactions', 'chartData'));
    }

    /**
     * Show the users list.
     */
    public function users()
    {
        // Get users with their total balance
        $users = User::where('role', 'user')
            ->withSum(['accounts' => function ($query) {
                $query->where('status', 'active');
            }], 'balance')
            ->orderBy('created_at', 'desc')
            ->paginate(15);

        return view('admin.users', compact('users'));
    }

    /**
     * Show the transactions list.
     */
    public function transactions()
    {
        $transactions = Transaction::with('account.user')
            ->orderBy('created_at', 'desc')
            ->paginate(20);

        return view('admin.transactions', compact('transactions'));
    }

    /**
     * Helper to get last 7 days chart data.
     */
    private function getChartData()
    {
        $labels = [];
        $volumes = [];

        for ($i = 6; $i >= 0; $i--) {
            $date = Carbon::today()->subDays($i);
            $labels[] = $date->format('M d');
            
            $dailyVolume = Transaction::whereDate('created_at', $date)->sum('amount');
            $volumes[] = (float) $dailyVolume;
        }

        return [
            'labels' => $labels,
            'data' => $volumes,
        ];
    }
}
