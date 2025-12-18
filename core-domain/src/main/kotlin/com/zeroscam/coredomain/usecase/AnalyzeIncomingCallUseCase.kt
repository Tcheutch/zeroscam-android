package com.zeroscam.coredomain.usecase

import com.zeroscam.coredomain.enums.RiskLevel
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.PhoneCall
import com.zeroscam.coredomain.ports.CallScamDetector
import com.zeroscam.coredomain.ports.DetectionLogRepository
import com.zeroscam.coredomain.ports.ThreatIntelRepository

/**
 * Use case : analyse d'un appel entrant (ZeroScam / Vigilis-like).
 *
 * Pipeline v2 "béton" :
 *  1. Score brut via [CallScamDetector] (ML + règles).
 *  2. Ajustement via [ThreatIntelRepository.adjustCallResult].
 *  3. Escalade déterministe :
 *      - numéro connu scam (threat intel),
 *      - reason `caller_known_scam`,
 *      - numérotation suspecte (malformed, high risk prefix, spoofing).
 *  4. Journalisation structurée via [DetectionLogRepository].
 */
class AnalyzeIncomingCallUseCase(
    private val callScamDetector: CallScamDetector,
    private val threatIntelRepository: ThreatIntelRepository,
    private val detectionLogRepository: DetectionLogRepository,
) {
    operator fun invoke(call: PhoneCall): DetectionResult {
        // 1) Résultat brut
        val baseResult = callScamDetector.analyze(call)

        // 2) Ajustement threat-intel
        val intelAdjusted =
            threatIntelRepository.adjustCallResult(
                call = call,
                initialResult = baseResult,
            )

        // 3.a) Escalade forte pour numéro connu scam
        val afterScamIndicators =
            escalateForKnownScamIndicators(
                call = call,
                currentResult = intelAdjusted,
            )

        // 3.b) Escalade pour signaux de métadonnées
        val afterMetadataEscalation =
            escalateForMetadataSignals(
                currentResult = afterScamIndicators,
            )

        // 4) Journalisation
        detectionLogRepository.logCallDetection(
            call = call,
            result = afterMetadataEscalation,
        )

        return afterMetadataEscalation
    }

    /**
     * Escalade si le numéro est connu comme scam via threat-intel,
     * et/ou si un reason explicite `caller_known_scam` est présent.
     */
    private fun escalateForKnownScamIndicators(
        call: PhoneCall,
        currentResult: DetectionResult,
    ): DetectionResult {
        val isKnownScamNumber = threatIntelRepository.isKnownScamPhone(call.phoneNumber)
        val hasScamReason = currentResult.reasons.contains(REASON_CALLER_KNOWN_SCAM)

        if (!isKnownScamNumber && !hasScamReason) {
            return currentResult
        }

        val hasBoth = isKnownScamNumber && hasScamReason

        val targetRisk =
            when {
                hasBoth ->
                    when (currentResult.riskLevel) {
                        RiskLevel.LOW,
                        RiskLevel.MEDIUM,
                        RiskLevel.HIGH,
                        -> RiskLevel.CRITICAL
                        RiskLevel.CRITICAL -> RiskLevel.CRITICAL
                    }

                else ->
                    when (currentResult.riskLevel) {
                        RiskLevel.LOW -> RiskLevel.HIGH
                        RiskLevel.MEDIUM -> RiskLevel.HIGH
                        RiskLevel.HIGH -> RiskLevel.HIGH
                        RiskLevel.CRITICAL -> RiskLevel.CRITICAL
                    }
            }

        val targetConfidence =
            if (hasBoth) {
                maxOf(currentResult.confidenceScore, 0.97)
            } else {
                maxOf(currentResult.confidenceScore, 0.90)
            }

        val extraReasons = mutableListOf<String>()
        extraReasons += REASON_ESCALATION_CALLER_SCAM

        return currentResult.copy(
            riskLevel = targetRisk,
            confidenceScore = targetConfidence,
            reasons = currentResult.reasons + extraReasons,
        )
    }

    /**
     * Escalade basée sur les signaux de métadonnées :
     *
     *  - `caller_number_malformed`
     *  - `high_risk_country_prefix`
     *  - `call_spoofing_suspected`
     *
     * Contrat :
     *  - un seul signal → bumpRisk + confiance ≥ 0.85
     *  - ≥ 2 signaux → bumpRisk x2 + confiance ≥ 0.92
     */
    private fun escalateForMetadataSignals(currentResult: DetectionResult): DetectionResult {
        val reasons = currentResult.reasons.toSet()

        val hasMalformed = reasons.contains(REASON_CALLER_NUMBER_MALFORMED)
        val hasHighRiskPrefix = reasons.contains(REASON_HIGH_RISK_COUNTRY_PREFIX)
        val hasSpoofing = reasons.contains(REASON_CALL_SPOOFING_SUSPECTED)

        val metadataSignalsCount =
            listOf(
                hasMalformed,
                hasHighRiskPrefix,
                hasSpoofing,
            ).count { it }

        if (metadataSignalsCount == 0) {
            return currentResult
        }

        var risk = bumpRisk(currentResult.riskLevel)
        var confidence = maxOf(currentResult.confidenceScore, 0.85)
        val extraReasons = mutableListOf<String>()

        if (metadataSignalsCount >= 2) {
            risk = bumpRisk(risk)
            confidence = maxOf(confidence, 0.92)
        }

        if (hasMalformed) {
            extraReasons += REASON_ESCALATION_CALLER_NUMBER_MALFORMED
        }
        if (hasHighRiskPrefix) {
            extraReasons += REASON_ESCALATION_HIGH_RISK_PREFIX
        }
        if (hasSpoofing) {
            extraReasons += REASON_ESCALATION_CALL_SPOOFING
        }

        return currentResult.copy(
            riskLevel = risk,
            confidenceScore = confidence,
            reasons = currentResult.reasons + extraReasons,
        )
    }

    private fun bumpRisk(riskLevel: RiskLevel): RiskLevel =
        when (riskLevel) {
            RiskLevel.LOW -> RiskLevel.MEDIUM
            RiskLevel.MEDIUM -> RiskLevel.HIGH
            RiskLevel.HIGH -> RiskLevel.CRITICAL
            RiskLevel.CRITICAL -> RiskLevel.CRITICAL
        }

    companion object {
        // Reasons produits par le moteur / threat-intel
        const val REASON_CALLER_KNOWN_SCAM = "caller_known_scam"
        const val REASON_CALLER_NUMBER_MALFORMED = "caller_number_malformed"
        const val REASON_HIGH_RISK_COUNTRY_PREFIX = "high_risk_country_prefix"
        const val REASON_CALL_SPOOFING_SUSPECTED = "call_spoofing_suspected"

        // Reasons ajoutés par ce use case lors des escalades
        private const val REASON_ESCALATION_CALLER_SCAM =
            "escalation_caller_known_scam"
        private const val REASON_ESCALATION_CALLER_NUMBER_MALFORMED =
            "escalation_caller_number_malformed"
        private const val REASON_ESCALATION_HIGH_RISK_PREFIX =
            "escalation_high_risk_country_prefix"
        private const val REASON_ESCALATION_CALL_SPOOFING =
            "escalation_call_spoofing_suspected"
    }
}
