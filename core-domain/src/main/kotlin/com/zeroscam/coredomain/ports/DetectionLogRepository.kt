package com.zeroscam.coredomain.ports

import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.model.PaymentIntent
import com.zeroscam.coredomain.model.PhoneCall

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
