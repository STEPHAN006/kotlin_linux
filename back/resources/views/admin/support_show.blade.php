@extends('admin.layout')
@section('title', 'Ticket #' . $ticket->id)
@section('page-title', 'Ticket #' . $ticket->id)

@section('content')
<div class="w-full space-y-5">

    <!-- Ticket meta -->
    <div class="card p-5">
        <div class="flex items-start justify-between gap-4 flex-wrap">
            <div class="flex items-center gap-4">
                <div class="w-12 h-12 rounded-full bg-surface-container-high flex items-center justify-center text-sm font-bold text-on-surface flex-shrink-0">
                    {{ strtoupper(substr($ticket->user->name, 0, 2)) }}
                </div>
                <div>
                    <p class="font-semibold text-on-surface text-headline-sm">{{ $ticket->user->name }}</p>
                    <p class="text-label-md text-on-surface-variant">{{ $ticket->user->email }}</p>
                </div>
            </div>
            <div class="flex flex-wrap items-center gap-3">
                <div class="text-center px-4 py-2 bg-gray-50 rounded-lg border border-gray-100">
                    <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Statut</p>
                    <span class="badge badge-{{ $ticket->status }} mt-1 inline-flex">
                        @php $statusLabels = ['open'=>'Ouvert','in_progress'=>'En cours','resolved'=>'Résolu','closed'=>'Fermé']; @endphp
                        {{ $statusLabels[$ticket->status] ?? $ticket->status }}
                    </span>
                </div>
                <div class="text-center px-4 py-2 bg-gray-50 rounded-lg border border-gray-100">
                    <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Priorité</p>
                    <span class="badge badge-{{ $ticket->priority }} mt-1 inline-flex capitalize">{{ $ticket->priority }}</span>
                </div>
                <div class="text-center px-4 py-2 bg-gray-50 rounded-lg border border-gray-100">
                    <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Catégorie</p>
                    <p class="text-body-md font-medium text-on-surface mt-1 capitalize">{{ $ticket->category }}</p>
                </div>
                <div class="text-center px-4 py-2 bg-gray-50 rounded-lg border border-gray-100">
                    <p class="text-label-md text-on-surface-variant uppercase tracking-wide">Créé le</p>
                    <p class="text-body-md font-medium text-on-surface mt-1">{{ $ticket->created_at->format('d/m/Y') }}</p>
                </div>
                <a href="{{ route('admin.support') }}"
                   class="btn-secondary px-4 py-2 text-body-md flex items-center gap-1.5 h-10 ml-auto">
                    <span class="material-symbols-outlined text-[16px]">arrow_back</span> Retour
                </a>
            </div>
        </div>
        <div class="mt-4 pt-4 border-t border-gray-100">
            <p class="text-label-md text-on-surface-variant uppercase tracking-wide mb-1">Sujet</p>
            <p class="text-body-lg font-semibold text-on-surface">{{ $ticket->subject }}</p>
        </div>
    </div>

    <!-- Messages thread -->
    <div class="space-y-3">
        @foreach($ticket->messages as $msg)
        <div class="flex {{ $msg->is_from_agent ? 'justify-end' : 'justify-start' }} gap-3">

            @if(!$msg->is_from_agent)
            <div class="w-8 h-8 rounded-full bg-surface-container-high flex items-center justify-center text-xs font-bold text-on-surface flex-shrink-0 mt-1">
                {{ strtoupper(substr($msg->user->name ?? '?', 0, 2)) }}
            </div>
            @endif

            <div class="max-w-[70%]">
                <div class="flex items-center gap-2 mb-1.5 {{ $msg->is_from_agent ? 'justify-end' : '' }}">
                    <span class="text-label-md font-semibold {{ $msg->is_from_agent ? 'text-primary-container' : 'text-on-surface' }}">
                        {{ $msg->is_from_agent ? 'Agent SCpay' : ($msg->user->name ?? 'Utilisateur') }}
                    </span>
                    <span class="text-label-md text-on-surface-variant">{{ $msg->created_at->format('d/m H:i') }}</span>
                </div>
                <div class="{{ $msg->is_from_agent
                    ? 'bg-primary-container text-white rounded-2xl rounded-tr-sm'
                    : 'bg-white border border-gray-100 shadow-card text-on-surface rounded-2xl rounded-tl-sm' }} p-4">
                    <p class="text-body-md whitespace-pre-wrap leading-relaxed">{{ $msg->message }}</p>
                    @if($msg->attachments->count())
                    <div class="mt-3 space-y-1.5">
                        @foreach($msg->attachments as $att)
                        <a href="{{ Storage::url($att->path) }}" target="_blank"
                           class="{{ $msg->is_from_agent ? 'bg-white/20 hover:bg-white/30 text-white' : 'bg-gray-50 hover:bg-gray-100 text-on-surface' }}
                                  flex items-center gap-2 px-3 py-2 rounded-lg text-label-md transition-colors">
                            <span class="material-symbols-outlined text-[16px] flex-shrink-0">attach_file</span>
                            <span class="truncate">{{ $att->original_name }}</span>
                            <span class="opacity-60 ml-auto flex-shrink-0">{{ round($att->size/1024) }}KB</span>
                        </a>
                        @endforeach
                    </div>
                    @endif
                </div>
            </div>

            @if($msg->is_from_agent)
            <div class="w-8 h-8 rounded-full bg-primary-container flex items-center justify-center flex-shrink-0 mt-1">
                <span class="material-symbols-outlined text-white text-[16px]">support_agent</span>
            </div>
            @endif
        </div>
        @endforeach
    </div>

    <!-- Reply form or closed notice -->
    @if($ticket->status !== 'closed')
    <div class="card p-5">
        <h3 class="font-semibold text-on-surface mb-4 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary-container text-[20px]">reply</span>
            Répondre en tant qu'agent
        </h3>
        <form action="{{ route('admin.support.reply', $ticket->id) }}" method="POST" enctype="multipart/form-data" class="space-y-4">
            @csrf
            <textarea name="message" rows="4" required placeholder="Votre réponse…"
                      class="w-full border border-outline-variant rounded-lg px-4 py-3 text-body-md text-on-surface bg-surface-container-lowest
                             focus:outline-none focus:ring-2 focus:ring-primary-container/20 focus:border-primary-container resize-none transition-all
                             placeholder:text-gray-400"></textarea>

            <div class="flex flex-wrap gap-3 items-center">
                <div class="flex flex-col gap-1">
                    <label class="text-label-md text-on-surface-variant uppercase tracking-wide">Changer le statut</label>
                    <select name="status" class="input pr-8">
                        <option value="">Inchangé</option>
                        <option value="in_progress">En cours</option>
                        <option value="resolved">Résolu</option>
                        <option value="closed">Fermé</option>
                    </select>
                </div>
                <div class="flex flex-col gap-1">
                    <label class="text-label-md text-on-surface-variant uppercase tracking-wide">Pièces jointes</label>
                    <label class="flex items-center gap-2 h-10 px-3 border border-outline-variant rounded-lg cursor-pointer
                                  bg-surface-container-lowest hover:bg-surface-container-low transition-colors text-body-md text-on-surface-variant">
                        <span class="material-symbols-outlined text-[18px]">attach_file</span>
                        <span id="file-label">Joindre des fichiers</span>
                        <input type="file" name="attachments[]" multiple accept="image/*,application/pdf" class="hidden"
                               onchange="document.getElementById('file-label').textContent = this.files.length + ' fichier(s)'">
                    </label>
                </div>
                <button type="submit"
                        class="btn-primary px-5 py-2.5 text-body-md font-semibold flex items-center gap-2 ml-auto">
                    <span class="material-symbols-outlined text-[18px]">send</span>
                    Envoyer la réponse
                </button>
            </div>
        </form>
    </div>
    @else
    <div class="card p-5 flex items-center gap-3 text-on-surface-variant">
        <span class="material-symbols-outlined text-gray-400">lock</span>
        <p class="text-body-md">Ce ticket est fermé et ne peut plus recevoir de réponses.</p>
    </div>
    @endif
</div>
@endsection
