package com.zeroscam.core_domain.model

import com.zeroscam.core_domain.enums.DetectionChannel
import com.zeroscam.core_domain.enums.RiskLevel
import com.zeroscam.core_domain.value.UserId
import java.time.Instant

/**
 * Résultat d'une détection ZeroScam (appel, message, paiement, device).
 */
data class DetectionResult(
    val id: String,
    val userId: UserId,
    val createdAt: Instant,
    val channel: DetectionChannel,
    val riskLevel: RiskLevel,
    val scamType: String?,
    val attackVectors: List<String>,
    val confidenceScore: Double,
    val reasons: List<String>,
    val recommendation: String,
)
