package com.zeroscam.core_domain.usecase

import com.zeroscam.core_domain.model.DetectionResult
import com.zeroscam.core_domain.model.DeviceSecuritySnapshot
import com.zeroscam.core_domain.ports.DetectionLogRepository
import com.zeroscam.core_domain.ports.DeviceThreatDetector
import com.zeroscam.core_domain.ports.ThreatIntelRepository

/**
 * Use case : évaluation de l’état de sécurité du device.
 *
 * Pipeline (Vigilis-like) :
 * 1. Analyse brute du snapshot via DeviceThreatDetector (root/jailbreak, emulator, apps douteuses).
 * 2. Ajustement via ThreatIntel (IOC device, profils compromis).
 * 3. Journalisation via DetectionLogRepository pour traçabilité & amélioration continue.
 */
class EvaluateDeviceSecurityStateUseCase(
    private val deviceThreatDetector: DeviceThreatDetector,
    private val threatIntelRepository: ThreatIntelRepository,
    private val detectionLogRepository: DetectionLogRepository,
) {
    operator fun invoke(snapshot: DeviceSecuritySnapshot): DetectionResult {
        val baseResult = deviceThreatDetector.analyze(snapshot)

        val intelAdjusted =
            threatIntelRepository.adjustDeviceSecurityResult(
                snapshot = snapshot,
                initialResult = baseResult,
            )

        detectionLogRepository.logDeviceSecurityDetection(
            snapshot = snapshot,
            result = intelAdjusted,
        )

        return intelAdjusted
    }
}
