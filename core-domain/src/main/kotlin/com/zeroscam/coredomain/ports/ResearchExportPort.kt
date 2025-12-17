package com.zeroscam.coredomain.ports

import com.zeroscam.coredomain.enums.RiskLevel
import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.value.UserId

/**
 * Snapshot de risque utilisateur multi-canal envoyé vers Vigilis-Research.
 */
data class UserRiskSnapshot(
    val userId: UserId,
    val globalRiskLevel: RiskLevel,
    val globalConfidence: Double,
    val detections: List<DetectionResult>,
)

/**
 * Port de sortie : export des signaux de risque ZeroScam
 * vers Vigilis-Research / moteurs analytiques.
 */
interface ResearchExportPort {
    fun publishUserRiskSnapshot(snapshot: UserRiskSnapshot)
}
