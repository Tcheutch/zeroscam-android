package com.zeroscam.core_domain.ports

import com.zeroscam.core_domain.model.DetectionResult
import com.zeroscam.core_domain.model.DeviceSecuritySnapshot
import com.zeroscam.core_domain.model.Message
import com.zeroscam.core_domain.model.PaymentIntent
import com.zeroscam.core_domain.model.PhoneCall

interface CallScamDetector {
    fun analyze(call: PhoneCall): DetectionResult
}

interface MessageScamDetector {
    fun analyze(message: Message): DetectionResult
}

interface PaymentRiskEngine {
    fun analyze(paymentIntent: PaymentIntent): DetectionResult
}

interface DeviceThreatDetector {
    fun analyze(snapshot: DeviceSecuritySnapshot): DetectionResult
}
