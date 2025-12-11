package com.zeroscam.core_domain.ports

import com.zeroscam.core_domain.model.DetectionResult
import com.zeroscam.core_domain.model.DeviceSecuritySnapshot
import com.zeroscam.core_domain.model.Message
import com.zeroscam.core_domain.model.PaymentIntent
import com.zeroscam.core_domain.model.PhoneCall

interface ThreatIntelRepository {
    fun adjustCallResult(
        call: PhoneCall,
        initialResult: DetectionResult,
    ): DetectionResult

    fun adjustMessageResult(
        message: Message,
        initialResult: DetectionResult,
    ): DetectionResult

    fun adjustPaymentResult(
        paymentIntent: PaymentIntent,
        initialResult: DetectionResult,
    ): DetectionResult

    fun adjustDeviceSecurityResult(
        snapshot: DeviceSecuritySnapshot,
        initialResult: DetectionResult,
    ): DetectionResult

    fun isKnownScamPhone(phoneNumber: String): Boolean

    fun isKnownScamSender(sender: String): Boolean

    fun isKnownScamUrl(url: String): Boolean

    fun isKnownScamPaymentDestination(
        iban: String?,
        walletAddress: String?,
    ): Boolean
}
