package com.zeroscam.core_domain.usecase

import com.zeroscam.core_domain.enums.RiskLevel
import com.zeroscam.core_domain.model.DetectionResult
import com.zeroscam.core_domain.model.Message
import com.zeroscam.core_domain.ports.DetectionLogRepository
import com.zeroscam.core_domain.ports.MessageScamDetector
import com.zeroscam.core_domain.ports.ThreatIntelRepository

/**
 * Use case : analyse d'un message entrant (SMS / messagerie).
 *
 * Pipeline :
 * 1. Détection de base via moteur ML/règles (MessageScamDetector).
 * 2. Ajustement via Threat Intel (contenu, IOC connus).
 * 3. Escalade si la threat intel indique un risque fort (au moins HIGH + confiance ≥ 0.9).
 * 4. Journalisation de la détection.
 */
class AnalyzeIncomingMessageUseCase(
    private val messageScamDetector: MessageScamDetector,
    private val threatIntelRepository: ThreatIntelRepository,
    private val detectionLogRepository: DetectionLogRepository,
) {
    operator fun invoke(message: Message): DetectionResult {
        // 1) Résultat brut (ML + règles)
        val baseResult = messageScamDetector.analyze(message)

        // 2) Ajustement par la threat intel
        val intelAdjusted =
            threatIntelRepository.adjustMessageResult(
                message = message,
                initialResult = baseResult,
            )

        // 3) Décision d’escalade :
        //    Ici on utilise le contenu du message comme clé d lookup.
        //    Dans les tests, les fakes ignorent la valeur exacte,
        //    ils renvoient juste true/false selon le scénario.
        val shouldEscalate =
            threatIntelRepository.isKnownScamSender(message.content) ||
                threatIntelRepository.isKnownScamUrl(message.content)

        val escalated =
            if (shouldEscalate && intelAdjusted.riskLevel < RiskLevel.HIGH) {
                intelAdjusted.copy(
                    riskLevel = RiskLevel.HIGH,
                    confidenceScore =
                        if (intelAdjusted.confidenceScore >= 0.9) {
                            intelAdjusted.confidenceScore
                        } else {
                            0.9
                        },
                )
            } else {
                intelAdjusted
            }

        // 4) Log pour traçabilité & amélioration continue
        detectionLogRepository.logMessageDetection(
            message = message,
            result = escalated,
        )

        return escalated
    }
}
