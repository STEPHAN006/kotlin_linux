<?php

namespace App\Http\Controllers;

use App\Models\SupportMessage;
use App\Models\SupportTicket;
use App\Models\Transaction;
use App\Models\User;
use App\Models\UserNotification;
use App\Repositories\AccountRepository;
use App\Repositories\TransactionRepository;
use App\Services\AdminService;
use Illuminate\Http\Request;

class AdminSupportController extends Controller
{
    public function __construct(private AdminService $adminService) {}

    /** Show login form or redirect to support list. */
    public function loginForm()
    {
        if (session('admin_logged_in')) {
            return redirect()->route('admin.support.index');
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

        if (!$user || !password_verify($request->password, $user->password)) {
            return back()->withErrors(['credentials' => 'Email ou mot de passe incorrect.'])->withInput();
        }

        session(['admin_logged_in' => true, 'admin_name' => $user->name]);

        return redirect()->route('admin.support.index');
    }

    public function logout()
    {
        session()->forget(['admin_logged_in', 'admin_name']);
        return redirect()->route('admin.login');
    }

    /** List all open tickets. */
    public function index()
    {
        $this->requireAdminSession();

        $tickets = SupportTicket::with(['user', 'messages' => fn($q) => $q->latest()->limit(1)])
            ->orderByDesc('updated_at')
            ->get();

        return view('admin.support.index', compact('tickets'));
    }

    /** Show a single ticket with full conversation + reply form. */
    public function show(int $id)
    {
        $this->requireAdminSession();

        $ticket = SupportTicket::with(['user', 'messages'])->findOrFail($id);

        return view('admin.support.show', compact('ticket'));
    }

    /** Admin sends a reply. */
    public function reply(Request $request, int $id)
    {
        $this->requireAdminSession();

        $request->validate(['message' => 'required|string|max:2000']);

        $ticket = SupportTicket::findOrFail($id);

        SupportMessage::create([
            'ticket_id' => $ticket->id,
            'sender'    => 'admin',
            'message'   => $request->message,
        ]);

        // Notify the user
        UserNotification::create([
            'user_id' => $ticket->user_id,
            'title'   => 'Réponse du support SCpay',
            'body'    => substr($request->message, 0, 120) . (strlen($request->message) > 120 ? '...' : ''),
        ]);

        $ticket->touch();

        return redirect()->route('admin.support.show', $id)->with('success', 'Réponse envoyée.');
    }

    /** Close a ticket. */
    public function close(int $id)
    {
        $this->requireAdminSession();

        SupportTicket::findOrFail($id)->update(['status' => 'closed']);

        return redirect()->route('admin.support.index')->with('success', 'Ticket fermé.');
    }

    public function adminDashboard()
    {
        $this->requireAdminSession();

        $stats           = $this->adminService->getDashboardStats();
        $recentTransactions = $this->adminService->getRecentTransactions(10);
        $fraudAlerts     = $this->adminService->getFraudAlerts(6);
        $openTickets     = SupportTicket::where('status', 'open')->count();

        return view('admin.dashboard', compact('stats', 'recentTransactions', 'fraudAlerts', 'openTickets'));
    }

    public function adminTransactions(Request $request)
    {
        $this->requireAdminSession();

        $query = Transaction::with('account.user')->orderByDesc('created_at');

        if ($request->filled('type'))       $query->where('type', $request->type);
        if ($request->filled('date_from'))  $query->whereDate('created_at', '>=', $request->date_from);
        if ($request->filled('date_to'))    $query->whereDate('created_at', '<=', $request->date_to);
        if ($request->filled('amount_min')) $query->where('amount', '>=', $request->amount_min);
        if ($request->filled('search')) {
            $s = $request->search;
            $query->where(fn($q) => $q->where('reference', 'like', "%$s%")->orWhere('description', 'like', "%$s%"));
        }

        $transactions = $query->paginate(30);

        return view('admin.transactions', compact('transactions'));
    }

    public function adminFraud()
    {
        $this->requireAdminSession();
        $alerts = $this->adminService->getFraudAlerts(50);
        return view('admin.fraud', compact('alerts'));
    }

    private function requireAdminSession()
    {
        if (!session('admin_logged_in')) {
            abort(redirect()->route('admin.login'));
        }
    }
}
