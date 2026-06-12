<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\SupportTicket;
use App\Models\User;
use App\Services\AdminService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class DashboardController extends Controller
{
    public function __construct(private AdminService $adminService) {}

    public function showLogin()
    {
        if (session('admin_logged_in')) {
            return redirect()->route('admin.dashboard');
        }

        return view('admin.login');
    }

    public function login(Request $request)
    {
        $request->validate([
            'email'    => 'required|email',
            'password' => 'required',
        ]);

        $user = User::where('email', $request->email)
            ->where('role', 'admin')
            ->first();

        if (!$user || !Hash::check($request->password, $user->password)) {
            return back()->withErrors(['credentials' => 'Email ou mot de passe incorrect.'])->withInput();
        }

        session(['admin_logged_in' => true, 'admin_name' => $user->name]);

        return redirect()->route('admin.dashboard');
    }

    public function logout()
    {
        session()->forget(['admin_logged_in', 'admin_name']);
        return redirect()->route('admin.login');
    }

    public function index()
    {
        $stats              = $this->adminService->getDashboardStats();
        $recentTransactions = $this->adminService->getRecentTransactions(10);
        $fraudAlerts        = $this->adminService->getFraudAlerts(6);
        $openTickets        = SupportTicket::where('status', 'open')->count();

        return view('admin.dashboard', compact('stats', 'recentTransactions', 'fraudAlerts', 'openTickets'));
    }
}
