package com.stephan.mobil.ui.theme

import androidx.compose.ui.graphics.Color

// ================================================================
// SCpay — Palette centralisée
// Pour changer le thème : modifiez UNIQUEMENT la section "BRAND"
// ================================================================

// --- BRAND (modifiez ici pour rethémer toute l'app) -------------
val BrandPrimary        = Color(0xFF6D28D9)   // Violet 700 — accent principal
val BrandPrimaryDark    = Color(0xFF5B21B6)   // Violet 800 — état pressé
val BrandPrimaryLight   = Color(0xFF7C3AED)   // Violet 600 — hover/focus
val BrandPrimarySoft    = Color(0x1A6D28D9)   // Violet 10% — surfaces teintées
val BrandPrimaryGlow    = Color(0x406D28D9)   // Violet 25% — ombres/glow
val BrandOnPrimary      = Color(0xFFFFFFFF)   // Texte sur bouton primary

// --- SEMANTIC ---------------------------------------------------
val SemanticSuccess     = Color(0xFF5DBB82)   // Crédit / Succès
val SemanticDanger      = Color(0xFFCB5961)   // Débit / Erreur
val SemanticWarning     = Color(0xFFF4A428)   // Attention
val SemanticInfo        = Color(0xFF5B8DEF)   // Info / Blue

// --- BACKGROUNDS (stack de profondeur) -------------------------
val BgDeep              = Color(0xFF080A0F)   // Fond le plus sombre
val BgBase              = Color(0xFF101114)   // Fond principal écrans
val BgSurface           = Color(0xFF17181C)   // Cartes niveau 1
val BgSurfaceElevated   = Color(0xFF1E2022)   // Cartes niveau 2
val BgSurfaceHigh       = Color(0xFF282A2D)   // Éléments interactifs
val BgSurfaceTop        = Color(0xFF333537)   // Bordures / séparateurs

// --- TEXT -------------------------------------------------------
val TextPrimary         = Color(0xFFE2E2E5)   // Texte principal
val TextSecondary       = Color(0xFF8B8F98)   // Texte secondaire / muted
val TextOnPrimary       = Color(0xFFFFFFFF)   // Texte sur fond primary
val TextLink            = BrandPrimary        // Liens et "Voir tout"

// --- STATES / MISC ----------------------------------------------
val SoftBackground      = Color(0xFFF4F5F7)   // Fond clair (light mode)
val LineColor           = Color(0xFFECEEF2)   // Séparateurs light mode
val LightBackground     = Color(0xFFF7F8FA)   // Fond inputs light mode

// ================================================================
// Aliases pour compatibilité (ne pas modifier)
// ================================================================
val ObsidianBlack   = BgDeep
val DarkSlate       = BgSurface
val MutedSlate      = BgSurfaceElevated
val LightSlate      = TextSecondary
val PremiumWhite    = TextPrimary
val NeonEmerald     = SemanticSuccess
val BrandCrimson    = SemanticDanger

// Aliases Material legacy
val Purple80        = BrandPrimaryLight
val PurpleGrey80    = BgSurfaceElevated
val Pink80          = SemanticDanger
val Purple40        = BrandPrimaryDark
val PurpleGrey40    = BgBase
val Pink40          = Color(0xFFB3261E)
