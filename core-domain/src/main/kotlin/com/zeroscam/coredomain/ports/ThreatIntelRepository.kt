package com.zeroscam.coredomain.ports

import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.model.PaymentIntent
import com.zeroscam.coredomain.model.PhoneCall

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
