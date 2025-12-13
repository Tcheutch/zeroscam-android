package com.zeroscam.coredomain.ports

import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.model.PaymentIntent
import com.zeroscam.coredomain.model.PhoneCall

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
