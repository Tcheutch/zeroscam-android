package com.zeroscam.core_domain.enums

/**
 * Catégorie fonctionnelle de l'arnaque.
 * Sert au reporting, aux règles métier et à l'explicabilité.
 */
enum class ScamType {
    GENERIC,
    PHISHING,
    VISHING,
    SMISHING,
    ROMANCE,
    INVESTMENT,
    IMPERSONATION,
    TECH_SUPPORT,
    OTP_RELAY,
    PAYMENT_INTERCEPTION,
    IDENTITY_THEFT,
}
