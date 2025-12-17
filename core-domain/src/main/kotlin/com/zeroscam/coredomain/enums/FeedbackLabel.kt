package com.zeroscam.coredomain.enums

/**
 * Feedback utilisateur / analyste sur une décision de ZeroScam.
 *
 * Convention :
 *
 *  - isScam == true  → l'utilisateur/analyste considère le cas comme frauduleux.
 *  - isScam == false → le cas est considéré comme légitime.
 *  - isScam == null  → l'utilisateur ne sait pas / ne se prononce pas.
 *
 * La combinaison (isScam, label) permet à ZeroScam-Research
 * de recalibrer les seuils et d'enrichir les datasets d'entraînement.
 *
 */
enum class FeedbackLabel {
    /**
     *  // --- Labels "classiques" de classification binaire ---
     * Le moteur a signalé un SCAM et l'utilisateur confirme.
     * Exemple : message signalé scam et effectivement frauduleux.
     */
    TRUE_POSITIVE,

    /**
     * Le moteur a signalé un SCAM mais l'utilisateur dit que c'est légitime.
     * Exemple : appel bloqué mais c'était la banque.
     */
    FALSE_POSITIVE,

    /**
     * Le moteur n'a pas signalé de SCAM et l'utilisateur confirme que tout est OK.
     */
    TRUE_NEGATIVE,

    /**
     * Le moteur n'a pas signalé de SCAM mais l'utilisateur rapporte une fraude.
     * Exemple critique pour le retraining.
     */
    FALSE_NEGATIVE,

    /**
     * L'utilisateur ne sait pas / ne comprend pas la décision.
     */
    UNKNOWN,

    // --- Labels "riches" pour ZeroScam-Research ---

    /**
     * Cas frauduleux confirmé, mais pattern non (ou mal) couvert par les règles / modèles actuels.
     * Typiquement : combinaison de signaux inédite.
     *
     * Souvent associé à isScam == true.
     */
    NEW_SCAM_PATTERN,

    /**
     * Cas borderline qui semble légitime mais
     * où l'utilisateur comprend pourquoi c'est suspect.
     *
     * Exemple : message de la banque avec wording très agressif.
     * Souvent associé à isScam == false.
     */
    BORDERLINE_BUT_SAFE,

    /**
     * Le moteur a été beaucoup trop agressif.
     *
     * Exemple : appel/messagerie/transaction légitime bloquée
     * avec impact UX important (perte de temps, argent, etc.).
     * Souvent un sous-ensemble des FALSE_POSITIVE.
     */
    OVERBLOCKING,

    /**
     * Le moteur a été trop laxiste.
     *
     * Exemple : comportement très suspect mais seulement classé MEDIUM / HIGH
     * ou laissé passer alors que le user a frôlé la fraude.
     * Souvent un sous-ensemble des FALSE_NEGATIVE.
     */
    UNDERBLOCKING,

    /**
     * L'utilisateur n'est pas sûr, mais le cas reste suspicieux
     * (souvent utilisé par un analyste ou un système semi-auto).
     */
    SUSPICIOUS_BUT_UNCONFIRMED,
}
