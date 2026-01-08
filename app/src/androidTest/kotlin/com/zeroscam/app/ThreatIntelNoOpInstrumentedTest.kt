package com.zeroscam.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.model.PaymentIntent
import com.zeroscam.coredomain.model.PhoneCall
import com.zeroscam.coredomain.ports.ThreatIntelRepository
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThreatIntelNoOpInstrumentedTest {
    @Test
    fun threatIntel_adjusters_should_be_passthrough() {
        val repo: ThreatIntelRepository = DebugThreatIntelRepositoryFactory.createNoOp()

        val dummyResult = DummyFactory.result()
        assertSame(dummyResult, repo.adjustMessageResult(DummyFactory.message(), dummyResult))
        assertSame(dummyResult, repo.adjustCallResult(DummyFactory.call(), dummyResult))
        assertSame(dummyResult, repo.adjustPaymentResult(DummyFactory.payment(), dummyResult))
        assertSame(dummyResult, repo.adjustDeviceSecurityResult(DummyFactory.device(), dummyResult))
    }

    private object DebugThreatIntelRepositoryFactory {
        fun createNoOp(): ThreatIntelRepository =
            object : ThreatIntelRepository {
                override fun adjustCallResult(
                    call: PhoneCall,
                    initialResult: DetectionResult,
                ) = initialResult

                override fun adjustMessageResult(
                    message: Message,
                    initialResult: DetectionResult,
                ) = initialResult

                override fun adjustPaymentResult(
                    paymentIntent: PaymentIntent,
                    initialResult: DetectionResult,
                ) = initialResult

                override fun adjustDeviceSecurityResult(
                    snapshot: DeviceSecuritySnapshot,
                    initialResult: DetectionResult,
                ) = initialResult

                override fun isKnownScamPhone(phoneNumber: String) = false

                override fun isKnownScamSender(sender: String) = false

                override fun isKnownScamUrl(url: String) = false

                override fun isKnownScamPaymentDestination(
                    iban: String?,
                    walletAddress: String?,
                ) = false
            }
    }
}
