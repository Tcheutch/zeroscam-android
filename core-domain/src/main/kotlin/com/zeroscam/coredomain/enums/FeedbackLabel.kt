package com.zeroscam.coredomain.enums

/**
 * Label de vérité terrain envoyé par l'utilisateur après une détection.
 *
 * - TRUE_POSITIVE  : ZeroScam a bien détecté un scam.
 * - FALSE_POSITIVE : ZeroScam a détecté un scam alors que ce n'en était pas un.
 * - TRUE_NEGATIVE  : ZeroScam a bien considéré que ce n'était pas un scam.
 * - FALSE_NEGATIVE : ZeroScam a raté un scam (cas le plus critique pour le moteur).
 */
enum class FeedbackLabel {
    TRUE_POSITIVE,
    FALSE_POSITIVE,
    TRUE_NEGATIVE,
    FALSE_NEGATIVE,
}
