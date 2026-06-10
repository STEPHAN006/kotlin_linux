<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\SupportMessage;
use App\Models\SupportTicket;
use App\Models\UserNotification;
use Illuminate\Http\Request;

class AdminSupportController extends Controller
{
    public function index()
    {
        $tickets = SupportTicket::with(['user', 'messages' => fn($q) => $q->latest()->limit(1)])
            ->orderByDesc('updated_at')
            ->get();

        return view('admin.support.index', compact('tickets'));
    }

    public function show(int $id)
    {
        $ticket = SupportTicket::with(['user', 'messages'])->findOrFail($id);

        return view('admin.support.show', compact('ticket'));
    }

    public function reply(Request $request, int $id)
    {
        $request->validate(['message' => 'required|string|max:2000']);

        $ticket = SupportTicket::findOrFail($id);

        SupportMessage::create([
            'ticket_id' => $ticket->id,
            'sender'    => 'admin',
            'message'   => $request->message,
        ]);

        UserNotification::create([
            'user_id' => $ticket->user_id,
            'title'   => 'Réponse du support SCpay',
            'body'    => substr($request->message, 0, 120) . (strlen($request->message) > 120 ? '...' : ''),
        ]);

        $ticket->touch();

        return redirect()->route('admin.support.show', $id)->with('success', 'Réponse envoyée.');
    }
}
