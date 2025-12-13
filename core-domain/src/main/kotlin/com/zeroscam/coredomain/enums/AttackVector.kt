package com.zeroscam.coredomain.enums

/**
 * Vecteur technique / tactique de l'attaque.
 * Ici on intègre explicitement jailbreaking + hash jacking.
 */
enum class AttackVector {
    SOCIAL_ENGINEERING,
    VISHING,
    SMISHING,
    PHISHING_EMAIL,
    INVESTMENT_FRAUD,
    ROMANCE_SCAM,
    ACCOUNT_TAKEOVER,
    JAILBREAKING,
    HASH_JACKING,
    MALWARE,
    OTHER,
}
