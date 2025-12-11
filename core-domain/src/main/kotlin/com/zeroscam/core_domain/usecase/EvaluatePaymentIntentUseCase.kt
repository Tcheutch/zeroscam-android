package com.zeroscam.core_domain.usecase

import com.zeroscam.core_domain.enums.RiskLevel
import com.zeroscam.core_domain.model.DetectionResult
import com.zeroscam.core_domain.model.PaymentIntent
import com.zeroscam.core_domain.ports.DetectionLogRepository
import com.zeroscam.core_domain.ports.PaymentRiskEngine
import com.zeroscam.core_domain.ports.ThreatIntelRepository
import kotlin.math.max

/**
 * Use case : évaluation d'une intention de paiement.
 *
 * Pipeline :
 * 1. Score initial via PaymentRiskEngine (features, modèles Vigilis-like).
 * 2. Ajustement via Threat Intel (destinataires blacklistés, patterns).
 * 3. Escalade forte si destination connue comme frauduleuse.
 * 4. Journalisation de la détection.
 */
class EvaluatePaymentIntentUseCase(
    private val paymentRiskEngine: PaymentRiskEngine,
    private val threatIntelRepository: ThreatIntelRepository,
    private val detectionLogRepository: DetectionLogRepository,
) {
    operator fun invoke(paymentIntent: PaymentIntent): DetectionResult {
        val baseResult = paymentRiskEngine.analyze(paymentIntent)

        val intelAdjusted =
            threatIntelRepository.adjustPaymentResult(
                paymentIntent = paymentIntent,
                initialResult = baseResult,
            )

        val escalated =
            if (threatIntelRepository.isKnownScamPaymentDestination(
                    iban = null,
                    walletAddress = paymentIntent.recipientAccount,
                ) &&
                intelAdjusted.riskLevel < RiskLevel.HIGH
            ) {
                intelAdjusted.copy(
                    riskLevel = RiskLevel.HIGH,
                    confidenceScore = max(intelAdjusted.confidenceScore, 0.9),
                )
            } else {
                intelAdjusted
            }

        detectionLogRepository.logPaymentDetection(
            paymentIntent = paymentIntent,
            result = escalated,
        )

        return escalated
    }
}
