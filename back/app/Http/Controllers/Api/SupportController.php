<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\SupportMessage;
use App\Models\SupportTicket;
use App\Models\UserNotification;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class SupportController extends Controller
{
    /** Get or create the current user's active support ticket. */
    public function getOrCreateTicket(Request $request): JsonResponse
    {
        $ticket = SupportTicket::where('user_id', $request->user()->id)
            ->where('status', 'open')
            ->with('messages')
            ->latest()
            ->first();

        if (!$ticket) {
            $ticket = SupportTicket::create([
                'user_id' => $request->user()->id,
                'subject' => 'Support client',
                'status'  => 'open',
            ]);
            SupportMessage::create([
                'ticket_id' => $ticket->id,
                'sender'    => 'admin',
                'message'   => "Bienvenue chez SCpay ! Je suis votre assistant SCpay, ici pour vous aider.\n\nPour que je puisse vous offrir la meilleure solution, veuillez décrire votre question avec le plus de détails possible.",
            ]);
            $ticket->load('messages');
        }

        return response()->json([
            'success' => true,
            'data'    => $this->formatTicket($ticket),
        ]);
    }

    /** Send a message in the current user's active ticket. */
    public function sendMessage(Request $request, int $ticketId): JsonResponse
    {
        $validated = $request->validate(['message' => 'required|string|max:2000']);

        $ticket = SupportTicket::where('user_id', $request->user()->id)
            ->findOrFail($ticketId);

        SupportMessage::create([
            'ticket_id' => $ticket->id,
            'sender'    => 'user',
            'message'   => $validated['message'],
        ]);

        $ticket->load('messages');

        return response()->json([
            'success' => true,
            'data'    => $this->formatTicket($ticket),
        ]);
    }

    private function formatTicket(SupportTicket $ticket): array
    {
        return [
            'id'       => $ticket->id,
            'subject'  => $ticket->subject,
            'status'   => $ticket->status,
            'messages' => $ticket->messages->map(fn($m) => [
                'id'         => $m->id,
                'sender'     => $m->sender,
                'message'    => $m->message,
                'created_at' => $m->created_at->format('Y-m-d H:i'),
            ]),
        ];
    }
}
