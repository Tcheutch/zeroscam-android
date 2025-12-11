package com.zeroscam.core_domain.ports

import com.zeroscam.core_domain.model.DetectionResult
import com.zeroscam.core_domain.model.DeviceSecuritySnapshot
import com.zeroscam.core_domain.model.Message
import com.zeroscam.core_domain.model.PaymentIntent
import com.zeroscam.core_domain.model.PhoneCall

interface DetectionLogRepository {
    fun logCallDetection(
        call: PhoneCall,
        result: DetectionResult,
    )

    fun logMessageDetection(
        message: Message,
        result: DetectionResult,
    )

    fun logPaymentDetection(
        paymentIntent: PaymentIntent,
        result: DetectionResult,
    )

    fun logDeviceSecurityDetection(
        snapshot: DeviceSecuritySnapshot,
        result: DetectionResult,
    )
}
