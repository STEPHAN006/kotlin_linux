@extends('admin.layout')

@section('title', 'Ticket #' . $ticket->id)
@section('page-title', 'Ticket #' . $ticket->id . ' — ' . ($ticket->user->name ?? 'Client'))

@section('content')
<div class="space-y-5">

    @if(session('success'))
    <div class="flex items-center gap-2 bg-emerald-50 border border-emerald-200 text-emerald-700 px-4 py-3 rounded-lg text-body-md">
        <span class="material-symbols-outlined text-[18px]">check_circle</span>
        {{ session('success') }}
    </div>
    @endif

    {{-- Ticket header --}}
    <div class="card p-5 flex items-center gap-4">
        <div class="w-10 h-10 rounded-full bg-gradient-to-br from-primary to-purple-600 flex items-center justify-center text-white font-bold flex-shrink-0">
            {{ strtoupper(substr($ticket->user->name ?? 'U', 0, 1)) }}
        </div>
        <div class="flex-1 min-w-0">
            <p class="text-body-md font-semibold text-on-surface">{{ $ticket->user->name ?? 'Client inconnu' }}</p>
            <p class="text-label-md text-on-surface-variant">{{ $ticket->user->email ?? '' }}{{ $ticket->user->phone ? ' · ' . $ticket->user->phone : '' }}</p>
        </div>
        <span class="badge badge-{{ $ticket->status }}">
            {{ $ticket->status === 'open' ? 'Ouvert' : 'Fermé' }}
        </span>
    </div>

    {{-- Conversation --}}
    <div class="card px-5 py-4 space-y-2">
        @foreach($ticket->messages as $msg)
        @php $isAdmin = $msg->sender === 'admin'; @endphp
        <div class="flex {{ $isAdmin ? 'justify-end' : 'justify-start' }}">
            <div class="max-w-[50%]">
                <div class="inline-block px-3 py-1.5 rounded-xl text-[13px] leading-snug whitespace-pre-wrap break-words text-left
                    {{ $isAdmin
                        ? 'bg-primary text-white rounded-br-sm'
                        : 'bg-gray-100 text-on-surface rounded-bl-sm' }}">{{ $msg->message }}
                    @if($msg->image_url)
                    <img src="{{ $msg->image_url }}" alt="Pièce jointe"
                         class="mt-2 max-w-[260px] rounded-lg border border-white/20 cursor-pointer"
                         onclick="window.open(this.src,'_blank')">
                    @endif
                </div>
                <p class="text-[10px] text-on-surface-variant mt-0.5 {{ $isAdmin ? 'text-right' : 'text-left' }}">
                    {{ $isAdmin ? 'Vous' : $ticket->user->name }} · {{ $msg->created_at->format('d/m H:i') }}
                </p>
            </div>
        </div>
        @endforeach
    </div>

    {{-- Reply / closed notice --}}
    @if($ticket->status === 'open')
    <div class="card p-5">
        <h3 class="text-body-md font-semibold text-on-surface mb-3">Répondre au client</h3>
        <form method="POST" action="{{ route('admin.support.reply', $ticket->id) }}">
            @csrf
            <textarea name="message"
                      rows="3"
                      placeholder="Tapez votre réponse ici…"
                      autofocus
                      required
                      class="w-full border border-outline-variant rounded-xl px-4 py-3 text-body-md text-on-surface bg-surface-container-lowest
                             focus:outline-none focus:ring-2 focus:ring-primary-container/20 focus:border-primary-container
                             resize-y transition-all">{{ old('message') }}</textarea>
            @error('message')
            <p class="text-error text-body-md mt-2">{{ $message }}</p>
            @enderror
            <div class="flex gap-3 mt-3">
                <button type="submit" class="btn-primary px-5 py-2 text-body-md rounded-lg inline-flex items-center gap-2">
                    <span class="material-symbols-outlined text-[15px]">send</span>
                    Envoyer la réponse
                </button>
                <a href="{{ route('admin.support.index') }}"
                   class="btn-secondary px-5 py-2 text-body-md rounded-lg inline-flex items-center gap-2">
                    <span class="material-symbols-outlined text-[15px]">arrow_back</span>
                    Retour à la liste
                </a>
            </div>
        </form>
    </div>
    @else
    <div class="card p-5 text-center text-on-surface-variant text-body-md">
        <span class="material-symbols-outlined text-[28px] mb-1 block">lock</span>
        Ce ticket est fermé.
        <a href="{{ route('admin.support.index') }}" class="text-primary font-medium ml-1 hover:underline">Retour à la liste</a>
    </div>
    @endif

</div>
@endsection
