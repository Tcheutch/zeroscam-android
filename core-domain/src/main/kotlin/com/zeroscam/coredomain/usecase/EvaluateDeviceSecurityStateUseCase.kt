package com.zeroscam.coredomain.usecase

import com.zeroscam.coredomain.model.DetectionResult
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.ports.DetectionLogRepository
import com.zeroscam.coredomain.ports.DeviceThreatDetector
import com.zeroscam.coredomain.ports.ThreatIntelRepository

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
