package com.zeroscam.core_domain.usecase

import com.zeroscam.core_domain.enums.RiskLevel
import com.zeroscam.core_domain.model.DetectionResult
import com.zeroscam.core_domain.model.PhoneCall
import com.zeroscam.core_domain.ports.CallScamDetector
import com.zeroscam.core_domain.ports.DetectionLogRepository
import com.zeroscam.core_domain.ports.ThreatIntelRepository

/**
 * Use case : analyse d'un appel entrant.
 *
 * Pipeline :
 * 1. Détection de base via moteur ML/règles (CallScamDetector).
 * 2. Ajustement via Threat Intel (numéros blacklistés, IOC connus).
 * 3. Escalade si numéro connu comme scam (au moins HIGH + confiance ≥ 0.9).
 * 4. Journalisation de la détection.
 */
class AnalyzeIncomingCallUseCase(
    private val callScamDetector: CallScamDetector,
    private val threatIntelRepository: ThreatIntelRepository,
    private val detectionLogRepository: DetectionLogRepository,
) {
    operator fun invoke(call: PhoneCall): DetectionResult {
        // 1) Résultat brut (ML + règles)
        val baseResult = callScamDetector.analyze(call)

        // 2) Ajustement par la threat intel (neutre ou renforcement)
        val intelAdjusted =
            threatIntelRepository.adjustCallResult(
                call = call,
                initialResult = baseResult,
            )

        // 3) Escalade forte si numéro connu comme scam
        val escalated =
            if (threatIntelRepository.isKnownScamPhone(call.phoneNumber) &&
                intelAdjusted.riskLevel < RiskLevel.HIGH
            ) {
                intelAdjusted.copy(
                    riskLevel = RiskLevel.HIGH,
                    confidenceScore = maxOf(intelAdjusted.confidenceScore, 0.9),
                )
            } else {
                intelAdjusted
            }

        // 4) Log pour traçabilité & amélioration continue
        detectionLogRepository.logCallDetection(
            call = call,
            result = escalated,
        )

        return escalated
    }
}
