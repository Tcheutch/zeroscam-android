package com.zeroscam.coredata.repository

import com.zeroscam.coredata.db.dao.ThreatIntelDao
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.model.PaymentIntent
import com.zeroscam.coredomain.model.PhoneCall
import com.zeroscam.coredomain.ports.ThreatIntelRepository

class RoomThreatIntelRepository(
    private val threatIntelDao: ThreatIntelDao,
) : ThreatIntelRepository {
    override fun adjustCallResult(
        call: PhoneCall,
        initialResult: DetectionResult,
    ): DetectionResult = initialResult

    override fun adjustMessageResult(
        message: Message,
        initialResult: DetectionResult,
    ): DetectionResult = initialResult

    override fun adjustPaymentResult(
        paymentIntent: PaymentIntent,
        initialResult: DetectionResult,
    ): DetectionResult = initialResult

    override fun adjustDeviceSecurityResult(
        snapshot: DeviceSecuritySnapshot,
        initialResult: DetectionResult,
    ): DetectionResult = initialResult

    override fun isKnownScamPhone(phoneNumber: String): Boolean =
        threatIntelDao.exists(type = TYPE_PHONE, value = phoneNumber) > 0

    override fun isKnownScamSender(sender: String): Boolean =
        threatIntelDao.exists(type = TYPE_SENDER, value = sender) > 0

    override fun isKnownScamUrl(url: String): Boolean = threatIntelDao.exists(type = TYPE_URL, value = url) > 0

    override fun isKnownScamPaymentDestination(
        iban: String?,
        walletAddress: String?,
    ): Boolean {
        val ibanHit = iban != null && threatIntelDao.exists(type = TYPE_IBAN, value = iban) > 0
        val walletHit = walletAddress != null && threatIntelDao.exists(type = TYPE_WALLET, value = walletAddress) > 0
        return ibanHit || walletHit
    }

    private companion object {
        private const val TYPE_PHONE: String = "PHONE"
        private const val TYPE_SENDER: String = "SENDER"
        private const val TYPE_URL: String = "URL"
        private const val TYPE_IBAN: String = "IBAN"
        private const val TYPE_WALLET: String = "WALLET"
    }
}
