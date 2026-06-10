<!DOCTYPE html>
<html lang="fr" class="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>@yield('title', 'Dashboard') — SCpay Admin</title>
    <script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <script defer src="https://cdn.jsdelivr.net/npm/alpinejs@3.x.x/dist/cdn.min.js"></script>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">
    <script id="tailwind-config">
        tailwind.config = {
            darkMode: "class",
            theme: {
                extend: {
                    colors: {
                        "primary": "#b80035",
                        "primary-container": "#e11d48",
                        "on-primary": "#ffffff",
                        "on-primary-container": "#fffaf9",
                        "inverse-primary": "#ffb3b6",
                        "secondary": "#5e5e63",
                        "on-secondary": "#ffffff",
                        "secondary-container": "#e0dfe4",
                        "on-secondary-container": "#626267",
                        "tertiary": "#006855",
                        "on-tertiary": "#ffffff",
                        "tertiary-container": "#00836c",
                        "on-tertiary-container": "#eefff7",
                        "error": "#ba1a1a",
                        "on-error": "#ffffff",
                        "error-container": "#ffdad6",
                        "on-error-container": "#93000a",
                        "background": "#fff8f7",
                        "on-background": "#281718",
                        "surface": "#fff8f7",
                        "surface-dim": "#f2d3d3",
                        "surface-bright": "#fff8f7",
                        "surface-container-lowest": "#ffffff",
                        "surface-container-low": "#fff0f0",
                        "surface-container": "#ffe9e9",
                        "surface-container-high": "#ffe1e1",
                        "surface-container-highest": "#fbdbdb",
                        "on-surface": "#281718",
                        "on-surface-variant": "#5c3f40",
                        "inverse-surface": "#3f2b2c",
                        "inverse-on-surface": "#ffedec",
                        "outline": "#906f70",
                        "outline-variant": "#e5bdbe",
                        "surface-tint": "#be0037",
                        "surface-variant": "#fbdbdb",
                        "navy": "#17181C",
                    },
                    borderRadius: {
                        DEFAULT: "0.5rem",
                        sm: "0.25rem",
                        md: "0.75rem",
                        lg: "1rem",
                        xl: "1.5rem",
                        full: "9999px",
                    },
                    spacing: {
                        "stack-xs": "4px",
                        "stack-sm": "8px",
                        "stack-md": "16px",
                        "stack-lg": "24px",
                        "sidebar-width": "280px",
                        "gutter": "24px",
                        "margin-mobile": "16px",
                        "margin-desktop": "32px",
                        "container-max": "1440px",
                    },
                    fontFamily: {
                        sans: ["Inter", "sans-serif"],
                    },
                    fontSize: {
                        "display-lg": ["36px", { lineHeight: "44px", letterSpacing: "-0.02em", fontWeight: "700" }],
                        "headline-md": ["24px", { lineHeight: "32px", letterSpacing: "-0.01em", fontWeight: "600" }],
                        "headline-sm": ["20px", { lineHeight: "28px", fontWeight: "600" }],
                        "body-lg": ["16px", { lineHeight: "24px", fontWeight: "400" }],
                        "body-md": ["14px", { lineHeight: "20px", fontWeight: "400" }],
                        "label-md": ["12px", { lineHeight: "16px", letterSpacing: "0.05em", fontWeight: "600" }],
                        "numeric-table": ["14px", { lineHeight: "20px", fontWeight: "500" }],
                    },
                    boxShadow: {
                        card: "0px 1px 3px rgba(0,0,0,0.05), 0px 10px 15px -3px rgba(0,0,0,0.03)",
                        modal: "0px 20px 25px -5px rgba(0,0,0,0.1), 0px 10px 10px -5px rgba(0,0,0,0.04)",
                    },
                }
            }
        }
    </script>
    <style type="text/tailwindcss">
        body { font-family: 'Inter', sans-serif; background-color: #F9FAFB; }
        .material-symbols-outlined {
            font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
            display: inline-block; vertical-align: middle;
        }
        .nav-item { @apply flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-all text-white/60 hover:bg-white/10 hover:text-white; }
        .nav-item.active { @apply bg-primary-container text-white; }
        .badge { @apply inline-flex items-center px-2.5 py-0.5 rounded-full text-label-md font-medium; }
        .badge-open { @apply bg-blue-100 text-blue-700; }
        .badge-in_progress { @apply bg-amber-100 text-amber-700; }
        .badge-resolved { @apply bg-emerald-100 text-emerald-700; }
        .badge-closed { @apply bg-gray-100 text-gray-500; }
        .badge-credit { @apply bg-emerald-100 text-emerald-700; }
        .badge-debit { @apply bg-red-100 text-red-600; }
        .badge-active { @apply bg-emerald-100 text-emerald-700; }
        .badge-frozen { @apply bg-amber-100 text-amber-700; }
        .badge-suspended { @apply bg-red-100 text-red-600; }
        .badge-low { @apply bg-gray-100 text-gray-500; }
        .badge-medium { @apply bg-blue-100 text-blue-700; }
        .badge-high { @apply bg-amber-100 text-amber-700; }
        .badge-urgent { @apply bg-red-100 text-red-700; }
        .card { @apply bg-white rounded-lg shadow-card border border-gray-100; }
        .input { @apply h-10 border border-outline-variant rounded-lg px-3 text-body-md text-on-surface bg-surface-container-lowest
                        focus:outline-none focus:ring-2 focus:ring-primary-container/20 focus:border-primary-container transition-all; }
        .btn-primary { @apply bg-primary-container hover:bg-primary text-white font-medium rounded-lg transition-all duration-200 active:scale-[0.98] shadow-sm hover:shadow-md; }
        .btn-secondary { @apply border border-outline-variant bg-white text-on-surface hover:bg-surface-container-low rounded-lg transition-all duration-200; }
        [x-cloak] { display: none !important; }
        ::-webkit-scrollbar { width: 6px; height: 6px; }
        ::-webkit-scrollbar-track { background: transparent; }
        ::-webkit-scrollbar-thumb { background: #e5bdbe; border-radius: 3px; }
        .tnum { font-variant-numeric: tabular-nums; }
    </style>
</head>
<body class="bg-gray-50 min-h-screen" x-data="{ sidebarOpen: true }">

<!-- Sidebar -->
<aside class="fixed top-0 left-0 h-full bg-navy text-white z-40 flex flex-col transition-all duration-300 overflow-hidden"
       :class="sidebarOpen ? 'w-[280px]' : 'w-[72px]'">

    <!-- Logo -->
    <div class="flex items-center gap-3 px-5 h-16 border-b border-white/10 flex-shrink-0">
        <div class="w-9 h-9 bg-primary-container rounded-lg flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
            <span class="material-symbols-outlined text-[18px]">shield</span>
        </div>
        <div class="overflow-hidden transition-all duration-300" :class="sidebarOpen ? 'w-auto opacity-100' : 'w-0 opacity-0'">
            <span class="font-bold text-base whitespace-nowrap">SCpay</span>
            <span class="ml-2 text-xs bg-primary-container/30 text-red-300 px-2 py-0.5 rounded-full">Admin</span>
        </div>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 p-3 space-y-0.5 overflow-y-auto overflow-x-hidden">
        <a href="{{ route('admin.dashboard') }}"
           class="nav-item {{ request()->routeIs('admin.dashboard') ? 'active' : '' }}"
           title="Dashboard">
            <span class="material-symbols-outlined flex-shrink-0">dashboard</span>
            <span class="transition-all duration-300" :class="sidebarOpen ? 'opacity-100 w-auto' : 'opacity-0 w-0 overflow-hidden'">Dashboard</span>
        </a>
        <a href="{{ route('admin.transactions') }}"
           class="nav-item {{ request()->routeIs('admin.transactions') ? 'active' : '' }}"
           title="Transactions">
            <span class="material-symbols-outlined flex-shrink-0">swap_horiz</span>
            <span class="transition-all duration-300" :class="sidebarOpen ? 'opacity-100 w-auto' : 'opacity-0 w-0 overflow-hidden'">Transactions</span>
        </a>
        <a href="{{ route('admin.users') }}"
           class="nav-item {{ request()->routeIs('admin.users*') ? 'active' : '' }}"
           title="Utilisateurs">
            <span class="material-symbols-outlined flex-shrink-0">group</span>
            <span class="transition-all duration-300" :class="sidebarOpen ? 'opacity-100 w-auto' : 'opacity-0 w-0 overflow-hidden'">Utilisateurs</span>
        </a>
        <a href="{{ route('admin.cards') }}"
           class="nav-item {{ request()->routeIs('admin.cards*') ? 'active' : '' }}"
           title="Cartes">
            <span class="material-symbols-outlined flex-shrink-0">credit_card</span>
            <span class="transition-all duration-300" :class="sidebarOpen ? 'opacity-100 w-auto' : 'opacity-0 w-0 overflow-hidden'">Cartes</span>
        </a>
        <a href="{{ route('admin.support') }}"
           class="nav-item relative {{ request()->routeIs('admin.support*') ? 'active' : '' }}"
           title="Support">
            <span class="material-symbols-outlined flex-shrink-0">support_agent</span>
            <span class="transition-all duration-300 flex-1" :class="sidebarOpen ? 'opacity-100 w-auto' : 'opacity-0 w-0 overflow-hidden'">Support</span>
            @php $openTickets = \App\Models\SupportTicket::whereIn('status',['open','in_progress'])->count(); @endphp
            @if($openTickets > 0)
                <span class="flex-shrink-0 bg-primary-container text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full leading-none"
                      :class="sidebarOpen ? '' : 'absolute top-1.5 right-1.5 w-4 h-4 flex items-center justify-center p-0'">
                    {{ $openTickets }}
                </span>
            @endif
        </a>
        <a href="{{ route('admin.kyc') }}"
           class="nav-item relative {{ request()->routeIs('admin.kyc*') ? 'active' : '' }}"
           title="Vérification KYC">
            <span class="material-symbols-outlined flex-shrink-0">verified_user</span>
            <span class="transition-all duration-300 flex-1" :class="sidebarOpen ? 'opacity-100 w-auto' : 'opacity-0 w-0 overflow-hidden'">Vérif. Identité</span>
            @php $pendingKyc = \App\Models\User::where('kyc_status', 'pending')->count(); @endphp
            @if($pendingKyc > 0)
                <span class="flex-shrink-0 bg-amber-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full leading-none"
                      :class="sidebarOpen ? '' : 'absolute top-1.5 right-1.5 w-4 h-4 flex items-center justify-center p-0'">
                    {{ $pendingKyc }}
                </span>
            @endif
        </a>
        <a href="{{ route('admin.fraud') }}"
           class="nav-item {{ request()->routeIs('admin.fraud') ? 'active' : '' }}"
           title="Fraude">
            <span class="material-symbols-outlined flex-shrink-0">security</span>
            <span class="transition-all duration-300" :class="sidebarOpen ? 'opacity-100 w-auto' : 'opacity-0 w-0 overflow-hidden'">Alertes Fraude</span>
        </a>
        <div class="my-2 border-t border-white/10"></div>
        <a href="{{ route('admin.export') }}"
           class="nav-item {{ request()->routeIs('admin.export') ? 'active' : '' }}"
           title="Export">
            <span class="material-symbols-outlined flex-shrink-0">download</span>
            <span class="transition-all duration-300" :class="sidebarOpen ? 'opacity-100 w-auto' : 'opacity-0 w-0 overflow-hidden'">Export</span>
        </a>
    </nav>

    <!-- Admin user + logout -->
    <div class="p-3 border-t border-white/10 flex-shrink-0">
        <div class="flex items-center gap-3 px-2 py-2 rounded-lg hover:bg-white/5 transition-all">
            <div class="w-9 h-9 bg-primary-container/40 rounded-full flex items-center justify-center text-white font-bold text-xs flex-shrink-0">
                {{ strtoupper(substr(auth('admin')->user()->name ?? 'A', 0, 2)) }}
            </div>
            <div class="flex-1 min-w-0 overflow-hidden transition-all duration-300" :class="sidebarOpen ? 'w-auto opacity-100' : 'w-0 opacity-0'">
                <p class="text-sm font-medium text-white truncate">{{ auth('admin')->user()->name ?? 'Admin' }}</p>
                <p class="text-xs text-white/40 truncate">{{ auth('admin')->user()->email ?? '' }}</p>
            </div>
            <form action="{{ route('admin.logout') }}" method="POST" class="flex-shrink-0">
                @csrf
                <button type="submit" title="Déconnexion"
                        class="text-white/40 hover:text-white transition-colors p-1 rounded-lg hover:bg-white/10">
                    <span class="material-symbols-outlined text-[18px]">logout</span>
                </button>
            </form>
        </div>
    </div>
</aside>

<!-- Main content -->
<div class="transition-all duration-300" :class="sidebarOpen ? 'ml-[280px]' : 'ml-[72px]'">

    <!-- Top bar -->
    <header class="bg-white border-b border-gray-200 px-6 h-16 flex items-center gap-4 sticky top-0 z-30 shadow-sm">
        <button @click="sidebarOpen = !sidebarOpen"
                class="w-9 h-9 flex items-center justify-center rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-all">
            <span class="material-symbols-outlined text-[20px]">menu</span>
        </button>
        <h1 class="font-semibold text-on-surface text-headline-sm">@yield('page-title', 'Dashboard')</h1>
        <div class="ml-auto flex items-center gap-3">
            <span class="text-label-md text-on-surface-variant">{{ now()->format('d/m/Y • H:i') }}</span>
            @if(session('success'))
                <div class="flex items-center gap-1.5 bg-emerald-50 text-emerald-700 text-body-md px-3 py-1.5 rounded-lg border border-emerald-200">
                    <span class="material-symbols-outlined text-[16px]">check_circle</span>
                    {{ session('success') }}
                </div>
            @endif
            @if(session('error'))
                <div class="flex items-center gap-1.5 bg-red-50 text-red-600 text-body-md px-3 py-1.5 rounded-lg border border-red-200">
                    <span class="material-symbols-outlined text-[16px]">error</span>
                    {{ session('error') }}
                </div>
            @endif
        </div>
    </header>

    <!-- Page content -->
    <main class="p-gutter w-full">
        @yield('content')
    </main>
</div>

@stack('scripts')
</body>
</html>
