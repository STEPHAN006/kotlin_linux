<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\SupportMessage;
use App\Models\SupportTicket;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

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

    /** Send a message (with optional image) in the current user's active ticket. */
    public function sendMessage(Request $request, int $ticketId): JsonResponse
    {
        $request->validate([
            'message'    => 'required|string|max:2000',
            'image'      => 'nullable|image|max:5120',
        ]);

        $ticket = SupportTicket::where('user_id', $request->user()->id)
            ->findOrFail($ticketId);

        $imageUrl = null;
        if ($request->hasFile('image')) {
            $path = $request->file('image')->store('support', 'public');
            $imageUrl = $request->getSchemeAndHttpHost() . '/storage/' . $path;
        }

        SupportMessage::create([
            'ticket_id' => $ticket->id,
            'sender'    => 'user',
            'message'   => $request->input('message'),
            'image_url' => $imageUrl,
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
                'id'           => $m->id,
                'sender'       => $m->sender,
                'is_from_agent'=> $m->sender === 'admin',
                'sender_name'  => $m->sender === 'admin' ? 'Agent SCpay' : 'Vous',
                'message'      => $m->message,
                'image_url'    => $m->image_url,
                'created_at'   => $m->created_at->format('Y-m-d H:i'),
            ]),
        ];
    }
}
