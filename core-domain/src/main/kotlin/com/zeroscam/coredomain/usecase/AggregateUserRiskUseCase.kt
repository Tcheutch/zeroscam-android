package com.zeroscam.coredomain.usecase

import com.zeroscam.coredomain.enums.DetectionChannel
import com.zeroscam.coredomain.enums.RiskLevel
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.value.UserId

/**
 * Vue agrégée du risque utilisateur sur plusieurs canaux
 * (message, appel, paiement, device, etc.).
 */
data class AggregatedUserRisk(
    val userId: UserId,
    val riskLevel: RiskLevel,
    val confidenceScore: Double,
    val sources: List<DetectionResult>,
)

/**
 * Configuration de l'agrégateur de risque utilisateur.
 *
 * Permet :
 *  - d'appliquer des poids par canal,
 *  - d'externaliser les seuils de confiance / déclenchement.
 *
 * Les valeurs par défaut reproduisent le comportement V2 actuel.
 */
data class AggregateUserRiskConfig(
    val channelWeights: Map<DetectionChannel, Double> =
        DetectionChannel
            .values()
            .associateWith { 1.0 },
    val multiHighScoreThreshold: Double = 2.0,
    val minConfidenceMultiHigh: Double = 0.95,
    val minConfidenceMultiChannel: Double = 0.90,
    val minConfidenceAllLowMultiChannel: Double = 0.70,
)

/**
 * Agrège une liste de [DetectionResult] en un [AggregatedUserRisk].
 *
 * Heuristiques V2 multi-canal :
 *
 *  - Mono-signal :
 *      → on propage simplement le risque,
 *        avec clamp de la confiance dans [0.0, 1.0].
 *
 *  - Baseline :
 *      → max(riskLevel) sur l’ensemble des signaux.
 *
 *  - Pattern "multi-HIGH" (pondéré par canal) :
 *      → score = Σ(weight(channel) pour les signaux HIGH/CRITICAL)
 *      → si score ≥ [multiHighScoreThreshold] ou au moins 1 CRITICAL :
 *          * bump d’un niveau (max HIGH → CRITICAL)
 *          * confiance min [minConfidenceMultiHigh].
 *
 *  - Pattern "multi-canal significatif" :
 *      → ≥ 3 canaux distincts impliqués
 *      → et au moins un signal à partir de MEDIUM
 *      → bump d’un niveau supplémentaire (sans dépasser CRITICAL)
 *      → confiance min [minConfidenceMultiChannel] si inférieure.
 *
 *  - Pattern "beaucoup de LOW" :
 *      → tous les signaux LOW sur ≥ 2 canaux différents
 *      → on ne reste pas en LOW : on remonte au moins à MEDIUM
 *      → confiance min [minConfidenceAllLowMultiChannel].
 */
class AggregateUserRiskUseCase(
    private val config: AggregateUserRiskConfig = AggregateUserRiskConfig(),
) {
    operator fun invoke(detections: List<DetectionResult>): AggregatedUserRisk {
        require(detections.isNotEmpty()) {
            "detections must not be empty"
        }

        val userIds: Set<UserId> = detections.map { it.userId }.toSet()
        require(userIds.size == 1) {
            "All detections must belong to the same userId, but found: $userIds"
        }

        val userId: UserId = userIds.first()
        val sources: List<DetectionResult> = detections.sortedBy { it.createdAt }

        val maxRisk: RiskLevel = sources.maxOf { it.riskLevel }
        val maxConfidence: Double = sources.maxOf { it.confidenceScore }

        val distinctChannels: Set<DetectionChannel> =
            sources.map { it.channel }.toSet()

        val highOrAboveScore: Double =
            sources
                .filter { it.riskLevel >= RiskLevel.HIGH }
                .sumOf { detection ->
                    config.channelWeights[detection.channel] ?: 1.0
                }

        val criticalCount: Int =
            sources.count { it.riskLevel == RiskLevel.CRITICAL }

        var aggregatedRisk: RiskLevel = maxRisk
        var aggregatedConfidence: Double = maxConfidence

        // Cas mono-signal : neutre, pas d’heuristique agressive,
        // mais clamp de la confiance pour rester dans [0.0, 1.0].
        if (sources.size == 1) {
            val clampedSingle: Double =
                aggregatedConfidence.coerceIn(0.0, 1.0)
            return AggregatedUserRisk(
                userId = userId,
                riskLevel = aggregatedRisk,
                confidenceScore = clampedSingle,
                sources = sources,
            )
        }

        // ≥ 2 HIGH/CRITICAL (pondéré) ou ≥ 1 CRITICAL → escalade agressive.
        val multiHighPattern: Boolean =
            highOrAboveScore >= config.multiHighScoreThreshold ||
                criticalCount >= 1

        if (multiHighPattern) {
            aggregatedRisk = bumpRisk(aggregatedRisk)
            aggregatedConfidence =
                maxOf(aggregatedConfidence, config.minConfidenceMultiHigh)
        }

        // ≥ 3 canaux distincts avec risque au moins MEDIUM → renforcement multi-canal.
        val hasMultiChannelMediumOrAbove: Boolean =
            distinctChannels.size >= 3 &&
                sources.any { it.riskLevel >= RiskLevel.MEDIUM }

        if (hasMultiChannelMediumOrAbove) {
            aggregatedRisk = bumpRisk(aggregatedRisk)
            aggregatedConfidence =
                maxOf(aggregatedConfidence, config.minConfidenceMultiChannel)
        }

        // Plusieurs LOW sur canaux différents → on ne reste pas en LOW silencieux.
        val allLow: Boolean = sources.all { it.riskLevel == RiskLevel.LOW }
        if (allLow && distinctChannels.size >= 2) {
            aggregatedRisk = RiskLevel.MEDIUM
            aggregatedConfidence =
                maxOf(aggregatedConfidence, config.minConfidenceAllLowMultiChannel)
        }

        // On borne la confiance dans [0.0, 1.0] par sécurité.
        val clampedConfidence: Double =
            aggregatedConfidence.coerceIn(0.0, 1.0)

        return AggregatedUserRisk(
            userId = userId,
            riskLevel = aggregatedRisk,
            confidenceScore = clampedConfidence,
            sources = sources,
        )
    }

    private fun bumpRisk(riskLevel: RiskLevel): RiskLevel =
        when (riskLevel) {
            RiskLevel.LOW -> RiskLevel.MEDIUM
            RiskLevel.MEDIUM -> RiskLevel.HIGH
            RiskLevel.HIGH -> RiskLevel.CRITICAL
            RiskLevel.CRITICAL -> RiskLevel.CRITICAL
        }
}
