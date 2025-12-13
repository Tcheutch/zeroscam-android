package com.zeroscam.coredomain.enums

/**
 * Niveau de risque d'une interaction (appel, message, paiement, etc.).
 *
 * LOW     : aucun signal fort, risque limité.
 * MEDIUM  : signaux suspects, prudence recommandée.
 * HIGH    : forte probabilité de scam, action immédiate conseillée.
 * CRITICAL: attaque quasi certaine, blocage recommandé.
 */
enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
}
