package com.zeroscam.core_domain.enums

/**
 * Canal de paiement analysé par PaymentGuardian.
 */
enum class PaymentChannel {
    MOBILE_MONEY,
    BANK_TRANSFER,
    CARD,
    CRYPTO,
    CASH_IN_APP,
    OTHER,
}
