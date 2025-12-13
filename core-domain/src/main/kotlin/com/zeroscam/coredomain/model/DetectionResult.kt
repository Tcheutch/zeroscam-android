package com.zeroscam.coredomain.model

import com.zeroscam.coredomain.enums.DetectionChannel
import com.zeroscam.coredomain.enums.RiskLevel
import com.zeroscam.coredomain.value.UserId
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
