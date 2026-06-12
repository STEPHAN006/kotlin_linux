@extends('admin.layout')

@section('title', 'Support')
@section('page-title', 'Tickets de Support')

@section('content')
<div class="space-y-6">

    {{-- Stats --}}
    <div class="grid grid-cols-3 gap-4">
        <div class="card p-5">
            <p class="text-label-md text-on-surface-variant uppercase tracking-wider mb-2">Ouverts</p>
            <p class="text-display-lg text-primary">{{ $tickets->where('status','open')->count() }}</p>
        </div>
        <div class="card p-5">
            <p class="text-label-md text-on-surface-variant uppercase tracking-wider mb-2">Fermés</p>
            <p class="text-display-lg text-on-surface">{{ $tickets->where('status','closed')->count() }}</p>
        </div>
        <div class="card p-5">
            <p class="text-label-md text-on-surface-variant uppercase tracking-wider mb-2">Total</p>
            <p class="text-display-lg text-navy">{{ $tickets->count() }}</p>
        </div>
    </div>

    @if(session('success'))
    <div class="flex items-center gap-2 bg-emerald-50 border border-emerald-200 text-emerald-700 px-4 py-3 rounded-lg text-body-md">
        <span class="material-symbols-outlined text-[18px]">check_circle</span>
        {{ session('success') }}
    </div>
    @endif

    {{-- Table --}}
    <div class="card overflow-hidden">
        @if($tickets->isEmpty())
        <div class="py-20 text-center">
            <span class="material-symbols-outlined text-[56px] text-on-surface-variant">support_agent</span>
            <p class="text-headline-sm text-on-surface mt-4">Aucun ticket pour l'instant</p>
        </div>
        @else
        <div class="overflow-x-auto">
            <table class="w-full">
                <thead>
                    <tr class="bg-gray-50">
                        <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">#</th>
                        <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Client</th>
                        <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Sujet</th>
                        <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Dernier message</th>
                        <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Statut</th>
                        <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Date</th>
                        <th class="px-6 py-3 text-left text-label-md text-on-surface-variant">Action</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-gray-50">
                    @foreach($tickets as $ticket)
                    @php
                        $lastMsg   = $ticket->messages->last();
                        $hasUnread = $lastMsg && $lastMsg->sender === 'user';
                    @endphp
                    <tr class="hover:bg-surface-container-low transition-colors">
                        <td class="px-6 py-4 text-body-md text-on-surface-variant">#{{ $ticket->id }}</td>
                        <td class="px-6 py-4">
                            <div class="flex items-center gap-2">
                                @if($hasUnread)
                                <span class="w-2 h-2 rounded-full bg-primary flex-shrink-0"></span>
                                @endif
                                <div>
                                    <p class="text-body-md font-semibold text-on-surface">{{ $ticket->user->name ?? 'Inconnu' }}</p>
                                    <p class="text-label-md text-on-surface-variant">{{ $ticket->user->email ?? '' }}</p>
                                </div>
                            </div>
                        </td>
                        <td class="px-6 py-4 text-body-md text-on-surface">{{ $ticket->subject }}</td>
                        <td class="px-6 py-4 max-w-[260px] truncate text-body-md text-on-surface-variant">
                            @if($lastMsg)
                                <span class="font-medium {{ $lastMsg->sender === 'user' ? 'text-amber-600' : 'text-on-surface-variant' }}">
                                    [{{ $lastMsg->sender === 'user' ? 'Client' : 'Admin' }}]
                                </span>
                                {{ $lastMsg->message }}
                            @else
                                <em class="text-on-surface-variant">Aucun message</em>
                            @endif
                        </td>
                        <td class="px-6 py-4">
                            <span class="badge badge-{{ $ticket->status }}">
                                {{ $ticket->status === 'open' ? 'Ouvert' : 'Fermé' }}
                            </span>
                        </td>
                        <td class="px-6 py-4 text-body-md text-on-surface-variant whitespace-nowrap">
                            {{ $ticket->updated_at->format('d/m H:i') }}
                        </td>
                        <td class="px-6 py-4">
                            <a href="{{ route('admin.support.show', $ticket->id) }}"
                               class="btn-primary px-4 py-2 text-body-md rounded-lg inline-flex items-center gap-1">
                                <span class="material-symbols-outlined text-[15px]">reply</span>
                                Répondre
                            </a>
                        </td>
                    </tr>
                    @endforeach
                </tbody>
            </table>
        </div>
        @endif
    </div>

</div>
@endsection
