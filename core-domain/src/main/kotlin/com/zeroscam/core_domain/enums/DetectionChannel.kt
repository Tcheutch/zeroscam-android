package com.zeroscam.core_domain.enums

/**
 * Canal par lequel une menace est détectée.
 */
enum class DetectionChannel {
    CALL,
    MESSAGE,
    PAYMENT,
    DEVICE,
    AWARENESS,
}
